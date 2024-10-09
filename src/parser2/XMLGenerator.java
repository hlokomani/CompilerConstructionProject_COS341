package parser2;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;

public class XMLGenerator{
    private Node root;
    private Document doc;
    private Map<Integer, Node> innerNodes;
    private List<Node> leafNodes;

    public XMLGenerator(Node root) throws ParserConfigurationException {
        this.root = root;
        this.innerNodes = new HashMap<>();
        this.leafNodes = new ArrayList<>();
        initializeDocument();
        categorizeNodes();
    }

    private void initializeDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();
        Element syntaxTreeElement = doc.createElement("SYNTREE");
        doc.appendChild(syntaxTreeElement);
    }

    private void categorizeNodes() {
        // Traverse the tree to categorize inner and leaf nodes
        traverseAndCategorize(root);
    }

    private void traverseAndCategorize(Node node) {
        if (node.isLeaf()) {
            leafNodes.add(node);
        } else {
            if (node != root) {
                innerNodes.put(node.getUnid(), node);
            }
            for (Node child : node.getChildren()) {
                traverseAndCategorize(child);
            }
        }
    }

    public void convertToXML(String filePath) throws TransformerException {
        Element syntaxTreeElement = doc.getDocumentElement();

        // ROOT Element
        Element rootElement = doc.createElement("ROOT");
        syntaxTreeElement.appendChild(rootElement);

        // ROOT UNID
        Element rootUnid = doc.createElement("UNID");
        rootUnid.appendChild(doc.createTextNode(String.valueOf(root.getUnid())));
        rootElement.appendChild(rootUnid);

        // ROOT SYMB
        Element rootSymb = doc.createElement("SYMB");
        rootSymb.appendChild(doc.createTextNode(root.getSymb()));
        rootElement.appendChild(rootSymb);

        // ROOT CHILDREN
        Element rootChildren = doc.createElement("CHILDREN");
        for (Node child : root.getChildren()) {
            Element childId = doc.createElement("ID");
            childId.appendChild(doc.createTextNode(String.valueOf(child.getUnid())));
            rootChildren.appendChild(childId);
        }
        rootElement.appendChild(rootChildren);

        // INNERNODES
        Element innerNodesElement = doc.createElement("INNERNODES");
        syntaxTreeElement.appendChild(innerNodesElement);
        for (Node innerNode : innerNodes.values()) {
            Element inElement = doc.createElement("IN");

            // PARENT
            Element parentElement = doc.createElement("PARENT");
            parentElement.appendChild(doc.createTextNode(String.valueOf(innerNode.getParent().getUnid())));
            inElement.appendChild(parentElement);

            // UNID
            Element unidElement = doc.createElement("UNID");
            unidElement.appendChild(doc.createTextNode(String.valueOf(innerNode.getUnid())));
            inElement.appendChild(unidElement);

            // SYMB
            Element symbElement = doc.createElement("SYMB");
            symbElement.appendChild(doc.createTextNode(innerNode.getSymb()));
            inElement.appendChild(symbElement);

            // CHILDREN
            Element childrenElement = doc.createElement("CHILDREN");
            for (Node child : innerNode.getChildren()) {
                Element childId = doc.createElement("ID");
                childId.appendChild(doc.createTextNode(String.valueOf(child.getUnid())));
                childrenElement.appendChild(childId);
            }
            inElement.appendChild(childrenElement);

            innerNodesElement.appendChild(inElement);
        }

        // LEAFNODES
        Element leafNodesElement = doc.createElement("LEAFNODES");
        syntaxTreeElement.appendChild(leafNodesElement);
        for (Node leaf : leafNodes) {
            Element leafElement = doc.createElement("LEAF");

            // PARENT
            Element parentElement = doc.createElement("PARENT");
            parentElement.appendChild(doc.createTextNode(String.valueOf(leaf.getParent().getUnid())));
            leafElement.appendChild(parentElement);

            // UNID
            Element unidElement = doc.createElement("UNID");
            unidElement.appendChild(doc.createTextNode(String.valueOf(leaf.getUnid())));
            leafElement.appendChild(unidElement);

            // TERMINAL
            Element terminalElement = doc.createElement("TERMINAL");
            // Assuming terminalXML is a well-formed XML string
            try {
                Document terminalDoc = parseXMLString(leaf.getTerminal());
                org.w3c.dom.Node importedTerminal = doc.importNode(terminalDoc.getDocumentElement(), true);
                terminalElement.appendChild(importedTerminal);
            } catch (Exception e) {
                // If terminalXML is not well-formed, store it as text
                terminalElement.appendChild(doc.createTextNode(leaf.getTerminal()));
            }
            leafElement.appendChild(terminalElement);

            leafNodesElement.appendChild(leafElement);
        }

        // Write the content into XML file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        // For pretty print
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));

        transformer.transform(source, result);
    }

    private Document parseXMLString(String xmlStr) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlStr)));
    }
}
