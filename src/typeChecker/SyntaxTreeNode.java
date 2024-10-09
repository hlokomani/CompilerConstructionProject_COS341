package typeChecker;

import java.util.ArrayList;
import java.util.List;

public class SyntaxTreeNode {
    private int unid; // Unique Node ID
    private String symb; // Symbol (Terminal or Non-terminal)
    private SyntaxTreeNode parent; // Parent node
    private List<SyntaxTreeNode> children; // List of child nodes
    private String terminal; // For leaf nodes, stores terminal XML

    public SyntaxTreeNode(int unid, String symb) {
        this.unid = unid;
        this.symb = symb;
        this.children = new ArrayList<>();
    }

    // Constructor for leaf nodes with terminal information
    public SyntaxTreeNode(int unid, String symb, String terminal) {
        this.unid = unid;
        this.symb = symb;
        this.children = new ArrayList<>();
        this.terminal = terminal;
    }

    public int getUnid() {
        return unid;
    }

    public String getSymb() {
        return symb;
    }

    public SyntaxTreeNode getParent() {
        return parent;
    }

    public void setParent(SyntaxTreeNode parent) {
        this.parent = parent;
    }

    public List<SyntaxTreeNode> getChildren() {
        return children;
    }

    public void addChild(SyntaxTreeNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public String getTerminal() {
        return terminal;
    }

    @Override
    public String toString() {
        return "SyntaxTreeNode{" +
                "unid=" + unid +
                ", symb='" + symb + '\'' +
                (terminal != null ? ", terminal='" + terminal + '\'' : "") +
                '}';
    }
}
