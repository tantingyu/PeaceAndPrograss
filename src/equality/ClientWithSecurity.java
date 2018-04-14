package equality;

/* Programming Assignment 2
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: -
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class ClientWithSecurity extends Thread {
	
	static final String SERVER_IP = "10.12.63.173";
	static final int PORT_NUMBER = 4321;
	static final int CP = 1;
	
	static final String FILE_NAME = "dataforfit.txt";
	static final String CA_CERT = "CA.crt";
	public int cp = 1;
	
	Socket clientSocket;
	DataOutputStream toServer;
	DataInputStream fromServer;
	
	Key sessionKey; // RSA public key, or AES symmetric key
	Cipher cipher;
	
	public ClientWithSecurity(String serverIP, int portNumber, int cp) throws Exception {
		clientSocket = new Socket(serverIP, portNumber);
		fromServer = new DataInputStream(clientSocket.getInputStream());
		toServer = new DataOutputStream(clientSocket.getOutputStream());
		this.cp = cp;
	}
	
	@Override
	public void run() {
		long timeStarted = System.nanoTime();
		try {
			int numBytes = 0;
			
			// request server's certificate
			print("Requesting server's certificate.");
			byte[] certRequest  = "Please send me your certificate.".getBytes();
			toServer.writeInt(certRequest.length);
			toServer.write(certRequest);
			toServer.flush();
			
			// receive server's certificate
			numBytes = fromServer.readInt();
			byte[] serverCertBytes = new byte[numBytes];
			fromServer.readFully(serverCertBytes);
			print("Received server's certificate.");
			
			// verify server's certificate using CA's public key and extract server's
			// public key
			Key rsaPublicKey = verifyCertificate(serverCertBytes);
	
			// return server's handshake
			print("Server's certificate verified, returning handshake.");
			byte[] handshake = "Your certificate has been verified.".getBytes();
			toServer.writeInt(handshake.length);
			toServer.write(handshake);
			toServer.flush();
			
			// configure cipher
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			
			// blocks will be encrypted using server's RSA public key
			if (cp == 1) {
				sessionKey = rsaPublicKey;
			// blocks will be encrypted using client's AES symmetric key
			} else if (cp == 2) {
				// generate AES symmetric key for file transfer
				KeyGenerator keyGen = KeyGenerator.getInstance("AES");
				keyGen.init(128);
				Key aesKey = keyGen.generateKey();
				sessionKey = aesKey;
				
				// encrypt AES symmetric key with RSA public key and send to server
				print("Sending encrypted AES symmetric key to server.");
				byte[] aesKeyEncrypted = encryptData(aesKey.getEncoded(), rsaPublicKey);
				toServer.writeInt(aesKeyEncrypted.length);
				toServer.write(aesKeyEncrypted);
				toServer.flush();
				
				// reconfigure cipher
				cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			}
			
			File file = new File(FILE_NAME); 
			print("Length of File: " + file.length());
			
			// send file name
			print("Starting secure file transfer, sending file name.");
			byte[] encryptedFileName = encryptData(FILE_NAME.getBytes(), sessionKey);
			toServer.writeInt(0);
			toServer.writeInt(encryptedFileName.length);
			toServer.write(encryptedFileName);
			toServer.flush();
			
			// open file
			FileInputStream fileInputStream = new FileInputStream(FILE_NAME);
			BufferedInputStream bufferedFileInputStream = 
					new BufferedInputStream(fileInputStream);
			byte[] fromFileBuffer = new byte[117];
	        
			// upload file
	        for (boolean fileEnded = false; !fileEnded;) {
				numBytes = bufferedFileInputStream.read(fromFileBuffer);
				fileEnded = numBytes < fromFileBuffer.length;
				
				byte[] encryptedData = encryptData(fromFileBuffer, sessionKey);
				toServer.writeInt(1);
				toServer.writeInt(numBytes);
				toServer.write(encryptedData);
				toServer.flush();
			}
			
	        print("Upload complete, notifying server.");
			byte[] notifyComplete = "File upload complete.".getBytes();
			toServer.writeInt(2);
			toServer.writeInt(notifyComplete.length);
			toServer.write(notifyComplete);
			toServer.flush();
			
	        // close streams
	        bufferedFileInputStream.close();
	        fileInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long timeTaken = System.nanoTime() - timeStarted;
		System.out.println("Transfer Time: " + timeTaken/1000000.0 + " ms");
	}
	
	Key verifyCertificate(byte[] serverCertBytes) throws Exception {
		// get public key from CA
		Certificate caCert = CertificateFactory.getInstance("X.509")
				.generateCertificate(new FileInputStream(CA_CERT));
		Key caPublicKey = caCert.getPublicKey();
		
		// verify server's certificate
		X509Certificate serverCert = (X509Certificate) CertificateFactory.getInstance("X.509")
				.generateCertificate(new ByteArrayInputStream(serverCertBytes));
		serverCert.checkValidity();
		serverCert.verify((PublicKey) caPublicKey);

		return serverCert.getPublicKey();
	}
	
	byte[] encryptData(byte[] block, Key key) throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(block);
	}
	
	void print(String message) {
		System.out.println("Client Console -\n\t" + message);
	}
	
	public static void main(String[] args) throws Exception {
		ClientWithSecurity client = new ClientWithSecurity(SERVER_IP, PORT_NUMBER, CP);
		// ClientWithSecurity client = new ClientWithSecurity(args[0], Integer.parseInt(args[1]), 
		//		Integer.parseInt(args[2]));
		client.start();
	}
}
