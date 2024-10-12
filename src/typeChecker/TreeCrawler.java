package typeChecker;

import org.w3c.dom.*;

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
        inOrderTraversal(root, traversedTree);
    }

    public static void inOrderTraversal(SyntaxTreeNode node, List<SyntaxTreeNode> result) {
        if (node == null) {
            return;
        }

        // If there are children, we assume the first child is the "left" subtree
        if (!node.getChildren().isEmpty()) {
            inOrderTraversal(node.getChildren().get(0), result);  // Visit the first child (left subtree)
        }

        // Add the current node to the result list
        result.add(node);

        // If there are more children, visit the rest (right subtrees)
        for (int i = 1; i < node.getChildren().size(); i++) {
            inOrderTraversal(node.getChildren().get(i), result);
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
                // Serialize the TERMINAL element to a string
                terminalXML = elementToString(terminalElement.getFirstChild());
            }

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

    private String elementToString(Node node) {
        StringBuilder sb = new StringBuilder();
        serializeNode(node, sb, 0);
        return sb.toString();
    }

    private void serializeNode(Node node, StringBuilder sb, int level) {
        // Indentation
        for (int i = 0; i < level; i++) {
            sb.append("    ");
        }

        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getTextContent().trim();
            if (!text.isEmpty()) {
                sb.append(text).append("\n");
            }
            return;
        }

        sb.append("<").append(node.getNodeName()).append(">");
        NodeList children = node.getChildNodes();
        boolean hasElementChildren = false;

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                hasElementChildren = true;
                sb.append("\n");
                serializeNode(children.item(i), sb, level + 1);
            }
        }

        if (hasElementChildren) {
            for (int i = 0; i < level; i++) {
                sb.append("    ");
            }
        }
        sb.append("</").append(node.getNodeName()).append(">").append("\n");
    }

    public SyntaxTreeNode getNext() {
        int temp = currentIndex;
        if (currentIndex < traversedTree.size()) {
            currentIndex++;
            return traversedTree.get(temp);
        }
        return null;
    }

    public static void main(String[] args) {
       // Crawling a tree and printing the nodes
        try {
            TreeCrawler treeCrawler = new TreeCrawler("src/parser2/output/output2.xml");
            SyntaxTreeNode node;
            while ((node = treeCrawler.getNext()) != null) {
                System.out.println(node.getUnid() + " " + node.getSymb());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
