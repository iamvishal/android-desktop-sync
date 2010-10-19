package com.swaroop.projects;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class JavaDesktopClipboardAccessServer {

	static ServerSocket serverSocket;

	static String cachedDesktopClipboard;
	static String cachedMobileClipboard;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnsupportedFlavorException 
	 */
	public static void main(String[] args) throws UnsupportedFlavorException, IOException {

		Thread serverThread = new Thread(new ServerThread());
		serverThread.start();

		//		Thread clientThread = new Thread(new ClientThread());
		//		clientThread.start();

		//		Clipboard clipboard = readClipBoard();
		//
//		setDesktopClipBoard();
		//
		//		readClipBoard();
	}

	private static void setDesktopClipBoard(String toUpdateDesktopClipboard) {
		// Set's a text into the clipboard. 
		StringSelection stringSelection = new StringSelection(toUpdateDesktopClipboard);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, null);
		cachedDesktopClipboard = toUpdateDesktopClipboard;
	}

	private static String readClipBoard() throws UnsupportedFlavorException, IOException {
		// Reads what's in the clipboard. 
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		String clipBoardContent = ((String) contents.getTransferData(DataFlavor.stringFlavor));
		cachedDesktopClipboard = clipBoardContent;
		System.out.println("Text in the clipboard is \n" + clipBoardContent);
		return clipBoardContent;
	}

	public static boolean isDesktopClipboardLatest() {
		try {
			String latestDesktopClipboard = readClipBoard();
			return cachedDesktopClipboard.equals(latestDesktopClipboard);
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isMobileClipboardLatest(String mobileClipboardLatest) {
		return cachedMobileClipboard.equals(mobileClipboardLatest);
	}

	private static class ServerThread implements Runnable {

		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(8686);
				System.out.println("Listening on port  " + serverSocket.getLocalPort());
				while(true) {
					Socket client = serverSocket.accept();

					System.out.println("Synching information");
					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
					String inputString = null;
					StringBuffer totalReceivedString = new StringBuffer();
					while ((inputString = in.readLine()) != null) {
						//System.out.println(inputString);
						System.out.println("read line " + inputString);
						totalReceivedString.append(inputString);
					}
					
//					in.close();
					// Mobile clipboard has been sent for updation.
					if(!"".equals(totalReceivedString.toString())) {
						System.out.println("Mobile sent its clipboard " + totalReceivedString);
						setDesktopClipBoard(totalReceivedString.toString());
					} else {
						System.out.println("Mobile clipboard didn't change, received dummy request");
					}
					
//					// Check if server clipboard has been updated, then send it back to device
//					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
//					if(!isDesktopClipboardLatest()) {
//						System.out.println("Appears desktop clipboard has been updated too, sending it to device");
//						String latestDesktopClipboard = readClipBoard();
//						out.write(latestDesktopClipboard);
//					} else {
//						out.write("");
//					}
//					
//					out.flush();
//					out.close();
					

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
//			catch (UnsupportedFlavorException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
			finally {
				try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}



	//	private static class ClientThread implements Runnable {
	//
	//		@Override
	//		public void run() {
	//			InetAddress serverAddress;
	//			try {
	//				serverAddress = InetAddress.getByName("10.177.223.231");
	//				Socket socket = new Socket(serverAddress, 8585);
	////				while(true) {
	//					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
	//							.getOutputStream())), true);
	//					// where you issue the commands
	//					out.println("Client to Server, echo echo");
	////				}
	//			} catch (UnknownHostException e) {
	//				e.printStackTrace();
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//
	//		}

}


