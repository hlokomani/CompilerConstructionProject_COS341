package parser;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class XMLTokenReader {
    public static List<Token> readTokens(String filePath) throws Exception {
        List<Token> tokens = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(filePath));
        NodeList tokenNodes = doc.getElementsByTagName("TOK");

        for (int i = 0; i < tokenNodes.getLength(); i++) {
            Element tokenElement = (Element) tokenNodes.item(i);
            String id = tokenElement.getElementsByTagName("ID").item(0).getTextContent();
            String tokenClass = tokenElement.getElementsByTagName("CLASS").item(0).getTextContent();
            String word = tokenElement.getElementsByTagName("WORD").item(0).getTextContent();
            int line = Integer.parseInt(tokenElement.getElementsByTagName("LINE").item(0).getTextContent());
            int column = Integer.parseInt(tokenElement.getElementsByTagName("COLUMN").item(0).getTextContent());

            tokens.add(new Token(id, tokenClass, word, line, column));
        }

        return tokens;
    }
}
