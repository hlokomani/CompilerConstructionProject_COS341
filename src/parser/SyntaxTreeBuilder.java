package parser;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;

public class SyntaxTreeBuilder {
    private Document doc;
    private Element root;
    private Element innerNodes;
    private Element leafNodes;
    private int unidCounter;

    public SyntaxTreeBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.newDocument();
        root = doc.createElement("SYNTREE");
        doc.appendChild(root);
        innerNodes = doc.createElement("INNERNODES");
        leafNodes = doc.createElement("LEAFNODES");
        root.appendChild(innerNodes);
        root.appendChild(leafNodes);
        unidCounter = 1;
    }

    public void buildTree(TreeNode node) throws Exception {
        if (node == null) {
            throw new Exception("Root node is null");
        }
        try {
            Element rootElement = createInnerNode(node, null);
            root.insertBefore(rootElement, innerNodes);
            buildTreeRecursive(node, rootElement);
        } catch (Exception e) {
            throw new Exception("Error building syntax tree: " + e.getMessage(), e);
        }
    }

    private void buildTreeRecursive(TreeNode node, Element parent) throws Exception {
        for (TreeNode child : node.getChildren()) {
            Element childElement;
            try {
                if (child instanceof LeafNode) {
                    childElement = createLeafNode((LeafNode) child, parent);
                    leafNodes.appendChild(childElement);
                } else {
                    childElement = createInnerNode(child, parent);
                    innerNodes.appendChild(childElement);
                    buildTreeRecursive(child, childElement);
                }
                addChildReference(parent, childElement);
            } catch (Exception e) {
                throw new Exception("Error processing node " + child.getType() + ": " + e.getMessage(), e);
            }
        }
    }

    private Element createInnerNode(TreeNode node, Element parent) {
        Element element = doc.createElement(node == root.getFirstChild() ? "ROOT" : "IN");
        element.appendChild(createUNID());
        element.appendChild(createSymbol(node.getType()));
        if (parent != null) {
            element.appendChild(createParentReference(parent));
        }
        element.appendChild(doc.createElement("CHILDREN"));
        return element;
    }

    private Element createLeafNode(LeafNode node, Element parent) throws Exception {
        Element element = doc.createElement("LEAF");
        element.appendChild(createUNID());
        element.appendChild(createParentReference(parent));
        Element terminal = doc.createElement("TERMINAL");
        terminal.appendChild(createTokenElement(node.getToken()));
        element.appendChild(terminal);
        return element;
    }

    private Element createUNID() {
        Element unid = doc.createElement("UNID");
        unid.setTextContent(String.valueOf(unidCounter++));
        return unid;
    }

    private Element createSymbol(String symbol) {
        Element symb = doc.createElement("SYMB");
        symb.setTextContent(symbol);
        return symb;
    }

    private Element createParentReference(Element parent) {
        Element parentRef = doc.createElement("PARENT");
        parentRef.setTextContent(parent.getElementsByTagName("UNID").item(0).getTextContent());
        return parentRef;
    }

    private void addChildReference(Element parent, Element child) {
        Element children = (Element) parent.getElementsByTagName("CHILDREN").item(0);
        Element childId = doc.createElement("ID");
        childId.setTextContent(child.getElementsByTagName("UNID").item(0).getTextContent());
        children.appendChild(childId);
    }

    private Element createTokenElement(Token token) {
        Element tokenElement = doc.createElement("TOK");

        Element id = doc.createElement("ID");
        id.setTextContent(token.getId());
        tokenElement.appendChild(id);

        Element tokenClass = doc.createElement("CLASS");
        tokenClass.setTextContent(token.getTokenClass());
        tokenElement.appendChild(tokenClass);

        Element word = doc.createElement("WORD");
        word.setTextContent(token.getWord());
        tokenElement.appendChild(word);

        Element line = doc.createElement("LINE");
        line.setTextContent(String.valueOf(token.getLine()));
        tokenElement.appendChild(line);

        Element column = doc.createElement("COLUMN");
        column.setTextContent(String.valueOf(token.getColumn()));
        tokenElement.appendChild(column);

        return tokenElement;
    }

    private Element findParent(TreeNode node) throws Exception {
        NodeList innerNodesList = innerNodes.getElementsByTagName("IN");
        for (int i = 0; i < innerNodesList.getLength(); i++) {
            Element innerNode = (Element) innerNodesList.item(i);
            NodeList children = innerNode.getElementsByTagName("CHILDREN").item(0).getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                if (children.item(j).getTextContent().equals(node.getType())) {
                    return innerNode;
                }
            }
        }
        throw new Exception("Parent node not found for " + node.getType());
    }

    public void writeToFile(String fileName) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));
        transformer.transform(source, result);
    }
}
