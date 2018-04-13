package justice;

/* Programming Assignment 1
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: 06/03/2018
 */

import java.util.ArrayList;
import java.util.List;

public class ProcessGraph {
	
    public static List<ProcessGraphNode> nodes = new ArrayList<>();		// contains all nodes

    // add node if not yet created
    public static void addNode(int index) {
        if (index >= nodes.size()) {
            nodes.add(new ProcessGraphNode(index));
        }
    }

    // prints the full information of every node in the graph
    public static void printGraph() {
        System.out.println("\nGraph Info:");
        try {
            for (ProcessGraphNode node : nodes) {
                System.out.println("\nNode "+ node.getNodeId() + ": ");
                System.out.print("\nParent: ");
                if (node.getParents().isEmpty()) System.out.print("none");
                for (ProcessGraphNode parentnode : node.getParents()) {
                    System.out.print(parentnode.getNodeId() + " ");
                }
                System.out.print("\nChildren: ");
                if (node.getChildren().isEmpty()) System.out.print("none");
                for (ProcessGraphNode childnode :
                        node.getChildren()) {
                    System.out.print(childnode.getNodeId() + " ");
                }
                System.out.print("\nCommand: " + node.getCommand() + "    ");
                System.out.print("\nInput File: " + node.getInputFile() + "    ");
                System.out.print("\nOutput File: " + node.getOutputFile() + "    ");
                System.out.print("\nRunnable: " + node.isRunnable());
                System.out.print("\nExecuted: " + node.isExecuted() + "\n");
            }
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    // prints the basic information of every node in the graph
    public static void printBasic() {
        System.out.println("\nBasic Info:");
        for (ProcessGraphNode node : nodes) {
            System.out.println("Node: " + node.getNodeId() + " Runnable: " + node.isRunnable() 
            		+ " Executed: " + node.isExecuted());
        }
    }
}
