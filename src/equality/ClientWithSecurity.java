package equality;

/* Programming Assignment 2
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: -
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

public class ClientWithSecurity extends Thread {
	
	final String FILENAME = "secure-file-transfer/rr.txt";
	final String AUTHORITY_DIRECTORY = "secure-file-transfer/CA.crt";
	int cp;
	
	Socket clientSocket;
	DataOutputStream toServer;
	DataInputStream fromServer;
	
	Key sessionKey; // RSA public key, or AES symmetric key
	Cipher cipher;
	
	public ClientWithSecurity() throws IOException {
		// connect to server and get the input/output streams
		System.out.println("Establishing connection to server...");
		clientSocket = new Socket("localhost", 4321);
		fromServer = new DataInputStream(clientSocket.getInputStream());
		toServer = new DataOutputStream(clientSocket.getOutputStream());
	}
	
	@Override
	public void run() {
		long timeStarted = System.nanoTime();
		try {
			/* TODO: Send certificate request and wait for response. */
			
			byte[] message  = "Please send me your certificate.".getBytes();
			toServer.writeInt(message.length);
			toServer.write(message);
			toServer.flush();
			
			/* TODO: Verify certificate using CA's public key. */
			
			int numBytes = fromServer.readInt();
			byte[] encodedCert = new byte[numBytes];
			fromServer.readFully(encodedCert);
			
			// get public key from CA
			InputStream fileInputStream = new FileInputStream(AUTHORITY_DIRECTORY);
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(fileInputStream);
			PublicKey authorityKey = cert.getPublicKey();
			
			// verify server's certificate
			X509Certificate serverCert = (X509Certificate) certFactory
					.generateCertificate(new ByteArrayInputStream(encodedCert));
			serverCert.checkValidity();
			serverCert.verify(authorityKey);
			
			// send acknowledgement
			message = "Your certificate has been verified.".getBytes();
			toServer.writeInt(message.length);
			toServer.write(message);
			toServer.flush();
			
			/**
			 * TODO: 
			 * CP-1 Request for RSA public key.
			 * CP-2	Generate AES symmetric key for secure file transfer, 
			 * 		encrypt with server's public key. 
			 */
			if (cp == 1) {
				// configure cipher
				cipher = Cipher.getInstance("RSA/ECB/PKCS8Padding");
				
				// receive and decode RSA public key from server
				byte[] keyInBytes = new byte[117];
				fromServer.readFully(keyInBytes);
				EncodedKeySpec publicKeySpec = new PKCS8EncodedKeySpec(keyInBytes);
				sessionKey = KeyFactory.getInstance("RSA")
						.generatePublic(publicKeySpec);
			} else if (cp == 2) {
				cipher = Cipher.getInstance("AES/ECB/PKCS8Padding");
			}

			System.out.println("Sending file...");
			sendFile();
			
			System.out.println("Closing connection...");
	        closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long timeTaken = System.nanoTime() - timeStarted;
		System.out.println("Running Time: " + timeTaken/1000000.0 + "ms");
	}
	
	void sendFile() throws Exception {
		// send the filename
		toServer.writeInt(0);
		toServer.writeInt(FILENAME.getBytes().length);
		toServer.write(encryptData(FILENAME.getBytes()));
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
			fromFileBuffer = encryptData(fromFileBuffer);
			
			toServer.writeInt(1);
			toServer.writeInt(numBytes);
			toServer.write(fromFileBuffer);
			toServer.flush();
		}
        
        // close input streams
        bufferedFileInputStream.close();
        fileInputStream.close();
	}
	
	void closeConnection() throws IOException {
		toServer.writeInt(2);
        toServer.flush();
        fromServer.close();
        clientSocket.close();
	}
	
	byte[] encryptData(byte[] block) throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
		return cipher.doFinal(block);
	}
	
	byte[] decryptData(byte[] block) throws Exception {
		cipher.init(Cipher.DECRYPT_MODE, sessionKey);
		return cipher.doFinal(block);
	}
}
