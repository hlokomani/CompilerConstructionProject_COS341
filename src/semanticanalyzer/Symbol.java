package semanticanalyzer;

public class Symbol {
    private String name;
    private String kind;
    private int nodeId;

    public Symbol(String name, String kind, int nodeId) {
        this.name = name;
        this.kind = kind;
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public int getNodeId() {
        return nodeId;
    }
}