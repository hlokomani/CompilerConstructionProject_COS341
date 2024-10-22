package intermediateCodeGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import parser2.SyntaxTreeNode;
import semanticanalyzer.SemanticAnalyzer;
import semanticanalyzer.SymbolTableAccessor;
import typeChecker.TreeCrawler;

public class intermeridateCodeGeneration {
    private TreeCrawler treeCrawler;
    private FileWriter outputFile;
    private int varCounter, labelCounter;
    private SyntaxTreeNode root;

    public intermeridateCodeGeneration(String xmlFilePath) throws Exception {
        //Initialize symbol table 
        treeCrawler = new TreeCrawler(xmlFilePath);
        varCounter = 0;
        labelCounter = 0;
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(xmlFilePath);
        semanticAnalyzer.analyze();
        //Create text file to write the translated code
        //extracting the output name from the xml file name from src/parser2/output1.xml
        String outputName = xmlFilePath.substring(xmlFilePath.lastIndexOf("/") + 1, xmlFilePath.lastIndexOf("."));
        String outputFilePath = "output/" + outputName + ".txt";
        File output = new File(outputFilePath);
        output.createNewFile();
        output.setWritable(true);
        outputFile = new FileWriter(output, true);
    }
   
    public SyntaxTreeNode trans() {
        try {
            root = treeCrawler.getNext();
            String intermediateCode = transPROG(root);
            outputFile.write(intermediateCode);
            outputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }
    
    public String transPROG(SyntaxTreeNode prog) throws IOException {
        //PROG->main GLOBVARS ALGO FUNCTIONS
        String algo = transALGO(prog.getChildren().get(2));
        String functions = transFUNCTIONS(prog.getChildren().get(3));

        //adding it to the syntax tree
        prog.setIntermediateCode(algo + functions);
        return algo + functions;
    }

    public String transVNAME(SyntaxTreeNode vname) throws IOException {
        SyntaxTreeNode next = vname.getChildren().get(0);
        String newName = SymbolTableAccessor.lookupVariable(next.getTerminalWord()).getName();

        //adding it to the syntax tree
        vname.setIntermediateCode(newName);

        return newName;
    }

    public String transVNAME(SyntaxTreeNode vname, String place) throws IOException {
        SyntaxTreeNode next = vname.getChildren().get(0);
        String newName = SymbolTableAccessor.lookupVariable(next.getTerminalWord()).getName();
        
        //adding it to the syntax tree
        vname.setIntermediateCode("\n " + place  + " = " + newName+ " ");
        return "\n " + place  + " = " + newName+ " ";
    }

    public String transALGO(SyntaxTreeNode algo) throws IOException {
        // ALGO -> begin INSTRUC end
        String instruc = transINSTRUC(algo.getChildren().get(1));
        //adding it to the syntax tree
        algo.setIntermediateCode(instruc);
        return instruc;
    }

    public String transINSTRUC(SyntaxTreeNode instruc) throws IOException {
        List<SyntaxTreeNode> children = instruc.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal() != null && next.getTerminal().isEmpty()) { //Case 1: INSTRUC -> 
            //adding to the syntax tree
            instruc.setIntermediateCode("\nREM END");
            return "\nREM END";
        } else if (next.getSymb().equals("COMMAND")) { //Case 2: INSTRUC -> COMMAND ; INSTRUC
            String command = transCOMMAND(next);
            String instruc2 = transINSTRUC(children.get(2));
            //adding to the syntax tree
            instruc.setIntermediateCode(command + instruc2);
            return command + instruc2;
        }
        return "";
    }

