package parser2;

import java.util.List;

public class StackElement {
    public int state;
    public SyntaxTreeNode node;

    public StackElement(int state, SyntaxTreeNode node) {
        this.state = state;
        this.node = node;
    }
}
