package justice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class NodeThread extends Thread {
	
	ProcessGraphNode node;
	String[] commandList;
	
	NodeThread(ProcessGraphNode node) {
		this.node = node;
		// augment command to redirect input and output if not default
		String command = node.getCommand();
		if (!node.getInputFile().getName().equals("stdin")) {
			command += " < " + node.getInputFile();
		}
		if (!node.getOutputFile().getName().equals("stdout")) {
			command += " > " + node.getOutputFile();
		}
		commandList = command.split(" ");
		// set runnable to false
		node.setNotRunnable();
	}
	
	@Override
	public void run() {
		try {
    		ProcessBuilder pb = new ProcessBuilder(commandList);
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
    		System.out.println(e);
    	}
	}
}
