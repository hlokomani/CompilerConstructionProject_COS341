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
    private SyntaxTreeNode root;
    private Document doc;
    private Map<Integer, SyntaxTreeNode> innerNodes;
    private List<SyntaxTreeNode> leafNodes;

    public XMLGenerator(SyntaxTreeNode root) throws ParserConfigurationException {
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

    private void traverseAndCategorize(SyntaxTreeNode node) {
        if (node.isLeaf()) {
            leafNodes.add(node);
        } else {
            if (node != root) {
                innerNodes.put(node.getUnid(), node);
            }
            for (SyntaxTreeNode child : node.getChildren()) {
                traverseAndCategorize(child);
            }
        }
    }

    public void convertToXML(String filePath) throws TransformerException {
        Element syntaxTreeElement = doc.getDocumentElement();
        //System.out.println("checkpoint 1");
        // ROOT Element
        Element rootElement = doc.createElement("ROOT");
        syntaxTreeElement.appendChild(rootElement);
        //System.out.println("checkpoint 2");

        // ROOT UNID
        Element rootUnid = doc.createElement("UNID");
        rootUnid.appendChild(doc.createTextNode(String.valueOf(root.getUnid())));
        rootElement.appendChild(rootUnid);
        //System.out.println("checkpoint 3");

        // ROOT SYMB
        Element rootSymb = doc.createElement("SYMB");
        rootSymb.appendChild(doc.createTextNode(root.getSymb()));
        rootElement.appendChild(rootSymb);
        //System.out.println("checkpoint 4");

        // ROOT CHILDREN
        Element rootChildren = doc.createElement("CHILDREN");
        for (SyntaxTreeNode child : root.getChildren()) {
            Element childId = doc.createElement("ID");
            childId.appendChild(doc.createTextNode(String.valueOf(child.getUnid())));
            rootChildren.appendChild(childId);
        }
        rootElement.appendChild(rootChildren);
        //System.out.println("checkpoint 5");

        // INNERNODES
        Element innerNodesElement = doc.createElement("INNERNODES");
        syntaxTreeElement.appendChild(innerNodesElement);
        for (SyntaxTreeNode innerNode : innerNodes.values()) {
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
            for (SyntaxTreeNode child : innerNode.getChildren()) {
                Element childId = doc.createElement("ID");
                childId.appendChild(doc.createTextNode(String.valueOf(child.getUnid())));
                childrenElement.appendChild(childId);
            }
            inElement.appendChild(childrenElement);

            innerNodesElement.appendChild(inElement);
        }
        //System.out.println("checkpoint 6");

        // LEAFNODES
        Element leafNodesElement = doc.createElement("LEAFNODES");
        syntaxTreeElement.appendChild(leafNodesElement);
        //System.out.println("checkpoint 6.1");
        for (SyntaxTreeNode leaf : leafNodes) {
            Element leafElement = doc.createElement("LEAF");
            //System.out.println("checkpoint 6.2");
            // PARENT
            Element parentElement = doc.createElement("PARENT");
            parentElement.appendChild(doc.createTextNode(String.valueOf(leaf.getParent().getUnid())));
            leafElement.appendChild(parentElement);

            //System.out.println("checkpoint 6.3");
            // UNID
            Element unidElement = doc.createElement("UNID");
            unidElement.appendChild(doc.createTextNode(String.valueOf(leaf.getUnid())));
            leafElement.appendChild(unidElement);

            //System.out.println("checkpoint 6.4");
            // TERMINAL
            Element terminalElement = doc.createElement("TERMINAL");
            // Assuming terminalXML is a well-formed XML string
            if (leaf.getTerminal() != null){
                try {
                    //System.out.println("checkpoint 6.5");
                    //System.out.println("leaf.getTerminal() = " + leaf.getTerminal());
                    Document terminalDoc = parseXMLString(leaf.getTerminal());
                    //System.out.println("checkpoint 6.6");
                    org.w3c.dom.Node importedTerminal = doc.importNode(terminalDoc.getDocumentElement(), true);
                    //System.out.println("checkpoint 6.7");
                    terminalElement.appendChild(importedTerminal);
                } catch (Exception e) {
                    //System.out.println("checkpoint 6.8");
                    // If terminalXML is not well-formed, store it as text
                    terminalElement.appendChild(doc.createTextNode(leaf.getTerminal()));
                }
            }

            leafElement.appendChild(terminalElement);

            leafNodesElement.appendChild(leafElement);
        }
        //System.out.println("checkpoint 7");

        // Write the content into XML file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        //System.out.println("checkpoint 8");
        // For pretty print
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        //System.out.println("checkpoint 8");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        //System.out.println("checkpoint 10");
        transformer.transform(source, result);
        //System.out.println("checkpoint 11");
    }

    private Document parseXMLString(String xmlStr) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlStr)));
    }
}
