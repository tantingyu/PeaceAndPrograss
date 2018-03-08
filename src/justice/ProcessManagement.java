/* Programming Assignment 1
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: 06/03/2018
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProcessManagement {
	
    private static File currentDirectory;	// working directory
    private static File instructionSet;		// instructions file
    
    public static List<NodeThread> threads;	// node threads

    public static void main(String[] args) throws InterruptedException {
    	currentDirectory = new File(System.getProperty("user.dir"));
    	instructionSet = new File(args[0]);
    	
    	// create a ProcessGraph from the instructions file
        boolean parse = ParseFile.generateGraph(new File(currentDirectory.getAbsolutePath() 
        		+ "/" + instructionSet));
        if (!parse) {
        	return;
        }
        ProcessGraph.printGraph();
        
        // create threads for every node in the ProcessGraph
        threads = new ArrayList<>();
        for (ProcessGraphNode node : ProcessGraph.nodes) {
        	threads.add(new NodeThread(node));
        }
        
        while (true) {
        	// print basic graph information
        	ProcessGraph.printBasic();
        	
        	// retrieve unexecuted nodes, exit loop if none
	        List<ProcessGraphNode> unexecutedNodes = unexecutedNodes(ProcessGraph.nodes);
	        if (unexecutedNodes.isEmpty()) {
	        	break;
	        }
	        
		// disqualify nodes that are awaiting dependencies
		List<ProcessGraphNode> runnableNodes = markRunnableNodes(unexecutedNodes);
		// start nodes that are eligible to run
		for (ProcessGraphNode node : runnableNodes) {
			NodeThread thread = threads.get(node.getNodeId());
			// ensure that threads are executed only once
			if (thread.getState() == Thread.State.NEW) {
				thread.start();
			}
		}
	}
        System.out.println("\nAll processes finished successfully");
    }
    
    /* Checks for unexecuted nodes in the ProcessGraph and 
     * stores them in a list
     * @param allNodes - list of all nodes in the ProcessGraph
     * @return list of unexecuted nodes in the ProcessGraph
     */
    public static List<ProcessGraphNode> unexecutedNodes(List<ProcessGraphNode> 
			allNodes) {
    	List<ProcessGraphNode> unexecutedNodes = new ArrayList<>();
    	for (ProcessGraphNode node : allNodes) {
    		if (!node.isExecuted()) {
    			unexecutedNodes.add(node);
    		}
    	}
    	return unexecutedNodes;
    }
    
    /* Checks for runnable nodes from a list of unexecuted nodes and 
     * stores them in a new list
     * @param unexecutedNodes - list of unexecuted nodes in the ProcessGraph
     * @return list of runnable nodes in the ProcessGraph
     */
    public static List<ProcessGraphNode> markRunnableNodes(List<ProcessGraphNode> 
    		unexecutedNodes) {
    	List<ProcessGraphNode> runnableNodes = new ArrayList<>();
    	for (ProcessGraphNode node : unexecutedNodes) {
    		// nodes with all parents completed are cleared to run
    		if (node.allParentsExecuted()){ 
    			runnableNodes.add(node);
    			// set node's runnable property to true
    			node.setRunnable();
    		}
    	}
    	return runnableNodes;
    }
}
