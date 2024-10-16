package semanticanalyzer;

public class Symbol {
    private String name;
    private String kind;
    private String type;
    private int nodeId;

    public Symbol(String name, String kind, String type, int nodeId) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getKind() {
        return kind;
    }

    public int getNodeId() {
        return nodeId;
    }
}