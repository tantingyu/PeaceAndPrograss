package justice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class NodeThread extends Thread {
	
	ProcessGraphNode node;
	String[] commandList;
	boolean sharedRes;
	
	NodeThread(ProcessGraphNode node) {
		sharedRes = false;
		// augment command to redirect input and output if not default
		String command = node.getCommand();
		if (!node.getInputFile().getName().equals("stdin")) {
			command += " < " + node.getInputFile();
			sharedRes = true;
		}
		if (!node.getOutputFile().getName().equals("stdout")) {
			command += " > " + node.getOutputFile();
			sharedRes = true;
		}
		commandList = command.split(" ");
		this.node = node;
	}
	
	@Override
	public void run() {
		if (sharedRes) {
	    	synchronized(ProcessManagement.lock) {
	    		doStuff();
	    	}
		} else {
			doStuff();
		}
	}
	
	public void doStuff() {
		try {
    		ProcessBuilder pb = new ProcessBuilder(commandList);
    		// set node's runnable property to false
    		node.setNotRunnable();
    		// read and display console output
    		BufferedReader br = new BufferedReader(new 
					InputStreamReader(pb.start().getInputStream()));
			for (String line; (line = br.readLine()) != null;) {
				System.out.println(line);
			}
			br.close();
			// set node's executed property to true
    		node.setExecuted();
    	} catch (IOException e) {
    		System.out.println(e);
    	}
	}
}
