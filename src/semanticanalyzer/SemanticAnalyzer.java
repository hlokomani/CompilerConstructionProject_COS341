package semanticanalyzer;

import parser2.SyntaxTreeNode;
import typeChecker.TreeCrawler;

import java.util.*;

public class SemanticAnalyzer {
    private SymbolTable symbolTable;
    private TreeCrawler treeCrawler;
    private SyntaxTreeNode currentNode;
    private Stack<String> scopeStack;
    private boolean inGlobalVars = false;
    private String currentType = null;
    private Set<String> functionNames;
    private Set<String> reservedKeywords;
    private int localVarCount = 0;
    private boolean inFunctionBody = false;
    private String currentFunctionName = null;
    private boolean inGlobalScope = true;
    private boolean isDeclaringGlobals = false;
    private Set<String> calledFunctions;
    private boolean hasReturn = false;

    public SemanticAnalyzer(String xmlFilePath) throws Exception {
        this.treeCrawler = new TreeCrawler(xmlFilePath);
        this.symbolTable = SymbolTable.getInstance();
        this.symbolTable.clear();
        this.scopeStack = new Stack<>();
        this.functionNames = new HashSet<>();
        this.reservedKeywords = new HashSet<>(Arrays.asList("main", "begin", "end", "if", "then", "else", "num", "text", "return", "print", "halt"));
        this.calledFunctions = new HashSet<>();
    }

    public void analyze() throws SemanticException {
        enterScope("global");

        while ((currentNode = treeCrawler.getNext()) != null) {
            switch (currentNode.getSymb()) {
                case "PROG":
                    handleProgramNode();
                    break;
                case "GLOBVARS":
                    handleGlobalVarsNode();
                    break;
                case "LOCVARS":
                    while (currentNode != null && !currentNode.getSymb().equals("ALGO")) {
                        currentNode = treeCrawler.getNext();
                    }
                    if (currentNode != null && currentNode.getSymb().equals("ALGO")) {
                        inGlobalVars = false;
                        handleAlgoNode(currentNode);
                    }
                    break;
                case "VTYP":
                    handleVarTypeNode();
                    break;
                case "VNAME":
                    handleVariableNode();
                    break;
                case "ASSIGN":
                    handleAssignmentNode(currentNode);
                    break;
                case "ALGO":
                    inGlobalVars = false;
                    break;
                case "FUNCTIONS":
                    handleFunctionsNode();
                    break;
                case "HEADER":
                    handleFunctionHeaderNode();
                    break;
                case "BODY":
                    handleBodyNode();
                    break;
                case "CALL":
                    handleFunctionCallNode(currentNode);
                    break;
                case "end":
                    if (inFunctionBody && !scopeStack.peek().equals("global")) {
                        exitFunctionBody();
                    }
                    break;
            }
        }
        verifyCalledFunctions();
        while (!scopeStack.isEmpty()) {
            exitScope();
        }
    }

    private void handleProgramNode() throws SemanticException {
        currentFunctionName = "main";
        functionNames.add("main");
    }

    private void handleGlobalVarsNode() throws SemanticException {
        isDeclaringGlobals = true;
        inGlobalVars = true;
        processGlobalVarsNode(currentNode);
        inGlobalVars = false;
        isDeclaringGlobals = false;
    }

