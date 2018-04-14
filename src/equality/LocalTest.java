package equality;

import java.util.Scanner;

public class LocalTest {

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
		ClientWithSecurity client = new ClientWithSecurity("localhost", 4321, selection);
		client.start();
	}
}
