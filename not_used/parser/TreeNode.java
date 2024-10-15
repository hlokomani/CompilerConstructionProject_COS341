package parser;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String type;
    private List<TreeNode> children;

    public TreeNode(String type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }

    public String getType() {
        return type;
    }

    public List<TreeNode> getChildren() {
        return children;
    }
}
