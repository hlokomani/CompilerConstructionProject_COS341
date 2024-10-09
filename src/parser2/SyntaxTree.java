package parser2;

import java.util.HashMap;
import java.util.Map;

public class SyntaxTree {
    private Node root;
    private Map<Integer, Node> nodeMap;
    private int currentUnid;

    public SyntaxTree() {
        this.nodeMap = new HashMap<>();
        this.currentUnid = 1; // Start UNID numbering from 1
    }

    public void initializeRoot(String startSymbol) {
        root = new Node(currentUnid, startSymbol);
        nodeMap.put(currentUnid, root);
        currentUnid++;
    }

    public int addInnerNode(int parentUnid, String symb) {
        Node parent = nodeMap.get(parentUnid);
        if (parent == null) {
            throw new IllegalArgumentException("Parent UNID " + parentUnid + " does not exist.");
        }
        Node node = new Node(currentUnid, symb);
        parent.addChild(node);
        nodeMap.put(currentUnid, node);
        currentUnid++;
        return node.getUnid();
    }

    public int addLeafNode(int parentUnid, String terminalXML) {
        Node parent = nodeMap.get(parentUnid);
        if (parent == null) {
            throw new IllegalArgumentException("Parent UNID " + parentUnid + " does not exist.");
        }
        // Extract the terminal symbol from terminalXML if necessary
        // For simplicity, we'll store the entire XML as a string
        Node leaf = new Node(currentUnid, "Terminal", terminalXML);
        parent.addChild(leaf);
        nodeMap.put(currentUnid, leaf);
        currentUnid++;
        return leaf.getUnid();
    }

    public Node getRoot() {
        return root;
    }
}
