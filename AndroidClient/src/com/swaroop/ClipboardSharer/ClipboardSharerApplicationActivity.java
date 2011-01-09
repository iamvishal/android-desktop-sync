package com.swaroop.ClipboardSharer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ClipboardSharerApplicationActivity extends Activity {

	static String cachedDeviceClipBoard;
	static String cachedDesktopClipBoard;
	static boolean clientThreadAliveToggle = true;
	private static Button startSyncButton;
	private static EditText editText;

	static ClipboardManager clipboardManager;
	static ClipboardSharerApplicationActivity currentActivity;

	public static ClipboardSharerApplicationActivity getInstance() {
		return currentActivity;
	}

	/**
	 * Starts the sync process after the user presses the 'Sync button'
	 */
	private final Button.OnClickListener startSyncButtonOnClick = new Button.OnClickListener() {
		public void onClick(View v) {
			Thread clientThread = new Thread(new ClientThread());
			clientThread.start();
		}
	};
	
	/**
	 * Is triggered when the Exit button is clicked. This finishes the activity
	 */
	private final Button.OnClickListener exitButtonOnClick = new Button.OnClickListener() {
		public void onClick(View v) {
			clientThreadAliveToggle = false;
			currentActivity.finish();
		}
	};


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		currentActivity = this;
		clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		editText = (EditText) findViewById(R.id.IPAddressInputField);
		startSyncButton = (Button) findViewById(R.id.StartSyncButton);

		startSyncButton.setOnClickListener(startSyncButtonOnClick);
		Button exitAppButton = (Button) findViewById(R.id.ExitSyncClientButton);
		exitAppButton.setOnClickListener(exitButtonOnClick);
	}

	private static class ClientThread implements Runnable {

		private static final String CLIPBOARD_SHARER_LOG_TAG = "ClipboardSharer";

		public void run() {
			String inputIP = editText.getText().toString();
			Log.d(CLIPBOARD_SHARER_LOG_TAG, "User entered IP Address = " + inputIP);
			InetAddress serverAddress;
			try {
				while(clientThreadAliveToggle) {
					serverAddress = InetAddress.getByName(inputIP);
					Socket socket = new Socket(serverAddress, 8686);

					// Check if the device has to send any data to server
					ObjectOutputStream oos = sendDataToServer(socket);
					
					// Check if the server has sent some data.
					ObjectInputStream ois = receiveDataFromServer(socket);

					oos.close();
					ois.close();
					socket.close();
					
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Checks if any data has to be sent to the server
		 * @param socket
		 * @return
		 * @throws IOException
		 */
		private ObjectOutputStream sendDataToServer(Socket socket) throws IOException {

			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

			if(isDeviceClipBoardChanged()) {
				Log.d(CLIPBOARD_SHARER_LOG_TAG, "Device clipboard changed, sending update to server " + cachedDeviceClipBoard);
				oos.writeObject(cachedDeviceClipBoard);
			} else {
				Log.d(CLIPBOARD_SHARER_LOG_TAG, "Device clipboard not-changed, sending dummy request");
				oos.writeObject("");
			}
		
			return oos;
		}

		/**
		 * Checks if the server has some data
		 * @param socket
		 * @return
		 * @throws IOException
		 * @throws ClassNotFoundException
		 */
		public ObjectInputStream receiveDataFromServer(Socket socket) throws IOException, ClassNotFoundException {
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			
			String receivedStringFromServer = (String) ois.readObject();

			if(!"".equals(receivedStringFromServer)) {
				Log.d(CLIPBOARD_SHARER_LOG_TAG, "Received a string from server, Desktop clipboard updated " + receivedStringFromServer);
				setDeviceClipBoard(receivedStringFromServer.toString());
				cachedDesktopClipBoard = receivedStringFromServer.toString();
			}	
			return ois;
		}
		
		public static String getDeviceClipBoard() {
			return clipboardManager.getText().toString();
		}

		public static void setDeviceClipBoard(String updatedClipboard) {
			cachedDeviceClipBoard = updatedClipboard;
			clipboardManager.setText(updatedClipboard);
		}

		public static boolean isDeviceClipBoardChanged() {
			String currentDeviceClipBoard = getDeviceClipBoard();
			if(currentDeviceClipBoard.equals(cachedDeviceClipBoard)) {
				return false;
			}
			cachedDeviceClipBoard = currentDeviceClipBoard;

			return true;
		}

	}


}