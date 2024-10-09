package typeChecker;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class TreeCrawler {

    private SyntaxTreeNode root;
    private Map<Integer, SyntaxTreeNode> nodeMap;
    private Stack<StackFrame> traversalStack;

    private static class StackFrame {
        SyntaxTreeNode node;
        int state; // 0: traverse first half, 1: visit node, 2: traverse second half
        int totalChildren;

        StackFrame(SyntaxTreeNode node) {
            this.node = node;
            this.state = 0;
            this.totalChildren = node.getChildren().size();
        }
    }

    public TreeCrawler(String xmlFilePath) throws Exception {
        nodeMap = new HashMap<>();
        parseXML(xmlFilePath);
        initializeTraversal();
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

        // Parse ROOT
        NodeList rootList = doc.getElementsByTagName("ROOT");
        if (rootList.getLength() == 0) {
            throw new Exception("No ROOT element found in the XML.");
        }
        Element rootElement = (Element) rootList.item(0);
        int rootUnid = Integer.parseInt(getTagValue("UNID", rootElement));
        String rootSymb = getTagValue("SYMB", rootElement);
        root = new SyntaxTreeNode(rootUnid, rootSymb);
        nodeMap.put(rootUnid, root);

        // Parse INNERNODES
        NodeList innerNodesList = doc.getElementsByTagName("IN");
        for (int i = 0; i < innerNodesList.getLength(); i++) {
            Element inElement = (Element) innerNodesList.item(i);
            int unid = Integer.parseInt(getTagValue("UNID", inElement));
            String symb = getTagValue("SYMB", inElement);

            SyntaxTreeNode innerNode = new SyntaxTreeNode(unid, symb);
            nodeMap.put(unid, innerNode);
        }

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

        // Link children to parents

        // First, link ROOT children
        Element rootChildrenElement = (Element) rootElement.getElementsByTagName("CHILDREN").item(0);
        NodeList rootChildren = rootChildrenElement.getElementsByTagName("ID");
        for (int i = 0; i < rootChildren.getLength(); i++) {
            int childUnid = Integer.parseInt(rootChildren.item(i).getTextContent());
            SyntaxTreeNode childNode = nodeMap.get(childUnid);
            if (childNode == null) {
                throw new Exception("Child with UNID " + childUnid + " not found.");
            }
            root.addChild(childNode);
        }

        // Link INNER nodes to their parents and their children
        for (int i = 0; i < innerNodesList.getLength(); i++) {
            Element inElement = (Element) innerNodesList.item(i);
            int unid = Integer.parseInt(getTagValue("UNID", inElement));
            SyntaxTreeNode currentNode = nodeMap.get(unid);
            if (currentNode == null) {
                throw new Exception("Inner node with UNID " + unid + " not found.");
            }

            // Get children IDs
            Element childrenElement = (Element) inElement.getElementsByTagName("CHILDREN").item(0);
            NodeList childrenIDs = childrenElement.getElementsByTagName("ID");
            for (int j = 0; j < childrenIDs.getLength(); j++) {
                int childUnid = Integer.parseInt(childrenIDs.item(j).getTextContent());
                SyntaxTreeNode childNode = nodeMap.get(childUnid);
                if (childNode == null) {
                    throw new Exception("Child with UNID " + childUnid + " not found.");
                }
                currentNode.addChild(childNode);
            }

            // Find the PARENT element to link current node to its parent
            int parentUnid = Integer.parseInt(getTagValue("PARENT", inElement));
            SyntaxTreeNode parentNode = nodeMap.get(parentUnid);
            if (parentNode == null) {
                throw new Exception("Parent node with UNID " + parentUnid + " not found for inner node " + unid);
            }
            parentNode.addChild(currentNode);
        }

        // Similarly, link LEAF nodes to their parents
        for (int i = 0; i < leafNodesList.getLength(); i++) {
            Element leafElement = (Element) leafNodesList.item(i);
            int unid = Integer.parseInt(getTagValue("UNID", leafElement));
            int parentUnid = Integer.parseInt(getTagValue("PARENT", leafElement));

            SyntaxTreeNode parentNode = nodeMap.get(parentUnid);
            SyntaxTreeNode leafNode = nodeMap.get(unid);
            if (parentNode == null) {
                throw new Exception("Parent node with UNID " + parentUnid + " not found for leaf node " + unid);
            }
            parentNode.addChild(leafNode);
        }
    }

    private void initializeTraversal() {
        traversalStack = new Stack<>();
        if (root != null) {
            traversalStack.push(new StackFrame(root));
        }
    }


    public SyntaxTreeNode getNext() {
        while (!traversalStack.isEmpty()) {
            StackFrame currentFrame = traversalStack.peek();

            if (currentFrame.state == 0) {
                // Traverse the first half of the children
                int firstHalf = currentFrame.totalChildren / 2;
                for (int i = 0; i < firstHalf; i++) {
                    SyntaxTreeNode child = currentFrame.node.getChildren().get(i);
                    traversalStack.push(new StackFrame(child));
                }
                currentFrame.state = 1; // Move to visiting the node
            } else if (currentFrame.state == 1) {
                // Visit the node
                currentFrame.state = 2; // Move to traversing the second half
                return currentFrame.node;
            } else if (currentFrame.state == 2) {
                // Traverse the second half of the children
                int firstHalf = currentFrame.totalChildren / 2;
                for (int i = firstHalf; i < currentFrame.totalChildren; i++) {
                    SyntaxTreeNode child = currentFrame.node.getChildren().get(i);
                    traversalStack.push(new StackFrame(child));
                }
                traversalStack.pop(); // Finished processing this node
            }
        }
        // Traversal complete
        return null;
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

    public void resetTraversal() {
        initializeTraversal();
    }

    public static void main(String[] args) {
        try {
            // Replace with the path to your XML file
            String xmlFilePath = "src/parser2/output/output2.xml";
            TreeCrawler crawler = new TreeCrawler(xmlFilePath);

            SyntaxTreeNode node;
            while ((node = crawler.getNext()) != null) {
                if (node.getTerminal() != null && !node.getTerminal().isEmpty()) {
                    System.out.println("Leaf Node: " + node);
                } else {
                    System.out.println("Inner Node: " + node);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
