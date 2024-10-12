package parser2;

import java.util.ArrayList;
import java.util.List;

public class SyntaxTreeNode {
    private int unid;
    private String symb;
    private SyntaxTreeNode parent;
    private List<SyntaxTreeNode> children;
    private boolean isLeaf;
    private boolean isRoot;
    private String terminal;

    public SyntaxTreeNode(int unid, String symb) {
        this.unid = unid;
        this.symb = symb;
        this.children = new ArrayList<>();
        this.isLeaf = true;
        this.isRoot = true;
    }

    // Constructor for leaf nodes
    public SyntaxTreeNode(int unid, String symb, String terminal) {
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

    public SyntaxTreeNode getParent() {
        return parent;
    }

    public void setParent(SyntaxTreeNode parent) {
        this.parent = parent;
        this.isRoot = false;
    }

    public List<SyntaxTreeNode> getChildren() {
        return children;
    }

    public void addChild(SyntaxTreeNode child) {
        child.setParent(this);
        this.children.add(child);
        this.isLeaf = false;
    }

    public void addChildren(List<SyntaxTreeNode> children) {
        for (SyntaxTreeNode child : children) {
            addChild(child);
        }
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public String getTerminal() {
        return terminal;
    }
}
