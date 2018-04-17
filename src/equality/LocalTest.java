package equality;

/* Programming Assignment 2
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: 17/4/2018
 */

import java.util.Scanner;

public class LocalTest {
	
	// change these fields as required
	static final String SERVER_IP = "localhost";
	static final int PORT_NUMBER = 4321;
	static final String FILE_NAME = "rr.txt";
	
	public static void main(String[] args) throws Exception {
		Scanner input = new Scanner(System.in);
		int selection = 0;
		
		while (true) {
			System.out.println("Select Confidentiality Protocol:\n"
					+ "1. RSA Encryption\n"
					+ "2. AES Encryption");
			
			selection = input.nextInt();
			if (selection == 1 | selection == 2) {
				System.out.println();
				input.close();
				break;
			}
			System.out.println("Invalid selection, try agian.");
		}
		
		ServerWithSecurity server = new ServerWithSecurity(4321, selection);
		server.start();
		ClientWithSecurity client = new ClientWithSecurity(SERVER_IP, PORT_NUMBER, 
				FILE_NAME, selection);
		client.start();
	}
}