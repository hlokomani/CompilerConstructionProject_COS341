package typeChecker;

import org.w3c.dom.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import parser2.SyntaxTreeNode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class TreeCrawler {
    private Map<Integer, SyntaxTreeNode> nodeMap = new HashMap<>();
    private SyntaxTreeNode root;
    private List<SyntaxTreeNode> traversedTree = new ArrayList<>();
    private int currentIndex;

    public TreeCrawler(String xmlFilePath) throws Exception {
        parseXML(xmlFilePath);
        preOrderTraversal(root, traversedTree);
    }

    public static void preOrderTraversal(SyntaxTreeNode node, List<SyntaxTreeNode> result) {
        if (node == null) {
            return;
        }

        // Add the current node to the result list (visit the current node first)
        result.add(node);

        // Visit all children in order (preorder, so visit before going deeper)
        for (SyntaxTreeNode child : node.getChildren()) {
            preOrderTraversal(child, result);
        }
    }

    private void parseXML(String xmlFilePath) throws Exception {
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.exists()) {
            throw new Exception("XML file not found at: " + xmlFilePath);
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);

        doc.getDocumentElement().normalize();

        // Starting with the children so that the tree can be built while parsing

        // Parse LEAFNODES
        NodeList leafNodesList = doc.getElementsByTagName("LEAF");
        for (int i = 0; i < leafNodesList.getLength(); i++) {
            Element leafElement = (Element) leafNodesList.item(i);
            int unid = Integer.parseInt(getTagValue("UNID", leafElement));
            Element terminalElement = (Element) leafElement.getElementsByTagName("TERMINAL").item(0);

            String terminalXML = "";
            if (terminalElement.hasChildNodes()) {
                terminalXML = elementToString(terminalElement);
            }
//            System.out.println("Terminal XML: " + terminalXML);

            SyntaxTreeNode leafNode = new SyntaxTreeNode(unid, "Terminal", terminalXML);
            nodeMap.put(unid, leafNode);
        }

        // Parse INNERNODES
        NodeList innerNodesList = doc.getElementsByTagName("IN");
        for (int i = 0; i < innerNodesList.getLength(); i++) {
            Element inElement = (Element) innerNodesList.item(i);
            int unid = Integer.parseInt(getTagValue("UNID", inElement));
            String symb = getTagValue("SYMB", inElement);
            SyntaxTreeNode innerNode = new SyntaxTreeNode(unid, symb);
            nodeMap.put(unid, innerNode);
        }

        // Adding the children for each inner node
        for (int i = 0; i < innerNodesList.getLength(); i++) {
            Element inElement = (Element) innerNodesList.item(i);
            int unid = Integer.parseInt(getTagValue("UNID", inElement));
            SyntaxTreeNode innerNode = nodeMap.get(unid);

            NodeList childrenList = inElement.getElementsByTagName("CHILDREN");
            if (childrenList.getLength() == 0) {
                throw new Exception("No CHILDREN element found in the XML.");
            }
            Element childrenElement = (Element) childrenList.item(0);
            NodeList childList = childrenElement.getElementsByTagName("ID");         

            for (int j = childList.getLength() - 1; j >= 0; j--) {
                int childUnid = Integer.parseInt(childList.item(j).getTextContent());
                innerNode.addChild(nodeMap.get(childUnid));
                //adding the parent to the child
                nodeMap.get(childUnid).setParent(innerNode);
            }
        }

        // Parse ROOT
        NodeList rootList = doc.getElementsByTagName("ROOT");
        if (rootList.getLength() == 0) {
            throw new Exception("No ROOT element found in the XML.");
        }
        Element rootElement = (Element) rootList.item(0);
        // Converting to a SyntaxTreeNode
        int rootUnid = Integer.parseInt(getTagValue("UNID", rootElement));
        String rootSymb = getTagValue("SYMB", rootElement);
        root = new SyntaxTreeNode(rootUnid, rootSymb);
        nodeMap.put(rootUnid, root);
        // Adding the children for the root node
        NodeList childrenList = rootElement.getElementsByTagName("CHILDREN");
        if (childrenList.getLength() == 0) {
            throw new Exception("No CHILDREN element found in the XML.");
        }
        Element childrenElement = (Element) childrenList.item(0);
        NodeList childList = childrenElement.getElementsByTagName("ID");
        for (int j = childList.getLength()-1; j >=0 ; j--) {
            int childUnid = Integer.parseInt(childList.item(j).getTextContent());
            root.addChild(nodeMap.get(childUnid));
            //adding the parent to the child
            nodeMap.get(childUnid).setParent(root);
        }              
    }


    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent();
        }
        return null;
    }

    public static String elementToString(Element element) {
        try {
            // Set up a transformer to convert the element to a string
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // Convert the element to a string
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));

            return writer.getBuffer().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SyntaxTreeNode getNext() {
        int temp = currentIndex;
        if (currentIndex < traversedTree.size()) {
            currentIndex++;
            return traversedTree.get(temp);
        }
        return null;
    }

    public SyntaxTreeNode getFnameForReturnCommand(SyntaxTreeNode command) {
        //going up the tree to find the function name
        SyntaxTreeNode temp = command;
        while (temp != null) {
            if (temp.getSymb() == "FNAME") {
                return temp;
            }
            temp = temp.getParent();
        }
        return null;
    }
    
    public SyntaxTreeNode peakNext() {
        if (currentIndex < traversedTree.size()) {
            return traversedTree.get(currentIndex);
        }
        return null;
    }

    public SyntaxTreeNode peakNextIndex(int index) {
        if ((currentIndex + index) < traversedTree.size()) {
            return traversedTree.get(currentIndex + index);
        }
        return null;
    }

    // public static void main(String[] args) {
    //    // Crawling a tree and printing the nodes
    //     try {
    //         TreeCrawler treeCrawler = new TreeCrawler("src/parser2/output/output3.xml");
    //         SyntaxTreeNode node;
    //         while ((node = treeCrawler.getNext()) != null) {
    //             if(node.getSymb() == "Terminal")
    //                 System.out.println(node.getUnid() + " " + node.getTerminal());
    //             else
    //                 System.out.println(node.getUnid() + " " + node.getSymb());
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
}
