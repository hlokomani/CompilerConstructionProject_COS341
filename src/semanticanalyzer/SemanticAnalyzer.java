package semanticanalyzer;

import parser2.SyntaxTreeNode;
import typeChecker.TreeCrawler;

import java.util.Stack;

public class SemanticAnalyzer {
    private SymbolTable symbolTable;
    private TreeCrawler treeCrawler;
    private SyntaxTreeNode currentNode;
    private Stack<String> scopeStack;
    private boolean inGlobalVars = false;

    public SemanticAnalyzer(String xmlFilePath) throws Exception {
        this.treeCrawler = new TreeCrawler(xmlFilePath);
        this.symbolTable = SymbolTable.getInstance();
        this.symbolTable.clear();
        this.scopeStack = new Stack<>();
    }

    public void analyze() throws SemanticException {
        scopeStack.push("global");

        while ((currentNode = treeCrawler.getNext()) != null) {
//            System.out.println("Processing node: " + currentNode.getSymb() + " - " + currentNode.getTerminal());
            switch (currentNode.getSymb()) {
                case "PROG":
                    handleProgramNode();
                    break;
                case "GLOBVARS":
                    handleGlobalVarsNode();
                    break;
                case "VNAME":
                    handleVariableNode();
                    break;
                case "ASSIGN":
                    handleAssignmentNode();
                    break;
                case "ALGO":
                    inGlobalVars = false;
                    break;
            }
        }
    }

    private void handleProgramNode() throws SemanticException {
        symbolTable.enterScope("main");
        scopeStack.push("main");
    }

    private void handleGlobalVarsNode() throws SemanticException {
        inGlobalVars = true;
    }

    private void handleVariableNode() throws SemanticException {
        if (inGlobalVars) {
            String variableName = extractVariableName(currentNode);
            if (variableName == null || variableName.isEmpty()) {
                throw new SemanticException("Invalid variable name at node: " + currentNode.getUnid());
            }
            if (symbolTable.lookupVariable(variableName, true) != null) {
                throw new SemanticException("Variable " + variableName + " is already defined in this scope");
            }
            symbolTable.addSymbol(new Symbol(variableName, "variable", currentNode.getUnid()));
        }
    }

    private void handleAssignmentNode() throws SemanticException {
        SyntaxTreeNode varNode = currentNode.getChildren().get(0);
        String variableName = extractVariableName(varNode);
        if (variableName == null || variableName.isEmpty()) {
            throw new SemanticException("Invalid variable in assignment at node: " + currentNode.getUnid());
        }
        if (symbolTable.lookupVariable(variableName, false) == null) {
            throw new SemanticException("Variable " + variableName + " is not declared before use");
        }
    }

    private String extractVariableName(SyntaxTreeNode node) {
        if (node.getSymb().equals("VNAME") && node.getChildren().size() > 0) {
            SyntaxTreeNode terminalNode = node.getChildren().get(0);
            if (terminalNode.getSymb().equals("Terminal")) {
                String terminal = terminalNode.getTerminal();
                if (terminal != null && terminal.contains("<WORD>")) {
                    int start = terminal.indexOf("<WORD>") + 6;
                    int end = terminal.indexOf("</WORD>");
                    return terminal.substring(start, end);
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            SemanticAnalyzer analyzer = new SemanticAnalyzer("src/parser2/output/output2.xml");
            analyzer.analyze();
            System.out.println("Semantic analysis completed successfully.");
            SymbolTable.getInstance().getAllSymbols().forEach((key, symbols) -> {
                symbols.forEach(symbol ->
                        System.out.println(key + ": " + symbol.getName() + " (" + symbol.getKind() + ")"));
            });
        } catch (Exception e) {
            System.err.println("Semantic analysis failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}