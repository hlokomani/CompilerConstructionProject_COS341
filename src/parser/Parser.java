package parser;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int currentTokenIndex;
    private final SyntaxTreeBuilder treeBuilder;

    public Parser(String inputXmlPath) throws Exception {
        this.tokens = XMLTokenReader.readTokens(inputXmlPath);
        this.currentTokenIndex = 0;
        this.treeBuilder = new SyntaxTreeBuilder();
    }

    public void parse() {
        try {
            TreeNode root = parseProgram();
            treeBuilder.buildTree(root);
            treeBuilder.writeToFile("syntax_tree.xml");
            System.out.println("Parsing completed successfully. Syntax tree written to syntax_tree.xml");
        } catch (ParseException e) {
            System.err.println("Syntax Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred during parsing or tree building: " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("Caused by: " + cause.getMessage());
                cause = cause.getCause();
            }
            e.printStackTrace();
        }
    }

    private TreeNode parseProgram() throws ParseException {
        System.out.println("Entering parseProgram");
        TreeNode programNode = new TreeNode("PROG");
        expect("main", programNode);
        System.out.println("Parsing global variables");
        parseGlobalVars(programNode);
        System.out.println("Parsing algorithm");
        parseAlgorithm(programNode);
        System.out.println("Parsing functions");
        parseFunctions(programNode);
        System.out.println("Exiting parseProgram");
        return programNode;
    }

    private void parseGlobalVars(TreeNode parent) throws ParseException {
        System.out.println("Entering parseGlobalVars");
        TreeNode globVarsNode = new TreeNode("GLOBVARS");
        parent.addChild(globVarsNode);
        while (checkToken("num") || checkToken("text")) {
            System.out.println("Parsing variable declaration");
            parseVarDeclaration(globVarsNode);
            if (checkToken(",")) {
                System.out.println("Found comma, expecting more variables");
                expect(",", globVarsNode);
            } else {
                System.out.println("No more variables");
                break;
            }
        }
        System.out.println("Exiting parseGlobalVars");
    }

    private void parseVarDeclaration(TreeNode parent) throws ParseException {
        System.out.println("Entering parseVarDeclaration");
        TreeNode varDeclNode = new TreeNode("VAR_DECL");
        parent.addChild(varDeclNode);
        expectAny(new String[]{"num", "text"}, varDeclNode);
        expect("V", varDeclNode);
        System.out.println("Exiting parseVarDeclaration");
    }

    private void parseAlgorithm(TreeNode parent) throws ParseException {
        System.out.println("Entering parseAlgorithm");
        TreeNode algoNode = new TreeNode("ALGO");
        parent.addChild(algoNode);
        expect("begin", algoNode);
        parseInstructions(algoNode);
        expect("end", algoNode);
        System.out.println("Exiting parseAlgorithm");
    }

    private void parseInstructions(TreeNode parent) throws ParseException {
        System.out.println("Entering parseInstructions");
        while (!checkToken("end")) {
            parseCommand(parent);
            if (checkToken(";")) {
                expect(";", parent);
            }
        }
        System.out.println("Exiting parseInstructions");
    }

    private void parseCommand(TreeNode parent) throws ParseException {
        System.out.println("Entering parseCommand");
        TreeNode commandNode = new TreeNode("COMMAND");
        parent.addChild(commandNode);
        if (checkToken("skip")) {
            expect("skip", commandNode);
        } else if (checkToken("halt")) {
            expect("halt", commandNode);
        } else if (checkToken("print")) {
            expect("print", commandNode);
            parseAtomic(commandNode);
        } else if (checkToken("V")) {
            parseAssignment(commandNode);
        } else if (checkToken("F")) {
            parseCall(commandNode);
        } else if (checkToken("if")) {
            parseBranch(commandNode);
        } else {
            throw new ParseException("Unexpected token: " + getCurrentToken().getWord());
        }
        System.out.println("Exiting parseCommand");
    }

    private void parseAssignment(TreeNode parent) throws ParseException {
        TreeNode assignNode = new TreeNode("ASSIGN");
        parent.addChild(assignNode);
        expect("V", assignNode);
        if (checkToken("<")) {
            expect("<", assignNode);
            expect("input", assignNode);
        } else {
            expect("=", assignNode);
            parseTerm(assignNode);
        }
    }

    private void parseTerm(TreeNode parent) throws ParseException {
        TreeNode termNode = new TreeNode("TERM");
        parent.addChild(termNode);
        if (checkToken("V") || checkToken("N") || checkToken("T")) {
            parseAtomic(termNode);
        } else if (checkToken("F")) {
            parseCall(termNode);
        } else {
            parseOperation(termNode);
        }
    }

    private void parseAtomic(TreeNode parent) throws ParseException {
        System.out.println("Entering parseAtomic");
        TreeNode atomicNode = new TreeNode("ATOMIC");
        parent.addChild(atomicNode);
        expectAny(new String[]{"V", "N", "T"}, atomicNode);
        System.out.println("Exiting parseAtomic");
    }

    private void parseCall(TreeNode parent) throws ParseException {
        System.out.println("Entering parseCall");
        TreeNode callNode = new TreeNode("CALL");
        parent.addChild(callNode);
        expect("F", callNode);
        expect("(", callNode);
        parseAtomic(callNode);
        expect(",", callNode);
        parseAtomic(callNode);
        expect(",", callNode);
        parseAtomic(callNode);
        expect(")", callNode);
        System.out.println("Exiting parseCall");
    }

    private void parseOperation(TreeNode parent) throws ParseException {
        TreeNode opNode = new TreeNode("OP");
        parent.addChild(opNode);
        if (checkAny(new String[]{"not", "sqrt"})) {
            parseUnaryOp(opNode);
        } else {
            parseBinaryOp(opNode);
        }
    }

    private void parseUnaryOp(TreeNode parent) throws ParseException {
        TreeNode unOpNode = new TreeNode("UNOP");
        parent.addChild(unOpNode);
        expectAny(new String[]{"not", "sqrt"}, unOpNode);
        expect("(", unOpNode);
        parseArgument(unOpNode);
        expect(")", unOpNode);
    }

    private void parseBinaryOp(TreeNode parent) throws ParseException {
        TreeNode binOpNode = new TreeNode("BINOP");
        parent.addChild(binOpNode);
        expectAny(new String[]{"or", "and", "eq", "grt", "add", "sub", "mul", "div"}, binOpNode);
        expect("(", binOpNode);
        parseArgument(binOpNode);
        expect(",", binOpNode);
        parseArgument(binOpNode);
        expect(")", binOpNode);
    }

    private void parseArgument(TreeNode parent) throws ParseException {
        TreeNode argNode = new TreeNode("ARG");
        parent.addChild(argNode);
        if (checkToken("V") || checkToken("N") || checkToken("T")) {
            parseAtomic(argNode);
        } else {
            parseOperation(argNode);
        }
    }

    private void parseBranch(TreeNode parent) throws ParseException {
        TreeNode branchNode = new TreeNode("BRANCH");
        parent.addChild(branchNode);
        expect("if", branchNode);
        parseCondition(branchNode);
        expect("then", branchNode);
        parseAlgorithm(branchNode);
        expect("else", branchNode);
        parseAlgorithm(branchNode);
    }

    private void parseCondition(TreeNode parent) throws ParseException {
        System.out.println("Entering parseCondition");
        TreeNode condNode = new TreeNode("COND");
        parent.addChild(condNode);
        if (checkAny(new String[]{"or", "and", "eq", "grt", "not"})) {
            parseSimpleCondition(condNode);
        } else {
            throw new ParseException("Expected condition, found " + getCurrentToken().getWord());
        }
        System.out.println("Exiting parseCondition");
    }

    private void parseSimpleCondition(TreeNode parent) throws ParseException {
        System.out.println("Entering parseSimpleCondition");
        TreeNode simpleCondNode = new TreeNode("SIMPLE");
        parent.addChild(simpleCondNode);
        expectAny(new String[]{"or", "and", "eq", "grt"}, simpleCondNode);
        expect("(", simpleCondNode);
        parseAtomic(simpleCondNode);
        expect(",", simpleCondNode);
        parseAtomic(simpleCondNode);
        expect(")", simpleCondNode);
        System.out.println("Exiting parseSimpleCondition");
    }

    private void parseCompositeCondition(TreeNode parent) throws ParseException {
        TreeNode compCondNode = new TreeNode("COMPOSIT");
        parent.addChild(compCondNode);
        if (checkToken("not")) {
            expect("not", compCondNode);
            expect("(", compCondNode);
            parseSimpleCondition(compCondNode);
            expect(")", compCondNode);
        } else {
            expectAny(new String[]{"or", "and"}, compCondNode);
            expect("(", compCondNode);
            parseSimpleCondition(compCondNode);
            expect(",", compCondNode);
            parseSimpleCondition(compCondNode);
            expect(")", compCondNode);
        }
    }

    private void parseFunctions(TreeNode parent) throws ParseException {
        while (checkToken("num") || checkToken("void")) {
            parseFunction(parent);
        }
    }

    private void parseFunction(TreeNode parent) throws ParseException {
        TreeNode funcNode = new TreeNode("FUNCTION");
        parent.addChild(funcNode);
        parseFunctionHeader(funcNode);
        parseFunctionBody(funcNode);
    }

    private void parseFunctionHeader(TreeNode parent) throws ParseException {
        TreeNode headerNode = new TreeNode("HEADER");
        parent.addChild(headerNode);
        expectAny(new String[]{"num", "void"}, headerNode);
        expect("F", headerNode);
        expect("(", headerNode);
        expect("V", headerNode);
        expect(",", headerNode);
        expect("V", headerNode);
        expect(",", headerNode);
        expect("V", headerNode);
        expect(")", headerNode);
    }

    private void parseFunctionBody(TreeNode parent) throws ParseException {
        TreeNode bodyNode = new TreeNode("BODY");
        parent.addChild(bodyNode);
        expect("{", bodyNode);
        parseLocalVars(bodyNode);
        parseAlgorithm(bodyNode);
        parseSubFunctions(bodyNode);
        expect("}", bodyNode);
        expect("end", bodyNode);
    }

    private void parseLocalVars(TreeNode parent) throws ParseException {
        TreeNode locVarsNode = new TreeNode("LOCVARS");
        parent.addChild(locVarsNode);
        for (int i = 0; i < 3; i++) {
            parseVarDeclaration(locVarsNode);
            expect(",", locVarsNode);
        }
    }

    private void parseSubFunctions(TreeNode parent) throws ParseException {
        TreeNode subFuncsNode = new TreeNode("SUBFUNCS");
        parent.addChild(subFuncsNode);
        while (checkToken("num") || checkToken("void")) {
            parseFunction(subFuncsNode);
        }
    }

    private boolean checkToken(String expected) {
        if (currentTokenIndex >= tokens.size()) return false;
        Token token = tokens.get(currentTokenIndex);
        boolean result = token.getTokenClass().equals(expected) || token.getWord().equals(expected);
        System.out.println("Checking token: " + expected + ", Found: " + token.getWord() + " (Class: " + token.getTokenClass() + "), Result: " + result);
        return result;
    }

    private boolean checkAny(String[] expectedTypes) {
        for (String type : expectedTypes) {
            if (checkToken(type)) return true;
        }
        return false;
    }

    private void expect(String expectedType, TreeNode parent) throws ParseException {
        if (currentTokenIndex >= tokens.size()) {
            throw new ParseException("Unexpected end of input");
        }
        Token token = tokens.get(currentTokenIndex);
        System.out.println("Expecting: " + expectedType + ", Found: " + token.getWord() + " (Class: " + token.getTokenClass() + ")");
        if (!token.getTokenClass().equals(expectedType) && !token.getWord().equals(expectedType)) {
            System.out.println("Mismatch found. Current context:");
            printCurrentContext();
            throw new ParseException("Expected " + expectedType + ", found " + token.getWord());
        }
        parent.addChild(new LeafNode(token));
        currentTokenIndex++;
    }

    private void expectAny(String[] expectedTypes, TreeNode parent) throws ParseException {
        for (String type : expectedTypes) {
            if (checkToken(type)) {
                expect(type, parent);
                return;
            }
        }
        throw new ParseException("Expected one of " + Arrays.toString(expectedTypes) + ", found " + getCurrentToken().getWord());
    }

    private void printCurrentContext() {
        for (int i = currentTokenIndex; i < Math.min(currentTokenIndex + 5, tokens.size()); i++) {
            Token token = tokens.get(i);
            System.out.println(String.format("  Token %d: %s (Class: %s)",
                    i - currentTokenIndex, token.getWord(), token.getTokenClass()));
        }
    }

    private Token getCurrentToken() {
        return currentTokenIndex < tokens.size() ? tokens.get(currentTokenIndex) : null;
    }
}
