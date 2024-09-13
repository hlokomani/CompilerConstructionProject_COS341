package parser;

public class Token {
    private String id;
    private String tokenClass;
    private String word;
    private int line;
    private int column;

    public Token(String id, String tokenClass, String word, int line, int column) {
        this.id = id;
        this.tokenClass = tokenClass;
        this.word = word;
        this.line = line;
        this.column = column;
    }

    public String getId() { return id; }
    public String getTokenClass() { return tokenClass; }
    public String getWord() { return word; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
}
