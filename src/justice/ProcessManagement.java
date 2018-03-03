package justice;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessManagement {

    private static File currentDirectory = new File("");			// set working directory
    private static File instructionSet = new File("test1.txt");		// set instructions file
    public static Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
    	// create a ProcessGraph from the instructions file
        ParseFile.generateGraph(new File(currentDirectory.getAbsolutePath() 
        		+ "/" + instructionSet));
        ProcessGraph.printGraph();
        
        // print the graph information
        // WRITE YOUR CODE

        // using index of ProcessGraph, loop through each ProcessGraphNode, to check whether it is ready to run
        // check if all the nodes are executed
        // WRITE YOUR CODE

        // mark all the runnable nodes
        // WRITE YOUR CODE

        // run the node if it is runnable
        // WRITE YOUR CODE

        System.out.println("\nAll processes finished successfully");
    }
}

class NodeThread extends Thread {
	
	File currentDirectory;
	String[] commandList;
	
	NodeThread(File currentDirectory, String[] commandList) {
		this.currentDirectory = currentDirectory;
		this.commandList = commandList;
	}
	
	@Override
	public void run() {
		ProcessBuilder pb = new ProcessBuilder(commandList);
    	pb.directory(currentDirectory);
    	
    	try {
    		Process p = pb.start();
    		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
    		
    		String line;
    		while ((line = br.readLine()) != null)
    			System.out.println(line);
    		br.close();
    	} catch (IOException e) {
    		System.out.println(e.getMessage());
    	}
	}
}
