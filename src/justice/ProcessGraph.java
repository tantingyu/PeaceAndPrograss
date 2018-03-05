package justice;

import java.util.ArrayList;

public class ProcessGraph {
	
	// contains all nodes
    public static ArrayList<ProcessGraphNode> nodes = new ArrayList<>();

    // add node if not yet created
    public static void addNode(int index) {
        if (index >= nodes.size()) {
            nodes.add(new ProcessGraphNode(index));
        }
    }

    // print all information
    public static void printGraph() {
        // System.out.println();
        System.out.println("\nGraph Info:");
        try {
            for (ProcessGraphNode node : nodes) {
                System.out.print("\nNode "+ node.getNodeId()+": \nParent: ");
                if (node.getParents().isEmpty()) System.out.print("none");
                for (ProcessGraphNode parentnode : node.getParents()) {
                    System.out.print(parentnode.getNodeId() + " ");
                }
                System.out.print(" \nChildren: ");
                if (node.getChildren().isEmpty()) System.out.print("none");
                for (ProcessGraphNode childnode :
                        node.getChildren()) {
                    System.out.print(childnode.getNodeId() + " ");
                }
                System.out.print("\nCommand: "+node.getCommand() + "    ");
                System.out.print("\nInput File: "+ node.getInputFile() + "    ");
                System.out.println("\nOutput File: " + node.getOutputFile() + "    ");
                System.out.println("Runnable: " + node.isRunnable());
                System.out.println("Executed: "+ node.isExecuted());
                // System.out.println("\n");
            }
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    // print basic information
    public static void printBasic() {
        System.out.println("\nBasic Info:");
        for (ProcessGraphNode node : nodes) {
            System.out.println("\nNode: " + node.getNodeId() + " Runnable: " + node.isRunnable() 
            		+ " Executed: " + node.isExecuted());
        }
    }


}
