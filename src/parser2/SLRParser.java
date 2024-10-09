package parser2;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import parser2.InputStream.Token;

public class SLRParser {

    Stack<StackElement> stack = new Stack<>();
    Token next; // This would represent the next input symbol
    InputStream inputStream;
    ParseTable parseTable;

    public SLRParser() {
        parseTable = new ParseTable();
    }

    public void parse(String filename) {
        try {
            int idCounter = 0;
            inputStream = new InputStream(filename);
            next = inputStream.getNextToken();
            StackElement elm = new StackElement(0, new Node(idCounter++, next.getWord()));
            stack.push(elm);
            String action = "";

            while (true) {
                int state = stack.peek().state;
                if (next.getClazz().equals("operator") || next.getClazz().equals("reserved_keyword")
                        || next.getClazz().equals("punctuation")) //Terminals
                {
                    action = parseTable.getAction(state, next.getWord());
                } else if (next.getClazz().equals("V")) {
                    action = parseTable.getAction(state, "TokenV");
                } else if (next.getClazz().equals("F")) {
                    action = parseTable.getAction(state, "TokenF");
                } else if (next.getClazz().equals("N")) {
                    action = parseTable.getAction(state, "TokenN");
                } else if (next.getClazz().equals("T")) {
                    action = parseTable.getAction(state, "TokenT");
                } else if (next.getClazz().equals("END")) {
                    action = parseTable.getAction(state, "$");
                } else {
                    reportError("Unknown token class: " + next.getClazz());
                    return;
                }

                if (action == null) {
                    reportError("No action found for state " + state + " and symbol " + next.getWord());
                    return;
                }

                if (action.startsWith("s")) {
                    int newState = Integer.parseInt(action.substring(1));
                    elm = new StackElement(newState, new Node(idCounter++, next.getWord(), next.toString()));
                    stack.push(elm);
                    next = inputStream.getNextToken();
                } else if (action.startsWith("r")) {
                    String n = parseTable.getLHS(Integer.parseInt(action.substring(1)));
                    int r = parseTable.numSymbolsRHS(Integer.parseInt(action.substring(1)));

                    List<Node> children = new ArrayList<>();
                    boolean popped = false;

                    for (int i = 0; i < r; i++) {
                        children.add(stack.pop().node);
                        popped = true;
                    }
                    if (!popped) {
                        children.add(new Node(idCounter++, "EPSILON"));
                    }

                    int stateAfterPop = stack.peek().state;
                    int goToState = parseTable.getGoto(stateAfterPop, n);
                    if (goToState == -1) {
                        reportError("No goto state found for state " + stateAfterPop + " and non-terminal " + n);
                        return;
                    }
                    Node newNode = new Node(idCounter++, n);
                    newNode.addChildren(children);
                    elm = new StackElement(goToState, newNode);
                    stack.push(elm);
                } else if (action.equals("acc")) {
                    System.out.println("Parsing successful!");
                    String newFileName = filename.replace("src/lexer", "src/parser2");
                    new XMLGenerator(stack.pop().node).convertToXML(newFileName);
                    return;
                } else {
                    reportError("Unknown action: " + action);
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred in catch: " + e.getMessage());
        }
    }

    private void reportError(String message) {
        System.err.println("Parsing error occurred: " + message);
    }

    public static void main(String[] args) {
        SLRParser parser = new SLRParser();
        parser.parse("src/lexer/output/output2.xml");
    }
}
