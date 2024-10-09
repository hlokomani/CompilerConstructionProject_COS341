package parser2;

import java.util.List;

public class StackElement {
    public int state;
    public Node node;

    public StackElement(int state, Node node) {
        this.state = state;
        this.node = node;
    }
}
