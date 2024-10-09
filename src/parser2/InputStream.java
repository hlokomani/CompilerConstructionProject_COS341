package parser2;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;


public class InputStream {

    private XMLStreamReader reader;
    private Token nextToken;

    public InputStream(String filePath) throws Exception {
        FileInputStream inputStream = new FileInputStream(filePath);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        reader = factory.createXMLStreamReader(inputStream);
        advance();
    }

    public Token getNextToken() throws XMLStreamException {
        Token current = nextToken;
        advance();
        return current;
    }

    public boolean hasNext() {
        return nextToken != null;
    }

    private void advance() throws XMLStreamException {
        nextToken = null;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("TOK")) {
                nextToken = parseToken();
                break;
            }
        }
    }

    private Token parseToken() throws XMLStreamException {
        int id = 0;
        String clazz = null;
        String word = null;
        int line = 0;
        int column = 0;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String tag = reader.getLocalName();
                reader.next(); // Move to the text content
                String text = reader.getText();
                switch (tag) {
                    case "ID":
                        id = Integer.parseInt(text);
                        break;
                    case "CLASS":
                        clazz = text;
                        break;
                    case "WORD":
                        word = text;
                        break;
                    case "LINE":
                        line = Integer.parseInt(text);
                        break;
                    case "COLUMN":
                        column = Integer.parseInt(text);
                        break;
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("TOK")) {
                break;
            }
        }
        return new Token(id, clazz, word, line, column);
    }

    public void close() throws XMLStreamException {
        if (reader != null) {
            reader.close();
        }
    }

    public static class Token {
        private final int id;
        private final String clazz;
        private final String word;
        private final int line;
        private final int column;

        public Token(int id, String clazz, String word, int line, int column) {
            this.id = id;
            this.clazz = clazz;
            this.word = word;
            this.line = line;
            this.column = column;
        }

        // Getters for each field
        public int getId() {
            return id;
        }

        public String getClazz() {
            return clazz;
        }

        public String getWord() {
            return word;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "id=" + id +
                    ", class='" + clazz + '\'' +
                    ", word='" + word + '\'' +
                    ", line=" + line +
                    ", column=" + column +
                    '}';
        }
    }

    public static void main(String[] args) {
        try {
            InputStream parser = new InputStream("src/lexer/output/output2.xml");
            Token token;
            while ((token = parser.getNextToken()) != null) {
                System.out.println(token);
            }
            parser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
