package parser2;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String type;
    private List<Node> children;

    public Node(String type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public String getType() {
        return type;
    }

    public List<Node> getChildren() {
        return children;
    }
}
