package parser2;
import java.util.Stack;

import parser2.InputStream.Token;

public class SLRParser {

    Stack<Integer> stack = new Stack<>();
    Token next; // This would represent the next input symbol
    InputStream inputStream;
    ParseTable parseTable;

    public SLRParser() {
        parseTable = new ParseTable();
    }

    public void parse(String filename) {
        try {
            inputStream = new InputStream(filename);

            stack.push(0);
            next = inputStream.getNextToken();

            while (next != null) {
                int top = stack.peek();
                System.out.println("Top: " + top);
                System.out.println("Printing next: " + next);
                String action = "";
                System.out.println("Next: " + next.getWord() + " " + next.getClazz());
                if (next.getClazz().equals("operator") || next.getClazz().equals("reserved_keyword")
                        || next.getClazz().equals("punctuation")) //Terminals
                {
                    action = parseTable.getAction(top, next.getWord());
                } else if (next.getClazz().equals("V")) {
                    action = parseTable.getAction(top, "TokenV");
                } else if (next.getClazz().equals("F")) {
                    action = parseTable.getAction(top, "TokenF");
                } else if (next.getClazz().equals("N")) {
                    action = parseTable.getAction(top, "TokenN");
                } else if (next.getClazz().equals("T")) {
                    action = parseTable.getAction(top, "TokenT");
                } else if (next.getClazz().equals("end_of_file")) {
                    action = parseTable.getAction(top, "$");
                } else {
                    reportError("Unknown token class: " + next.getClazz());
                    return;
                }

                System.out.println("Action: " + action);

                if (action == null) {
                    reportError("No action found for state " + top + " and symbol " + next.getWord());
                    return;
                }

                if (action.startsWith("s")) {
                    int state = Integer.parseInt(action.substring(1));
                    stack.push(state);
                    next = inputStream.getNextToken();
                } else if (action.startsWith("r")) {
                    String n = parseTable.getLHS(Integer.parseInt(action.substring(1)));
                    System.out.println("N: " + n);
                    int r = parseTable.numSymbolsRHS(Integer.parseInt(action.substring(1)));
                    System.out.println("R: " + r);

                    System.out.println("Top before pop: " + stack.peek());

                    for (int i = 0; i < r; i++) {
                        stack.pop();
                    }

                    int topAfterPop = stack.peek();
                    System.out.println("Top after pop: " + topAfterPop);
                    int goToState = parseTable.getGoto(topAfterPop, n);
                    if (goToState == -1) {
                        reportError("No goto state found for state " + topAfterPop + " and non-terminal " + n);
                        return;
                    }
                    System.out.println("Go to state: " + goToState);
                    stack.push(goToState);
                } else if (action.equals("acc")) {
                    System.out.println("Parsing successful!");
                    return;
                } else {
                    reportError("Unknown action: " + action);
                    return;
                }
            }
            System.out.println("Parsing successful!");

        } catch (Exception e) {
            System.out.println("Error occurred in catch: " + e.getMessage());
        }
    }

    private void reportError(String message) {
        System.err.println("Parsing error occurred: " + message);
    }

    public static void main(String[] args) {
        SLRParser parser = new SLRParser();
        parser.parse("src/lexer/output/output3.xml");
    }
}
