package semanticanalyzer;

public class Symbol {
    private String name;
    private String kind;
    private String type;
    private int unid;

    public Symbol(String name, String kind, String type, int unid) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.unid = unid;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public int getUnid() {
        return unid;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "name='" + name + '\'' +
                ", kind='" + kind + '\'' +
                ", type='" + type + '\'' +
                ", unid=" + unid +
                '}';
    }
}