package com.swaroop.ClipboardSharer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;

public class ClipboardSharerApplicationActivity extends Activity {

	static String cachedDeviceClipBoard;
	static String cachedDesktopClipBoard;

	ClipboardManager clipboardManager;
	static ClipboardSharerApplicationActivity currentActivity;

	public static ClipboardSharerApplicationActivity getInstance() {
		return currentActivity;
	}


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//		setContentView(R.layout.main);

		currentActivity = this;
		clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		Thread clientThread = new Thread(new ClientThread());
		clientThread.start();
	}

	public String getDeviceClipBoard() {
		return this.clipboardManager.getText().toString();
	}

	public void setDeviceClipBoard(String updatedClipboard) {
		cachedDeviceClipBoard = updatedClipboard;
		this.clipboardManager.setText(updatedClipboard);
	}

	public boolean isDeviceClipBoardChanged() {
		String currentDeviceClipBoard = currentActivity.getDeviceClipBoard();
		if(currentDeviceClipBoard.equals(cachedDeviceClipBoard)) {
			return false;
		}
		cachedDeviceClipBoard = currentDeviceClipBoard;

		return true;
	}

	private static class ClientThread implements Runnable {

		public void run() {
			InetAddress serverAddress;
			try {
				while(true) {
					serverAddress = InetAddress.getByName("10.0.0.6");
					Socket socket = new Socket(serverAddress, 8686);

					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
							.getOutputStream())), true);

					if(ClipboardSharerApplicationActivity.getInstance().isDeviceClipBoardChanged()) {
						System.out.println("Device clipboard changed, sending update to server " + cachedDeviceClipBoard);
						out.println(cachedDeviceClipBoard);
					} else {
						System.out.println("Device clipboard not-changed, sending dummy request");
						out.println("");
					}
					out.flush();
					out.close();

//					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//					StringBuffer receivedStringFromServer = new StringBuffer();
//					String inputString = "";
//					while ((inputString = in.readLine()) != null) {
//						//Syst.out.println(inputString);
//						receivedStringFromServer.append(inputString);
//					}
//
//					in.close();
//
//					if(!"".equals(receivedStringFromServer)) {
//						System.out.println("Received a string from server, Desktop clipboard updated " + receivedStringFromServer);
//						ClipboardSharerApplicationActivity.getInstance().setDeviceClipBoard(receivedStringFromServer.toString());
//						cachedDesktopClipBoard = receivedStringFromServer.toString();
//					}

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
			}
		}


	}
}