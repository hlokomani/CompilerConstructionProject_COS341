import java.io.File;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

public class XMLFormatter {
    public static void writeXMLOutput(List<Token> tokens, String outputFile) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("TOKENSTREAM");
        doc.appendChild(rootElement);

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            Element tokElement = doc.createElement("TOK");
            rootElement.appendChild(tokElement);

            Element idElement = doc.createElement("ID");
            idElement.appendChild(doc.createTextNode(String.valueOf(i + 1)));
            tokElement.appendChild(idElement);

            Element classElement = doc.createElement("CLASS");
            classElement.appendChild(doc.createTextNode(token.getTokenClass()));
            tokElement.appendChild(classElement);

            Element wordElement = doc.createElement("WORD");
            wordElement.appendChild(doc.createTextNode(token.getWord()));
            tokElement.appendChild(wordElement);

            Element lineElement = doc.createElement("LINE");
            lineElement.appendChild(doc.createTextNode(String.valueOf(token.getLineNumber())));
            tokElement.appendChild(lineElement);

            Element columnElement = doc.createElement("COLUMN");
            columnElement.appendChild(doc.createTextNode(String.valueOf(token.getColumnNumber())));
            tokElement.appendChild(columnElement);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(outputFile));
        transformer.transform(source, result);
    }
}