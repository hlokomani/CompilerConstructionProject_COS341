package semanticanalyzer;

public class Symbol {
    private String name;
    private String kind;
    private String type;
    private int unid;
    private String generatedName;

    public Symbol(String name, String kind, String type, int unid) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.unid = unid;
        this.generatedName = generateBasicName();
    }

    private String generateBasicName() {
        if (type.equals("text")) {
            return NameGenerator.getInstance().getNextTextName();
        } else {
            return NameGenerator.getInstance().getNextNumName();
        }
    }

    public String getName() {
        return name;
    }

    public String getGeneratedName() {
        return generatedName;
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
        if (kind.equals("function")) {
            return "Symbol{" +
                    "name='" + name + '\'' +
                    ", kind='" + kind + '\'' +
                    ", type='" + type + '\'' +
                    ", unid=" + unid +
                    '}';
        }
        return "Symbol{" +
                "name='" + name + '\'' +
                ", generatedName='" + generatedName + '\'' +
                ", kind='" + kind + '\'' +
                ", type='" + type + '\'' +
                ", unid=" + unid +
                '}';
    }
}