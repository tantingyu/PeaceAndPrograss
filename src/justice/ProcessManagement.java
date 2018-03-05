package justice;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ProcessManagement {
	
    private static File currentDirectory;	// working directory
    private static File instructionSet;		// instructions file
    public static Object lock;				// resource lock

    public static void main(String[] args) throws InterruptedException {
    	currentDirectory = new File(System.getProperty("user.dir"));
    	instructionSet = new File(args[0]);
    	lock = new Object();
    	
    	// create a ProcessGraph from the instructions file
        ParseFile.generateGraph(new File(currentDirectory.getAbsolutePath() 
        		+ "/" + instructionSet));
        ProcessGraph.printGraph();
        
        /*
        while (!finished) {
	        print the graph information
	        WRITE YOUR CODE
	
	        using index of ProcessGraph, loop through each ProcessGraphNode, to check whether it is ready to run
	        check if all the nodes are executed
	        WRITE YOUR CODE - use getUnexecutedNodes method
	
	        mark all the runnable nodes
	        WRITE YOUR CODE - use getRunnableNodes method
	
	        run the node if it is runnable
	        WRITE YOUR CODE - use executeRunnableNodes method
	    }
        */
        
        System.out.println("\nAll processes finished successfully");
    }
    
    /*
     * Checks for and consolidates unexecuted nodes in the ProcessGraph
     * @param allNodes - list of all nodes in the ProcessGraph
     * @return list of unexecuted nodes in the ProcessGraph, empty if all nodes have been executed
     */
    public static ArrayList<ProcessGraphNode> getUnexecutedNodes(ArrayList<ProcessGraphNode> 
    		allNodes) {
    	ArrayList<ProcessGraphNode> unexecutedNodes = new ArrayList<>();
    	for (ProcessGraphNode node : allNodes) {
    		if (!node.isExecuted()) {
    			unexecutedNodes.add(node);
    		}
    	}
    	return unexecutedNodes;
    }
    
    /*
     * Checks for and consolidates runnable nodes in the ProcessGraph
     * @param unexecutedNodes - list of unexecuted nodes in the ProcessGraph
     * @return list of runnable nodes in the ProcessGraph, should not be empty
     */
    public static ArrayList<ProcessGraphNode> getRunnableNodes(ArrayList<ProcessGraphNode> 
    		unexecutedNodes) {
    	ArrayList<ProcessGraphNode> runnableNodes = new ArrayList<>();
    	for (ProcessGraphNode node : unexecutedNodes) {
    		if (node.allParentsExecuted()) {
    			runnableNodes.add(node);
    			// set node's runnable property to true
    			node.setRunnable();
    		}
    	}
    	return runnableNodes;
    }
    
    /*
     * Executes runnable nodes in the ProcessGraph as threads
     * @param runnableNodes - list of runnable nodes in the ProcessGraph
     */
    public static void executeRunnableNodes(ArrayList<ProcessGraphNode> runnableNodes) {
    	// create threads and start
    	for (ProcessGraphNode node : runnableNodes) {
    		new NodeThread(node).start();
    	}
    }
}

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
    		node.setNotRunnable();
    		node.setExecuted();
    		// read and display console output
    		BufferedReader br = new BufferedReader(new 
					InputStreamReader(pb.start().getInputStream()));
			for (String line; (line = br.readLine()) != null;) {
				System.out.println(line);
			}
			br.close();
    	} catch (IOException e) {
    		System.out.println(e);
    	}
	}
}
