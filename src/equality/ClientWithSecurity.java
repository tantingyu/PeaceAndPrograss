package equality;

/* Programming Assignment 2
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: -
 */

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.Key;

public class ClientWithSecurity {
	
	static final String FILENAME = "secure-file-transfer/rr.txt";
	static int cp;
	
	static Socket clientSocket;
	static DataOutputStream toServer;
	static DataInputStream fromServer;
	static Key sessionKey; // RSA public key, or AES symmetric key
	
	public static void main(String[] args) {
		long timeStarted = System.nanoTime();
		try {
			System.out.println("Establishing connection to server...");

			// connect to server and get the input and output streams
			clientSocket = new Socket("localhost", 4321);
			toServer = new DataOutputStream(clientSocket.getOutputStream());
			fromServer = new DataInputStream(clientSocket.getInputStream());
			
			/* TODO: Send certificate request and wait for response. */
			
			/* TODO: Verify certificate, decrypt with server's public key. */
			
			/**
			 * TODO: 
			 * CP-1 Request for RSA public key.
			 * CP-2	Generate AES symmetric key for secure file transfer, 
			 * 		encrypt with server's public key. 
			 */

			System.out.println("Sending file...");
			sendFile();
			
			System.out.println("Closing connection...");
	        closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long timeTaken = System.nanoTime() - timeStarted;
		System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");
	}
	
	static void sendFile() throws IOException {
		// send the filename
		
		/**
		 * TODO:
		 * CP-1 Encrypt with RSA public key.
		 * CP-2 Encrpyt with AES symmetric key.
		 */
		if (cp == 1) {
			// encryptRSA(block)
		} else if (cp == 2) {
			// encryptAES(block)
		}
		
		toServer.writeInt(0);
		toServer.writeInt(FILENAME.getBytes().length);
		toServer.write(FILENAME.getBytes());
		toServer.flush();

		// open the file
		FileInputStream fileInputStream = new FileInputStream(FILENAME);
		BufferedInputStream bufferedFileInputStream = 
				new BufferedInputStream(fileInputStream);
        byte[] fromFileBuffer = new byte[117];

        // send the file
        int numBytes = 0;
        for (boolean fileEnded = false; !fileEnded;) {
			numBytes = bufferedFileInputStream.read(fromFileBuffer);
			fileEnded = numBytes < fromFileBuffer.length;
			
			/**
			 * TODO:
			 * CP-1 Encrypt with RSA public key.
			 * CP-2 Encrpyt with AES symmetric key.
			 */
			if (cp == 1) {
				// encryptRSA(block)
			} else if (cp == 2) {
				// encryptAES(block)
			}
			
			toServer.writeInt(1);
			toServer.writeInt(numBytes);
			toServer.write(fromFileBuffer);
			toServer.flush();
		}
        
        // close input streams
        bufferedFileInputStream.close();
        fileInputStream.close();
	}
	
	static void closeConnection() throws IOException {
		toServer.writeInt(2);
        toServer.flush();
        fromServer.close();
        clientSocket.close();
	}
	
	byte[] encryptRSA(byte[] block) {
		return new byte[0];
	}
	
	byte[] encryptAES(byte[] block) {
		return new byte[0];
	}
}
