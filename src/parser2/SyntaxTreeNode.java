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
        this.terminal = sanitizesTerminal(terminal);
    }

    private String sanitizesTerminal(String terminal) {
        //finding special character in between the terminal
        //finding the contents between <WORD> and </WORD>
        //System.out.println("In the sanitizesTerminal");
        if (terminal.contains("<WORD>") && terminal.contains("</WORD>")) {
            int start = terminal.indexOf("<WORD>") + 6;
            int end = terminal.indexOf("</WORD>");
            String word = terminal.substring(start, end);
            //System.out.println("word: " + word);
            if (word.contains(">")) {
                //System.out.println("In if 1");
                word = "&gt;";
            }
            if (word.contains("<")) {
                //System.out.println("In if 2");
                word = "&lt;";
            }
            if (word.contains("&")) {
                //System.out.println("In if 3");
                word="&amp;";
            }                  
            //replacing the contents between <WORD> and </WORD> with the sanitized word
            //System.out.println("Sanitized word: " + word);
            terminal = terminal.substring(0, start) + word + terminal.substring(end);
            //System.out.println("The sanitized terminal: " + terminal);
        }
        return terminal;
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
