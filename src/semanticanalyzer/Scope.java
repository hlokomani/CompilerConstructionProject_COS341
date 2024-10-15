package semanticanalyzer;

public class Scope {
    private String name;
    private Scope parent;

    public Scope(String name, Scope parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Scope getParent() {
        return parent;
    }
}