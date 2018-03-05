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
    private static boolean finished = false;// checks if all are executed

    public static void main(String[] args) throws InterruptedException {
    	currentDirectory = new File(System.getProperty("user.dir"));
    	instructionSet = new File(args[0]);
    	lock = new Object();
    	
    	
    	// create a ProcessGraph from the instructions file
        ParseFile.generateGraph(new File(currentDirectory.getAbsolutePath() 
        		+ "/" + instructionSet));
        // print graph information
        ProcessGraph.printGraph();
        
        ArrayList<ProcessGraphNode> allNodes = ProcessGraph.nodes;
        ArrayList<ProcessGraphNode> unexecutedNodes;
        ArrayList<ProcessGraphNode> runnableNodes;

        while (!finished) {
        	// using index of ProcessGraph, loop through each ProcessGraphNode, to check whether it is ready to run
        	
        	// check if all the nodes are executed
	        unexecutedNodes = getUnexecutedNodes(allNodes);
	        // mark all the runnable nodes
	        runnableNodes = markRunnableNodes(unexecutedNodes);
	        // run the node if it is runnable
	        executeRunnableNodes(runnableNodes);
//	        if (node unexecuted but not runnable == currently running. wait until it finishes then run the loop again){
	        if (unexecutedNodes.size() == 0){
	        	finished = true;
	        }
	    }
        
        
        System.out.println("\nAll processes finished successfully");
    }
    
    /*
     * Checks for and consolidates unexecuted nodes in the ProcessGraph
     * @param allNodes - list of all nodes in the ProcessGraph
     * @return list of unexecuted nodes in the ProcessGraph, empty if all nodes have been executed
     * for nodes currently running, do not set as unexecuted, access the lock to 
     */
    public static ArrayList<ProcessGraphNode> getUnexecutedNodes(ArrayList<ProcessGraphNode> 
    		allNodes) {
    	ArrayList<ProcessGraphNode> unexecutedNodes = new ArrayList<>();
    	for (ProcessGraphNode node : allNodes) {
    		if (node.isExecuted() == false) { 
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
    public static ArrayList<ProcessGraphNode> markRunnableNodes(ArrayList<ProcessGraphNode> 
    		unexecutedNodes) {
    	ArrayList<ProcessGraphNode> runnableNodes = new ArrayList<>();
    	for (ProcessGraphNode node : unexecutedNodes) {
    		// nodes without parents or nodes with all control dependencies are cleared can be run.
    		if (node.getParents().isEmpty()|| node.allParentsExecuted()){ // || node.isrunnable() 
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