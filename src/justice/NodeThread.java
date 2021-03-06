package justice;

/* Programming Assignment 1
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: 06/03/2018
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class NodeThread extends Thread {
	
	ProcessGraphNode node;
	String[] commandList;
	
	NodeThread(ProcessGraphNode node) {
		this.node = node;
		commandList = node.getCommand().split(" ");
	}
	
	@Override
	public void run() {
		try {
    		ProcessBuilder pb = new ProcessBuilder(commandList);
    		// redirect input and output if not default
    		if (!node.getInputFile().getName().equals("stdin")) {
    			pb.redirectInput(node.getInputFile());
    		}
    		if (!node.getOutputFile().getName().equals("stdout")) {
    			pb.redirectOutput(node.getOutputFile());
    		}
    		
    		// read and display console output
    		BufferedReader br = new BufferedReader(new 
					InputStreamReader(pb.start().getInputStream()));
			for (String line; (line = br.readLine()) != null;) {
				System.out.println(line);
			}
			br.close();
			
			// set executed to true
			node.setExecuted();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}
}
