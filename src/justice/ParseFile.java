package justice;

/* Programming Assignment 1
 * Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)
 * Date: 06/03/2018
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ParseFile {
	
    // generates a ProcessGraph from a given input file
    public static boolean generateGraph(File inputFile) {
        try {
        	List<Integer> edgeParents = new ArrayList<Integer>();
            List<Integer> edgeChildren = new ArrayList<Integer>();
        	Scanner fileIn = new Scanner(inputFile);
        	
            int index = 0;
            while (fileIn.hasNext()) {
                String line = fileIn.nextLine();
                String[] quatiles = line.split(":");
                if (quatiles.length != 4) {
                    System.out.println("Wrong input format");
                    throw new Exception();
                }
                
                // add node
                ProcessGraph.addNode(index);
                // add edges
                if (!quatiles[1].equals("none")) {
                    String[] childrenStringArray = quatiles[1].split(" ");
                    int[] childrenId = new int[childrenStringArray.length];
                    for (int i = 0; i < childrenId.length; i++) {
                        childrenId[i]= Integer.parseInt(childrenStringArray[i]);
                        edgeParents.add(index);
                        edgeChildren.add(childrenId[i]);
                    }
                }
                                              
                // set command
                ProcessGraph.nodes.get(index).setCommand(quatiles[0]);
                // set input file
                File redirInp = new File(quatiles[2]);
                if (redirInp.exists() || quatiles[2].equals("stdin")) {
                	ProcessGraph.nodes.get(index).setInputFile(new File(quatiles[2]));
                } else {
                	System.out.println("Error in Node " + index + ": Invalid input file");
                	return false;
                }
                // set output file
                ProcessGraph.nodes.get(index).setOutputFile(new File(quatiles[3]));
                index += 1;
            }
            
            // set children
            for (int i = 0; i < edgeParents.size(); i++) {
                int p = edgeParents.get(i);
                int c = edgeChildren.get(i);
                ProcessGraph.nodes.get(p).addChild(ProcessGraph.nodes.get(c));
            }
            
            // set parents
            for (ProcessGraphNode node : ProcessGraph.nodes) {
                for (ProcessGraphNode childNode : node.getChildren()) {
                	ProcessGraph.nodes.get(childNode.getNodeId())
                		.addParent(ProcessGraph.nodes.get(node.getNodeId()));
                }
            }
            
            // set nodes with no parents as runnable
            for (ProcessGraphNode node : ProcessGraph.nodes) {
                if (node.getParents().isEmpty()) {
                    node.setRunnable();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
