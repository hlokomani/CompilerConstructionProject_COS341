public class Token {
    private final String tokenClass;
    private final String word;
    private final int lineNumber;
    private final int columnNumber;

    public Token(String tokenClass, String word, int lineNumber, int columnNumber) {
        this.tokenClass = tokenClass;
        this.word = word;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public String getTokenClass() {
        return tokenClass;
    }

    public String getWord() {
        return word;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}