    private void processGlobalVarsNode(SyntaxTreeNode node) throws SemanticException {
        List<SyntaxTreeNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            SyntaxTreeNode child = children.get(i);
            if (child.getSymb().equals("GLOBVARS")) {
                // Instead of recursively calling processGlobalVarsNode, continue processing
                continue;
            } else if (child.getSymb().equals("VTYP")) {
                if (i + 1 < children.size() && children.get(i + 1).getSymb().equals("VNAME")) {
                    currentType = extractType(child);
                    currentNode = children.get(i + 1);  // Set currentNode to VNAME node
                    handleVariableNode();
                    i++;  // Skip the VNAME node in the next iteration
                } else {
                    throw new SemanticException("Invalid global variable declaration structure at node: " + child.getUnid());
                }
            }
        }
    }

    private void handleVariableNode() throws SemanticException {
        String variableName = extractVariableName(currentNode);
        if (variableName == null || variableName.isEmpty()) {
            throw new SemanticException("Invalid variable name at node: " + currentNode.getUnid());
        }
        if (functionNames.contains(variableName)) {
            throw new SemanticException("Variable name " + variableName + " conflicts with a function name");
        }
        if (reservedKeywords.contains(variableName)) {
            throw new SemanticException("Variable name " + variableName + " is a reserved keyword");
        }

        boolean isDeclaration = isVariableDeclaration();

        if (isDeclaringGlobals) {
            // We're declaring global variables
            if (symbolTable.isGlobalVariable(variableName)) {
                throw new SemanticException("Global variable " + variableName + " is already defined");
            }
            symbolTable.addSymbol(new Symbol(variableName, "variable", currentType, currentNode.getUnid()), true);
        } else if (inGlobalScope()) {
            // We're in the global scope but not declaring globals (i.e., using them)
            if (!symbolTable.isGlobalVariable(variableName)) {
                throw new SemanticException("Variable " + variableName + " is not declared in the global scope");
            }
        } else {
            // We're in a local scope
            if (isDeclaration) {
                // This case should not occur here, as local declarations are handled in handleLocVarsNode
                throw new SemanticException("Unexpected local variable declaration: " + variableName);
            } else {
                // This is a variable usage, not a declaration
                if (symbolTable.lookupVariable(variableName, false) == null) {
                    throw new SemanticException("Variable " + variableName + " is not declared before use");
                }
            }
        }
    }

    private boolean isVariableDeclaration() {
        // Check if the parent node is VTYP or if we're in LOCVARS
        SyntaxTreeNode parent = currentNode.getParent();
        return parent != null && (parent.getSymb().equals("VTYP") || parent.getSymb().equals("LOCVARS"));
    }

    private boolean inGlobalScope() {
        return scopeStack.size() == 1 && scopeStack.peek().equals("global");
    }

    private void handleVarTypeNode() {
        currentType = extractType(currentNode);
    }

    private String extractType(SyntaxTreeNode node) {
        if (node.getChildren().size() > 0) {
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

    private void handleAssignmentNode(SyntaxTreeNode assignmentNode) throws SemanticException {
        List<SyntaxTreeNode> children = assignmentNode.getChildren();
        if (children.size() < 2) {
            throw new SemanticException("Invalid assignment structure at node: " + assignmentNode.getUnid());
        }

        SyntaxTreeNode vnameNode = children.get(0);
        String variableName = extractVariableName(vnameNode);

        if (variableName == null || variableName.isEmpty()) {
            throw new SemanticException("Invalid variable in assignment at node: " + assignmentNode.getUnid());
        }

        if (symbolTable.lookupVariable(variableName, false) == null) {
            throw new SemanticException("Variable " + variableName + " is not declared before use");
        }

        String operator = extractOperator(children.get(1));

        if (operator.equals("<")) {
            // Check if the next token is "input"
            if (children.size() >= 3 && extractOperator(children.get(2)).equals("input")) {
                // ASSIGN ::= VNAME < input
                handleInputAssignment(variableName);
            } else {
                throw new SemanticException("Expected 'input' after '<' in assignment");
            }
        } else if (operator.equals("=")) {
            // ASSIGN ::= VNAME = TERM
            if (children.size() < 3) {
                throw new SemanticException("Invalid assignment structure, missing right-hand side at node: " + assignmentNode.getUnid());
            }
            SyntaxTreeNode termNode = children.get(2);
            handleTermAssignment(variableName, termNode);
        } else {
            throw new SemanticException("Invalid assignment operator: '" + operator + "' (ASCII: " + (int)operator.charAt(0) + ")");
        }
    }

    private void handleArgumentNode(SyntaxTreeNode argNode) throws SemanticException {
        if (argNode.getSymb().equals("ATOMIC")) {
            handleAtomicNode(argNode);
        } else if (argNode.getSymb().equals("OP")) {
            handleOperationNode(argNode);
        } else {
            throw new SemanticException("Invalid argument node: " + argNode.getSymb());
        }
    }

    private String extractOperator(SyntaxTreeNode node) {
        if (node.getSymb().equals("Terminal")) {
            String terminal = node.getTerminal();
            if (terminal != null) {
                // First, decode the entire terminal string
                terminal = decodeXmlEntities(terminal);

                if (terminal.contains("<WORD>")) {
                    int start = terminal.indexOf("<WORD>") + 6;
                    int end = terminal.indexOf("</WORD>");
                    String operator = terminal.substring(start, end);

                    // The operator should already be decoded, but let's make sure
                    operator = decodeXmlEntities(operator);

                    return operator;
                }
            }
        }
        return "";
    }

    private String decodeXmlEntities(String input) {
        return input.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&#38;", "&")
                .replace("&#60;", "<")
                .replace("&#62;", ">");
    }

    private SyntaxTreeNode getNextSibling(SyntaxTreeNode node) {
        if (node.getParent() != null) {
            List<SyntaxTreeNode> siblings = node.getParent().getChildren();
            int index = siblings.indexOf(node);
            if (index < siblings.size() - 1) {
                return siblings.get(index + 1);
            }
        }
        return null;
    }

    private void handleTermAssignment(String variableName, SyntaxTreeNode termNode) throws SemanticException {
        switch (termNode.getChildren().get(0).getSymb()) {
            case "ATOMIC":
                handleAtomicNode(termNode.getChildren().get(0));
                break;
            case "CALL":
                handleFunctionCallNode(termNode.getChildren().get(0));
                break;
            case "OP":
                handleOperationNode(termNode.getChildren().get(0));
                break;
            default:
                throw new SemanticException("Invalid term in assignment: " + termNode.getSymb());
        }
    }

    private void handleOperationNode(SyntaxTreeNode opNode) throws SemanticException {
        List<SyntaxTreeNode> children = opNode.getChildren();
        if (children.isEmpty()) {
            throw new SemanticException("Invalid operation node: no children");
        }

        SyntaxTreeNode firstChild = children.get(0);
        if (firstChild.getSymb().equals("UNOP")) {
            if (children.size() != 4) {
                throw new SemanticException("Invalid unary operation structure");
            }
            handleUnaryOperation(firstChild, children.get(2));
        } else if (firstChild.getSymb().equals("BINOP")) {
            if (!firstChild.getChildren().get(0).getSymb().equals("Terminal")) {
                throw new SemanticException("Invalid binary operation structure");
            }
            handleBinaryOperation(firstChild.getChildren().get(0), opNode.getChildren().get(2), opNode.getChildren().get(4));
        } else {
            throw new SemanticException("Invalid operation type: " + firstChild.getSymb());
        }
    }

    private void handleBinaryOperation(SyntaxTreeNode binopNode, SyntaxTreeNode arg1Node, SyntaxTreeNode arg2Node) throws SemanticException {
        String operator = extractOperator(binopNode);
        if (operator == null || operator.isEmpty()) {
            throw new SemanticException("Invalid binary operator at node: " + binopNode.getUnid());
        }

        // Check if the operator is valid
        Set<String> validBinaryOperators = new HashSet<>(Arrays.asList("add", "sub", "mul", "div", "and", "or", "eq", "grt"));
        if (!validBinaryOperators.contains(operator)) {
            throw new SemanticException("Invalid binary operator: " + operator);
        }

        handleArgumentNode(arg1Node.getChildren().get(0));
        handleArgumentNode(arg2Node.getChildren().get(0));

    }

    private void handleUnaryOperation(SyntaxTreeNode unopNode, SyntaxTreeNode argNode) throws SemanticException {
        String operator = extractOperator(unopNode.getChildren().get(0));
        if (operator == null || operator.isEmpty()) {
            throw new SemanticException("Invalid unary operator at node: " + unopNode.getUnid());
        }

        Set<String> validUnaryOperators = new HashSet<>(Arrays.asList("not", "sqrt"));
        if (!validUnaryOperators.contains(operator)) {
            throw new SemanticException("Invalid unary operator: " + operator);
        }

        handleArgumentNode(argNode.getChildren().get(0));
    }

    private void handleInputAssignment(String variableName) throws SemanticException {
        // Check if the variable exists in the current scope or global scope
        Symbol symbol = symbolTable.lookupVariable(variableName, false);
        if (symbol == null) {
            throw new SemanticException("Variable " + variableName + " is not declared before input assignment");
        }

    }


    private String extractVariableNameFromAssignment(SyntaxTreeNode node) {
        if (node.getSymb().equals("VNAME")) {
            return extractVariableName(node);
        } else if (node.getSymb().equals("Terminal")) {
            String terminal = node.getTerminal();
            if (terminal != null && terminal.contains("<WORD>")) {
                int start = terminal.indexOf("<WORD>") + 6;
                int end = terminal.indexOf("</WORD>");
                return terminal.substring(start, end);
            }
        } else if (!node.getChildren().isEmpty()) {
            return extractVariableNameFromAssignment(node.getChildren().get(0));
        }
        return null;
    }

    private void handleFunctionsNode() throws SemanticException {
        if (!inFunctionBody) {
            enterScope("function");
            inFunctionBody = true;
            localVarCount = 0;
        }
    }

    private void handleFunctionHeaderNode() throws SemanticException {
        List<SyntaxTreeNode> children = currentNode.getChildren();
        if (children.size() != 9) { // FTYP, FNAME, and 3 parameters
            throw new SemanticException("Function must have exactly 3 parameters");
        }

        String functionType = extractType(children.get(0));
        String functionName = extractFunctionName(children.get(1));
        if (functionNames.contains(functionName)) {
            throw new SemanticException("Function " + functionName + " is already declared");
        }
        if (reservedKeywords.contains(functionName)) {
            throw new SemanticException("Function name " + functionName + " is a reserved keyword");
        }
        if (functionName.equals(scopeStack.peek())) {
            throw new SemanticException("Function name " + functionName + " is the same as its parent scope");
        }
        for (String existingFunction : functionNames) {
            if (existingFunction.startsWith(scopeStack.peek() + ".") && !existingFunction.equals(scopeStack.peek() + "." + functionName)) {
                throw new SemanticException("Function name " + functionName + " conflicts with a sibling scope");
            }
        }
        functionNames.add(functionName);
        currentFunctionName = functionName;

        symbolTable.addSymbol(new Symbol(functionName, "function", functionType, currentNode.getUnid()), true);
        enterScope(functionName);

        // Add parameters to the current function scope
        for (int i = 2; i < children.size(); i++) {
            SyntaxTreeNode paramNode = children.get(i);
            if (paramNode.getSymb().equals("VNAME")) {
                String paramName = extractVariableName(paramNode);
                symbolTable.addSymbol(new Symbol(paramName, "parameter", "num", paramNode.getUnid()), false); //params will always be nums
            }
        }
    }

    private void verifyCalledFunctions() throws SemanticException {
        for (String calledFunction : calledFunctions) {
            if (!functionNames.contains(calledFunction)) {
                throw new SemanticException("Function " + calledFunction + " is called but never declared");
            }
        }
    }

    private void handleFunctionCallNode(SyntaxTreeNode node) throws SemanticException {
        List<SyntaxTreeNode> children = node.getChildren();
        if (children.size() != 8) {
            throw new SemanticException("Invalid function call structure");
        }

        String functionName = extractFunctionName(children.get(0));
        calledFunctions.add(functionName);  // Record that this function was called

        if (functionName.equals("main")) {
            throw new SemanticException("Recursive calls to MAIN are not allowed");
        }

        // Verify parameters
        for (int i = 2; i <= 6; i += 2) {
            if (!children.get(i).getSymb().equals("ATOMIC")) {
                throw new SemanticException("Function parameter must be ATOMIC");
            }
            handleAtomicNode(children.get(i));
        }
    }

    private void enterScope(String scopeName) throws SemanticException {
        if (!scopeStack.isEmpty()) {
            String parentScope = scopeStack.peek();
            if (scopeName.equals(parentScope)) {
                throw new SemanticException("Child scope " + scopeName + " has the same name as its parent scope");
            }
            // Check for sibling scopes with the same name
            for (String existingScope : functionNames) {
                if (existingScope.startsWith(parentScope + ".") && existingScope.endsWith("." + scopeName)) {
                    throw new SemanticException("Sibling scope " + scopeName + " already exists");
                }
            }
        }
        scopeStack.push(scopeName);
        symbolTable.enterScope(scopeName);
    }

    private void exitFunctionBody() {
        inFunctionBody = false;
        exitScope();
    }

    private void exitScope() {
        if (!scopeStack.isEmpty()) {
            scopeStack.pop();
            symbolTable.exitScope();
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

    private String extractFunctionName(SyntaxTreeNode node) {
        if (node.getSymb().equals("FNAME") && node.getChildren().size() > 0) {
            SyntaxTreeNode terminalNode = node.getChildren().get(0);
            return extractFromTerminal(terminalNode.getTerminal());
        }
        return null;
    }

    private String extractFromTerminal(String terminal) {
        if (terminal != null && terminal.contains("<WORD>")) {
            int start = terminal.indexOf("<WORD>") + 6;
            int end = terminal.indexOf("</WORD>");
            return terminal.substring(start, end);
        }
        return null;
    }

    private void handleBodyNode() throws SemanticException {
        hasReturn = false;

        List<SyntaxTreeNode> children = currentNode.getChildren();

        SyntaxTreeNode locvarsNode = null;
        SyntaxTreeNode algoNode = null;
        SyntaxTreeNode subFuncs = null;

        // Find LOCVARS, ALGO, and SUBFUNCS nodes
        for (SyntaxTreeNode child : children) {
            if (child.getSymb().equals("LOCVARS")) {
                locvarsNode = child;
            } else if (child.getSymb().equals("ALGO")) {
                algoNode = child;
            } else if (child.getSymb().equals("SUBFUNCS")) {
                subFuncs = child;
            }
        }

        // Handle LOCVARS if present
        if (locvarsNode != null) {
            handleLocVarsNode(locvarsNode);
        }

        // Handle ALGO
        if (algoNode != null) {
            handleAlgoNode(algoNode);
        } else {
            throw new SemanticException("Expected ALGO node in function body");
        }

        Symbol functionSymbol = symbolTable.lookupVariable(currentFunctionName, false);
        if (functionSymbol != null && functionSymbol.getType().equals("num") && !hasReturn) {
            throw new SemanticException("Function " + currentFunctionName + " must have at least one return statement");
        }

        // Handle SUBFUNCS only if it contains actual functions
        if (subFuncs != null && !subFuncs.getChildren().isEmpty()) {
            handleSubFuncsNode(subFuncs);
        }
    }

    private int handleLocVarsNode(SyntaxTreeNode locvarsNode) throws SemanticException {
        List<SyntaxTreeNode> varNodes = locvarsNode.getChildren();
        int processedNodes = 0;
        for (int i = 0; i < varNodes.size(); i++) {
            SyntaxTreeNode node = varNodes.get(i);
            if (node.getSymb().equals("VTYP")) {
                currentType = extractType(node);
                if (i + 1 < varNodes.size() && varNodes.get(i + 1).getSymb().equals("VNAME")) {
                    SyntaxTreeNode vnameNode = varNodes.get(i + 1);
                    String variableName = extractVariableName(vnameNode);
                    if (variableName == null || variableName.isEmpty()) {
                        throw new SemanticException("Invalid variable name at node: " + vnameNode.getUnid());
                    }
                    if (functionNames.contains(variableName)) {
                        throw new SemanticException("Variable name " + variableName + " conflicts with a function name");
                    }
                    if (reservedKeywords.contains(variableName)) {
                        throw new SemanticException("Variable name " + variableName + " is a reserved keyword");
                    }
                    if (symbolTable.lookupVariable(variableName, true) != null) {
                        throw new SemanticException("Variable " + variableName + " is already defined in this scope");
                    }
                    symbolTable.addSymbol(new Symbol(variableName, "variable", currentType, vnameNode.getUnid()), false);
                    i++; // Skip the VNAME node in the next iteration
                    processedNodes += 2; // Count both VTYP and VNAME nodes
                } else {
                    throw new SemanticException("Invalid local variable declaration structure at node: " + node.getUnid());
                }
            }
        }
        return processedNodes + 3;
    }

    private void handleAlgoNode(SyntaxTreeNode algoNode) throws SemanticException {
        List<SyntaxTreeNode> children = algoNode.getChildren();

        if (children.size() != 3) {
            throw new SemanticException("Invalid ALGO structure: expected 3 children, found " + children.size() + " (UNID: " + algoNode.getUnid() + ")");
        }

        SyntaxTreeNode beginNode = children.get(0);
        SyntaxTreeNode instrucNode = children.get(1);
        SyntaxTreeNode endNode = children.get(2);

        if (!beginNode.getSymb().equals("Terminal")) {
            throw new SemanticException("Invalid ALGO structure: expected 'begin', found '" + beginNode.getSymb() + "' (UNID: " + beginNode.getUnid() + ")");
        }

        if (!endNode.getSymb().equals("Terminal")) {
            throw new SemanticException("Invalid ALGO structure: expected 'end', found '" + endNode.getSymb() + "' (UNID: " + endNode.getUnid() + ")");
        }

        if (!instrucNode.getSymb().equals("INSTRUC")) {
            throw new SemanticException("Invalid ALGO structure: expected 'INSTRUC', found '" + instrucNode.getSymb() + "' (UNID: " + instrucNode.getUnid() + ")");
        }

        handleInstrucNode(instrucNode);
    }

    private void handleInstrucNode(SyntaxTreeNode instrucNode) throws SemanticException {
        // INSTRUC ::= COMMAND ; INSTRUC | ε
        List<SyntaxTreeNode> children = instrucNode.getChildren();

        if (children.isEmpty()) {
            return;
        }

        if (children.size() >= 1 && children.get(0).getSymb().equals("COMMAND")) {
            handleCommandNode(children.get(0));

            if (children.size() >= 3) {
                handleInstrucNode(children.get(2));
            }
        }
    }

    private void handleCommandNode(SyntaxTreeNode commandNode) throws SemanticException {
        List<SyntaxTreeNode> children = commandNode.getChildren();

        for (SyntaxTreeNode child : children) {
            if (child.getSymb().equals("Terminal")) {
                String terminal = child.getTerminal();
                if (terminal != null) {
                    if (terminal.contains("<WORD>return</WORD>")) {
                        handleReturnCommand(commandNode);
                        hasReturn = true;
                        return;
                    } else if (terminal.contains("<WORD>print</WORD>")) {
                        handlePrintCommand(commandNode);
                        return;
                    } else if (terminal.contains("<WORD>halt</WORD>") ||
                            terminal.contains("<WORD>skip</WORD>")) {
                        return;
                    }
                }
            }
        }

        SyntaxTreeNode firstChild = children.get(0);
        String command = firstChild.getSymb();

        switch (command) {
            case "ASSIGN":
                handleAssignmentNode(firstChild);
                break;
            case "CALL":
                handleFunctionCallNode(currentNode);
                break;
            case "BRANCH":
                handleBranchNode(firstChild);
                break;
            case "ATOMIC":
                if (children.size() == 2) {
                    SyntaxTreeNode otherChild = children.get(1);
                    if (otherChild.getSymb().equals("Terminal") &&
                            otherChild.getTerminal() != null &&
                            otherChild.getTerminal().contains("<WORD>return</WORD>")) {
                        handleReturnCommand(commandNode);
                        hasReturn = true;
                        return;
                    }
                }
                break;
            default:
                throw new SemanticException("Unknown command type: " + command);
        }
    }

    private void handleReturnCommand(SyntaxTreeNode node) throws SemanticException {
        if (!inFunctionBody || currentFunctionName == null || currentFunctionName.equals("main")) {
            throw new SemanticException("Return statement found outside of function body");
        }

        Symbol functionSymbol = symbolTable.lookupVariable(currentFunctionName, false);
        if (functionSymbol == null) {
            throw new SemanticException("Cannot find function " + currentFunctionName);
        }

        String functionType = functionSymbol.getType();

        // In an INSTRUC node, return is followed by ATOMIC
        SyntaxTreeNode atomicNode = null;
        for (int i = 0; i < node.getChildren().size(); i++) {
            SyntaxTreeNode child = node.getChildren().get(i);
            if (child.getSymb().equals("ATOMIC")) {
                atomicNode = child;
                break;
            }
        }

        if (atomicNode == null) {
            throw new SemanticException("Return statement must be followed by an ATOMIC value");
        }

        if (functionType.equals("void")) {
            throw new SemanticException("Void function cannot return a value");
        }

        if (functionType.equals("num")) {
            SyntaxTreeNode valueNode = atomicNode.getChildren().get(0);
            if (valueNode.getSymb().equals("VNAME")) {
                String varName = extractVariableName(valueNode);
                Symbol varSymbol = symbolTable.lookupVariable(varName, false);
                if (varSymbol == null) {
                    throw new SemanticException("Undefined variable in return statement: " + varName);
                }
                if (!varSymbol.getType().equals("num")) {
                    throw new SemanticException("Return value type mismatch: expected num, got " + varSymbol.getType());
                }
            } else if (valueNode.getSymb().equals("CONST")) {
                String constValue = extractFromTerminal(valueNode.getChildren().get(0).getTerminal());
                if (constValue.startsWith("\"")) {
                    throw new SemanticException("Return value type mismatch: expected num, got text");
                }
            }
        }
    }

    private void handleSubFuncsNode(SyntaxTreeNode subfuncsNode) throws SemanticException {
        // SUBFUNCS ::= FUNCTIONS
        List<SyntaxTreeNode> functionNodes = subfuncsNode.getChildren();
        for (SyntaxTreeNode functionNode : functionNodes) {
            handleFunctionsNode();
        }
    }

    private void handlePrintCommand(SyntaxTreeNode printNode) throws SemanticException {
        List<SyntaxTreeNode> children = printNode.getChildren();
        // Find the ATOMIC node - it should be the second child after the print Terminal
        SyntaxTreeNode atomicNode = null;
        for (SyntaxTreeNode child : children) {
            if (child.getSymb().equals("ATOMIC")) {
                atomicNode = child;
                break;
            }
        }

        if (atomicNode == null) {
            throw new SemanticException("Print command must have exactly one ATOMIC argument");
        }

        handleAtomicNode(atomicNode);
    }

    private void handleAtomicNode(SyntaxTreeNode atomicNode) throws SemanticException {
        // ATOMIC ::= VNAME | CONST
        if (atomicNode.getChildren().isEmpty()) {
            throw new SemanticException("Invalid ATOMIC node: no children");
        }

        SyntaxTreeNode child = atomicNode.getChildren().get(0);
        String childSymb = child.getSymb();

        if (childSymb.equals("VNAME")) {
            String variableName = extractVariableName(child);
            if (symbolTable.lookupVariable(variableName, false) == null) {
                throw new SemanticException("Variable " + variableName + " is not declared before use");
            }
        } else if (childSymb.equals("CONST")) {
            // Constants are always valid, no need to check further
            // You might want to add type checking here in the future
        } else {
            throw new SemanticException("Invalid ATOMIC node: " + childSymb);
        }
    }

    private void handleBranchNode(SyntaxTreeNode branchNode) throws SemanticException {
        // BRANCH ::= if COND then ALGO else ALGO
        List<SyntaxTreeNode> children = branchNode.getChildren();

        for (int i = 0; i < children.size(); i++) {
            SyntaxTreeNode child = children.get(i);
        }

        if (children.size() != 6) {
            throw new SemanticException("Invalid branch structure: expected 6 children, found " + children.size() + " (UNID: " + branchNode.getUnid() + ")");
        }

        // Check COND
        SyntaxTreeNode condNode = children.get(1);
        handleCondNode(condNode);

        // Check first ALGO (then branch)
        SyntaxTreeNode thenAlgoNode = children.get(3);
        if (!thenAlgoNode.getSymb().equals("ALGO")) {
            throw new SemanticException("Expected ALGO node for 'then' branch, found " + thenAlgoNode.getSymb() + " (UNID: " + thenAlgoNode.getUnid() + ")");
        }
        handleAlgoNode(thenAlgoNode);

        // Check second ALGO (else branch)
        SyntaxTreeNode elseAlgoNode = children.get(5);
        if (!elseAlgoNode.getSymb().equals("ALGO")) {
            throw new SemanticException("Expected ALGO node for 'else' branch, found " + elseAlgoNode.getSymb() + " (UNID: " + elseAlgoNode.getUnid() + ")");
        }
        handleAlgoNode(elseAlgoNode);
    }

    private void handleCondNode(SyntaxTreeNode condNode) throws SemanticException {
        // COND ::= SIMPLE | COMPOSIT
        SyntaxTreeNode child = condNode.getChildren().get(0);
        if (child.getSymb().equals("SIMPLE")) {
            handleSimpleCondNode(child);
        } else if (child.getSymb().equals("COMPOSIT")) {
            handleCompositCondNode(child);
        } else {
            throw new SemanticException("Invalid condition type: " + child.getSymb());
        }
    }

    private void handleSimpleCondNode(SyntaxTreeNode simpleNode) throws SemanticException {
        // SIMPLE ::= BINOP( ATOMIC , ATOMIC )
        List<SyntaxTreeNode> children = simpleNode.getChildren();
        if (children.size() != 6) {
            throw new SemanticException("Invalid simple condition structure");
        }

        String binop = extractBinOp(children.get(0));
        handleAtomicNode(children.get(2));
        handleAtomicNode(children.get(4));

        // Check if the BINOP is a valid comparison operator
        if (!binop.equals("eq") && !binop.equals("grt")) {
            throw new SemanticException("Invalid comparison operator in condition: " + binop);
        }
    }

    private void handleCompositCondNode(SyntaxTreeNode compositNode) throws SemanticException {
        // COMPOSIT ::= BINOP( SIMPLE , SIMPLE ) | UNOP ( SIMPLE )
        List<SyntaxTreeNode> children = compositNode.getChildren();
        if (children.size() == 4) {
            // BINOP( SIMPLE , SIMPLE )
            String binop = extractBinOp(children.get(0));
            handleSimpleCondNode(children.get(2));
            handleSimpleCondNode(children.get(3));

            // Check if the BINOP is a valid logical operator
            if (!binop.equals("and") && !binop.equals("or")) {
                throw new SemanticException("Invalid logical operator in composite condition: " + binop);
            }
        } else if (children.size() == 3) {
            // UNOP ( SIMPLE )
            String unop = extractUnOp(children.get(0));
            handleSimpleCondNode(children.get(2));

            // Check if the UNOP is 'not'
            if (!unop.equals("not")) {
                throw new SemanticException("Invalid unary operator in composite condition: " + unop);
            }
        } else {
            throw new SemanticException("Invalid composite condition structure");
        }
    }

    private String extractBinOp(SyntaxTreeNode binopNode) {
        if (binopNode.getSymb().equals("BINOP") && binopNode.getChildren().size() > 0) {
            SyntaxTreeNode terminalNode = binopNode.getChildren().get(0);
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

    private String extractUnOp(SyntaxTreeNode unopNode) {
        if (unopNode.getSymb().equals("UNOP") && unopNode.getChildren().size() > 0) {
            SyntaxTreeNode terminalNode = unopNode.getChildren().get(0);
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
            SemanticAnalyzer analyzer = new SemanticAnalyzer("src/parser2/output/output5.xml");
            analyzer.analyze();
            System.out.println("Semantic analysis completed successfully.");
            SymbolTable.getInstance().getAllSymbols().forEach((key, symbols) -> {
                symbols.forEach(symbol ->
                        System.out.println(key + ": " + symbol.getName() + " (" + symbol.getKind() + ")" + " - " + symbol.getType()));
            });
        } catch (Exception e) {
            System.err.println("Semantic analysis failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}