    public String transCOMMAND(SyntaxTreeNode command) throws IOException {
        List<SyntaxTreeNode> children = command.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal() != null && next.getTerminal().contains("<WORD>skip</WORD>")) { //Case 1: COMMAND -> skip
            //adding to the syntax tree
            command.setIntermediateCode("\nREM DO NOTHING");
            return "\nREM DO NOTHING";
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>halt</WORD>")) { //Case 2: COMMAND -> halt
            command.setIntermediateCode("\nSTOP");
            return "\nSTOP";
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>print</WORD>")) {//Case 3: COMMAND -> print ATOMIC
            String atomic = transATOMIC(children.get(1));
            command.setIntermediateCode("\nPRINT" +  atomic);
            return "\nPRINT" +  atomic;
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>return</WORD>")) {//Case 4: COMMAND -> return ATOMIC
            String atomic = transATOMIC(children.get(1));
            command.setIntermediateCode("\nreturn" +  atomic);
            return "\nreturn" +  atomic;
        } else if (next.getSymb().equals("ASSIGN")) {//Case 5: COMMAND -> ASSIGN
            String assign = transASSIGN(children.get(0));
            command.setIntermediateCode(assign);
            return assign;
        } else if (next.getSymb().equals("CALL")) { //Case 6: COMMAND -> CALL
            String call = transCALL(children.get(0));
            command.setIntermediateCode(call);
            return call;
        } else if (next.getSymb().equals("BRANCH")) { //Case 7: COMMAND -> BRANCH
            String branch = transBRANCH(children.get(0));
            command.setIntermediateCode(branch);
            return branch;
        }
        return "";
    }

    public String transATOMIC(SyntaxTreeNode atomic) throws IOException {
        List<SyntaxTreeNode> children = atomic.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("VNAME")) { //Case 1: ATOMIC -> VNAME
            String vname = transVNAME(next);
            atomic.setIntermediateCode(vname);
            return vname;
        } else if (next.getSymb().equals("CONST")) { //Case 2: ATOMIC -> CONST
            String cons = transCONST(next);
            atomic.setIntermediateCode(cons);
            return cons;
        }
        return "";
    }
    
    public String transATOMIC(SyntaxTreeNode atomic, String place) throws IOException {
        List<SyntaxTreeNode> children = atomic.getChildren();
        SyntaxTreeNode next = children.get(0);
        if(next.getSymb().equals("VNAME")) { //Case 1: ATOMIC -> VNAME
            String vname = transVNAME(next, place);
            atomic.setIntermediateCode(vname);
            return vname;
        } else if (next.getSymb().equals("CONST")) { //Case 2: ATOMIC -> CONST
            String cons = transCONST(next, place);
            atomic.setIntermediateCode(cons);
            return cons;
        }
        return "";
    }

    public String transCONST(SyntaxTreeNode cons) throws IOException {
        List<SyntaxTreeNode> children = cons.getChildren();
        SyntaxTreeNode next = children.get(0);
        //Case 1: CONST -> TokenN  && Case 2: CONST -> TokenT
        //adding to the syntax tree
        cons.setIntermediateCode(next.getTerminalWord());
        return next.getTerminalWord();
    }

    public String transCONST(SyntaxTreeNode cons, String place) throws IOException {
        List<SyntaxTreeNode> children = cons.getChildren();
        SyntaxTreeNode next = children.get(0);
        //Case 1: CONST -> TokenN  && Case 2: CONST -> TokenT
        //adding to the syntax tree
        cons.setIntermediateCode("\n " + place + " = " + next.getTerminalWord()  + " ");
        return "\n " + place + " = " + next.getTerminalWord()  + " ";
    }

    public String transASSIGN(SyntaxTreeNode assign) throws IOException {
        List<SyntaxTreeNode> children = assign.getChildren();
        SyntaxTreeNode symbol = children.get(1);
        
        if (symbol.getTerminal()!=null && symbol.getTerminal().contains("<WORD>&lt;</WORD>")) { // ASSIGN -> VNAME < input
            String vname = transVNAME(children.get(0));
            assign.setIntermediateCode("INPUT " + vname);
            return "INPUT " + vname;
        } else if (symbol.getTerminal() != null && symbol.getTerminal().contains("<WORD>=</WORD>")) { //Case 2: ASSIGN -> VNAME := TERM
            String place = newVar();
            String term = transTERM(children.get(2), place);
            String vname =transVNAME(children.get(0));
            assign.setIntermediateCode(term + "\n" + vname + " = " + place);
            return term + "\n" + vname + " = " + place;
        }
        return "";
    }

    public String transTERM(SyntaxTreeNode term, String place) throws IOException {
        List<SyntaxTreeNode> children = term.getChildren();
        SyntaxTreeNode next = children.get(0);
        if(next.getSymb().equals("ATOMIC")) { //Case 1: TERM -> ATOMIC
            String atomic = transATOMIC(next, place);
            term.setIntermediateCode(atomic);
            return atomic;
        }else if(next.getSymb().equals("CALL")) { //Case 2: TERM -> CALL
            String call = transCALL(next, place);
            term.setIntermediateCode(call);
            return call;
        } else if (next.getSymb().equals("OP")) { //Case 3: TERM -> OP
            String op = transOP(next, place);
            term.setIntermediateCode(op);
            return op;
        }
        return "";
    }

    public String transCALL(SyntaxTreeNode call) throws IOException {
        // Call -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
        String fname = transFNAME(call.getChildren().get(0));
        String atomic1 = transATOMIC(call.getChildren().get(2));
        String atomic2 = transATOMIC(call.getChildren().get(4));
        String atomic3 = transATOMIC(call.getChildren().get(6));
        
        //adding to the syntax tree
        call.setIntermediateCode("CALL " + fname + "(" + atomic1 + "," + atomic2 + "," + atomic3 + ")");
        return "CALL " + fname + "(" + atomic1 + "," + atomic2 + "," + atomic3 + ")";
    }

    public String transCALL(SyntaxTreeNode call, String place) throws IOException {
        // Call -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
        String fname = transFNAME(call.getChildren().get(0));
        String atomic1 = transATOMIC(call.getChildren().get(2));
        String atomic2 = transATOMIC(call.getChildren().get(4));
        String atomic3 = transATOMIC(call.getChildren().get(6));
        
        //adding to the syntax tree
        call.setIntermediateCode("CALL " + fname + "(" + atomic1 + "," + atomic2 + "," + atomic3 + ")");
        return "CALL " + fname + "(" + atomic1 + "," + atomic2 + "," + atomic3 + ")";
    }

    public String transOP(SyntaxTreeNode op, String place) throws IOException {
        List<SyntaxTreeNode> children = op.getChildren();
        SyntaxTreeNode next = children.get(0);

        if (next.getSymb().equals("UNOP")) { //Case 1: OP -> UNOP ( ARG )
            String place1 = newVar();
            String arg =transARG(children.get(2), place1);
            outputFile.write(place);
            outputFile.write(" = ");
            String unop = transUNOP(next);
            outputFile.write(place1);
            //adding to the syntax tree
            op.setIntermediateCode(arg + place + " = " + unop + " " + place1 + " ");
            return arg + place + " = " + unop + " " + place1 + " ";
           
        } else if (next.getSymb().equals("BINOP")) { //Case 2: OP -> BINOP ( ARG , ARG )
            String place1 = newVar();
            String place2 = newVar();
            String arg1 = transARG(children.get(2), place1);
            String arg2 = transARG(children.get(4), place2);
            String binop = transBINOP(next);
            //adding to the syntax tree
            op.setIntermediateCode(arg1 + " " + arg2 + "\n " + place + " = " + place1 + " " + binop + " " + place2);
            return arg1 + " " + arg2 + "\n " + place + " = " + place1 + " " + binop + " " + place2;
        }
        return "";
    }

    public String transARG(SyntaxTreeNode arg, String place) throws IOException {
        List<SyntaxTreeNode> children = arg.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("ATOMIC")) { //Case 1: ARG -> ATOMIC
            String atomic = transATOMIC(next, place);
            //adding to the syntax tree
            arg.setIntermediateCode(atomic);
            return atomic;
        } else if (next.getSymb().equals("OP")) { //Case 2: ARG -> OP
            String op = transOP(next, place);
            //adding to the syntax tree
            arg.setIntermediateCode(op);
            return op;
        }
        return "";
    }

    public String transUNOP(SyntaxTreeNode unop) throws IOException {
        List<SyntaxTreeNode> children = unop.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>not</WORD>")) { //Case 1: UNOP -> not
            //adding to the syntax tree
            unop.setIntermediateCode("NOT");
            return "NOT";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>sqrt</WORD>")) { //Case 2: UNOP -> sqrt
            //adding to the syntax tree
            unop.setIntermediateCode("SQR");
            return "SQR";
        }
        return "";
    }

    public String transBINOP(SyntaxTreeNode binop) throws IOException {
        List<SyntaxTreeNode> children = binop.getChildren();
        SyntaxTreeNode next = children.get(0);

        if (next.getTerminal() != null && next.getTerminal().contains("<WORD>or</WORD>")) { //Case 1: BINOP -> or
            //adding to the syntax tree
            binop.setIntermediateCode(" OR ");
            return " OR ";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>and</WORD>")) { //Case 2: BINOP -> and
            //adding to the syntax tree
            binop.setIntermediateCode(" AND ");
            return " AND ";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>eq</WORD>")) { //Case 3: BINOP -> eq
            //adding to the syntax tree
            binop.setIntermediateCode(" = ");
            return " = ";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>grt</WORD>")) { //Case 4: BINOP -> grt
            //adding to the syntax tree
            binop.setIntermediateCode(" > ");
            return " > ";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>add</WORD>")) { //Case 5: BINOP -> add
            //adding to the syntax tree
            binop.setIntermediateCode(" + ");
            return " + ";
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>sub</WORD>")) { //Case 6: BINOP -> sub
            //adding to the syntax tree
            binop.setIntermediateCode(" - ");
            return " - ";
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>mul</WORD>")) { //Case 7: BINOP -> mul
            //adding to the syntax tree
            binop.setIntermediateCode(" * ");
            return " * ";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>div</WORD>")) { //Case 8: BINOP -> div
            outputFile.write(" / ");
            //adding to the syntax tree
            binop.setIntermediateCode(" / ");
            return " / ";
        }
        return "";
    }
    

    public String transBRANCH(SyntaxTreeNode branch) throws IOException {
        //NOT SURE ABOUT THIS
        //BRANCH->if COND then ALGO else ALGO   
        List<SyntaxTreeNode> children = branch.getChildren();
        SyntaxTreeNode cond = children.get(1);
        SyntaxTreeNode next = cond.getChildren().get(0);

        String label1 = newLabel();
        String label2 = newLabel();
        String label3 = newLabel();

        String condCode;
        
        if (next.getSymb().equals("SIMPLE")) {//CASE 1 : COND -> SIMPLE
            condCode = transSIMPLE(children.get(2), label1, label2);

        } else if (next.getSymb().equals("COMPOSIT")) { //CASE 2: COND -> COMPOSIT
            condCode = transCOMPOSIT(children.get(2), label1, label2, "");
        } else {
            condCode = "";
        }
        
        String algo1 = transALGO(children.get(4));
        String algo2 = transALGO(children.get(6));
        //adding to the syntax tree
        branch.setIntermediateCode(condCode + "\nLABEL" + label1 + "\n" + algo1 + "\nGOTO " + label3 + "\nLABEL" + label2 + "\n" + algo2 + "\nLABEL" + label3);
        return condCode + "\nLABEL" + label1 + "\n" + algo1 + "\nGOTO " + label3 + "\nLABEL" + label2 + "\n" + algo2 + "\nLABEL" + label3;
    }

    public String transSIMPLE(SyntaxTreeNode simple, String labelt, String labelf) throws IOException {
        //SIMPLE->BINOP ( ATOMIC , ATOMIC )
        List<SyntaxTreeNode> children = simple.getChildren();
        String t1 = newVar();
        String t2 = newVar();
        String arg1 = transARG(children.get(2), t1);
        String arg2 = transARG(children.get(4), t2);
        String op = transOP(children.get(0), t2);

        //adding to the syntax tree
        simple.setIntermediateCode(arg1 + arg2 + "IF " + t1 + " " + op + " " + t2 + " THEN " + labelt + " ELSE " + labelf);
        return arg1 + arg2 + "IF " + t1 + " " + op + " " + t2 + " THEN " + labelt + " ELSE " + labelf;
    }

    public String transCOMPOSIT(SyntaxTreeNode composit, String labelt, String labelf, String place) throws IOException {
        // NOT SURE ABOUT THIS IMPLEMENTATION
        List<SyntaxTreeNode> children = composit.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("BINOP") ) { //Case 1: COMPOSIT -> BINOP ( SIMPLE , SIMPLE )
            String t1 = newVar();
            String t2 = newVar();
            String simple1 = transSIMPLE(children.get(2), t1, t2);
            String simple2 = transSIMPLE(children.get(4), t1, t2);
            String binop = transBINOP(children.get(0));
            //adding to the syntax tree
            composit.setIntermediateCode(simple1 + simple2 + "IF " + t1 + " " + binop + " " + t2 + " THEN " + labelt + " ELSE " + labelf);
            return simple1 + simple2 + "IF " + t1 + " " + t2 + " THEN " + labelt + " ELSE " + labelf;
        } else if (next.getSymb().equals("UNOP")) {//Case 2: COMPOSIT -> UNOP ( SIMPLE )
            String place1 = newVar();
            String simple = transSIMPLE(children.get(2), labelt, labelf);
            String unop = transUNOP(next);
            //adding to the syntax tree
            composit.setIntermediateCode(simple + place + " = " + unop + " " + place1 + " ");
            return simple + place + " = " + unop + " " + place1 + " ";
        }
        return "";
    }

    public String transFNAME(SyntaxTreeNode fname) throws IOException {
        SyntaxTreeNode next = fname.getChildren().get(0);
        String newName = SymbolTableAccessor.lookupVariable(next.getTerminalWord()).getName();
        //adding to the syntax tree
        fname.setIntermediateCode(newName);
        return newName;
    }


    public String transFUNCTIONS(SyntaxTreeNode functions) throws IOException {
        List<SyntaxTreeNode> children = functions.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!= null && next.getTerminal().isEmpty()) { //Case 1: FUNCTIONS -> 
            //adding to the syntax tree
            functions.setIntermediateCode("\nREM END");
            return "\nREM END";
        } else if (next.getSymb().equals("DECL")) { //Case 2: FUNCTIONS -> DECL FUNCTIONS
            String decl = transDECL(next);
            String function = transFUNCTIONS(children.get(1));
            //adding to the syntax tree
            functions.setIntermediateCode(decl + "\nSTOP " + function);
            return decl + "\nSTOP " + function;
        }
        return "";
    }

    public String transDECL(SyntaxTreeNode decl) throws IOException {
        //DECL->HEADER BODY
        List<SyntaxTreeNode> children = decl.getChildren();
        String header = transHEADER(children.get(0));
        String body = transBODY(children.get(1));
        //adding to the syntax tree
        decl.setIntermediateCode(header + body);
        return header + body;
    }

    public String transHEADER(SyntaxTreeNode header) throws IOException {
        //HEADER->FTYP FNAME ( VNAME , VNAME , VNAME )
        List<SyntaxTreeNode> children = header.getChildren();
        String ftype = children.get(0).getTerminalWord();
        String fname = transFNAME(children.get(1));
        String vname1 = transVNAME(children.get(3));
        String vname2 = transVNAME(children.get(5));
        String vname3 = transVNAME(children.get(7));
        //adding to the syntax tree
        header.setIntermediateCode(ftype + " " + fname + " (" + vname1 + "," + vname2 + "," + vname3 + ")");
        return ftype + " " + fname + " (" + vname1 + "," + vname2 + "," + vname3 + ")";

    }
    
    public String transBODY(SyntaxTreeNode body) throws IOException {
        //BODY->PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
        List<SyntaxTreeNode> children = body.getChildren();
        String prolog = transPROLOG(children.get(0));
        String algo = transALGO(children.get(2));
        String epilog = transEPILOG(children.get(3));
        String subfuncs = transSUBFUNCS(children.get(4));
        //adding to the syntax tree
        body.setIntermediateCode(prolog + algo + epilog + subfuncs);
        return prolog + algo + epilog + subfuncs;
    }  

    public String transPROLOG(SyntaxTreeNode  prolog) throws IOException {
        //PROLOG->{
        //adding to the syntax tree
        prolog.setIntermediateCode("\nREM BEGIN");
        return "\nREM BEGIN";
    }

    public String transEPILOG(SyntaxTreeNode epilog) throws IOException {
        //EPILOG->}
        //adding to the syntax tree
        epilog.setIntermediateCode("\nREM END");
        return "\nREM END";
    }

    public String transSUBFUNCS(SyntaxTreeNode subfuncs) throws IOException {
        //SUBFUNCS->FUNCTIONS
        List<SyntaxTreeNode> children = subfuncs.getChildren();
        String functions = transFUNCTIONS(children.get(0));
        //adding to the syntax tree
        subfuncs.setIntermediateCode(functions);
        return functions;
    }

    private String newVar() {
        return "var" + varCounter++;
    }

    private String newLabel() {
        return "label" + labelCounter++;
    }

    public static void main(String[] args) {
        //Testing the code generator
        try {
            intermeridateCodeGeneration codeGen = new intermeridateCodeGeneration("src/parser2/output/output2.xml");
            codeGen.trans();
            codeGen.outputFile.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
