package parser2;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private int unid;
    private String symb;
    private Node parent;
    private List<Node> children;
    private boolean isLeaf;
    private String terminal; // For leaf nodes

    public Node(int unid, String symb) {
        this.unid = unid;
        this.symb = symb;
        this.children = new ArrayList<>();
        this.isLeaf = false;
    }

    // Constructor for leaf nodes
    public Node(int unid, String symb, String terminal) {
        this.unid = unid;
        this.symb = symb;
        this.children = new ArrayList<>();
        this.isLeaf = true;
        this.terminal = terminal;
    }

    public int getUnid() {
        return unid;
    }

    public String getSymb() {
        return symb;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        child.setParent(this);
        this.children.add(child);
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public String getTerminal() {
        return terminal;
    }
}
