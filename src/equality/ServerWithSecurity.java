package equality;

/* Programming Assignment 2
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: -
 */

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class ServerWithSecurity {
	
	static final int SOCKET_NUMBER = 4321;
	static int cp;
	
	static List<ConnectionSocket> clientConnections;
	
	public static void main(String[] args) {
		try {
			ServerSocket welcomeSocket = new ServerSocket(SOCKET_NUMBER);
			clientConnections = new ArrayList<>();
	
			while (true) {
				ConnectionSocket newClient = new ConnectionSocket(welcomeSocket.accept());
				clientConnections.add(newClient);
				newClient.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static class ConnectionSocket extends Thread {
		
		Socket connectionSocket;
		Key sessionKey;	// RSA public key, or AES symmetric key
		
		DataInputStream fromClient;
		DataOutputStream toClient;
		FileOutputStream fileOutputStream;
		BufferedOutputStream bufferedFileOutputStream;
		
		ConnectionSocket(Socket connectionSocket) {
			this.connectionSocket = connectionSocket;
		}
		
		@Override
		public void run() {
			try {
				fromClient = new DataInputStream(connectionSocket.getInputStream());
				toClient = new DataOutputStream(connectionSocket.getOutputStream());
				
				/* TODO: Receive client hello, send certificate. */
				
				/** 
				 * TODO: 
				 * CP-1 Generate RSA key-pair, send public key to client.
				 * CP-2	Receive AES symmetric key from client. 
				 */
				
				// secure connection established, ready to accept file
				while (!connectionSocket.isClosed()) {
					int packetType = fromClient.readInt();

					// packet is for transferring the filename
					if (packetType == 0) {
						System.out.println("Receiving filename...");
						receiveFilename();
					// packet is for transferring a chunk of the file
					} else if (packetType == 1) {
						System.out.println("Receiving data...");
						writeDataToFile();
					} else if (packetType == 2) {
						System.out.println("Closing connection...");
						closeConnection();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		void receiveFilename() throws IOException {			
			/** 
			 * TODO:
			 * CP-1	Decrypt data with RSA private key (use method decryptRSA)
			 * CP-2	Decrypt data with AES symmetric key (use method decryptAES)
			 */
			if (cp == 1) {
				// decryptRSA(block)
			} else if (cp == 2) {
				// decryptAES(block)
			}
			
			int numBytes = fromClient.readInt();
			byte[] filename = new byte[numBytes];
			fromClient.readFully(filename);

			fileOutputStream = new FileOutputStream("recv/" + new String(filename, 0, numBytes));
			bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);
		}
		
		void writeDataToFile() throws IOException {
			/** 
			 * TODO:
			 * CP-1	Decrypt data with RSA private key.
			 * CP-2	Decrypt data with AES symmetric key.
			 */
			if (cp == 1) {
				// decryptRSA(block)
			} else if (cp == 2) {
				// decryptAES(block)
			}
			
			int numBytes = fromClient.readInt();
			byte[] block = new byte[numBytes];
			fromClient.readFully(block);
			
			if (numBytes > 0)
				bufferedFileOutputStream.write(block, 0, numBytes);
		}
		
		void closeConnection() throws IOException {
			if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
			if (bufferedFileOutputStream != null) fileOutputStream.close();
			fromClient.close();
			toClient.close();
			connectionSocket.close();
		}

		byte[] decryptRSA(byte[] block) {
			return new byte[0];
		}
		
		byte[] decryptAES(byte[] block) {
			return new byte[0];
		}
	}
}
