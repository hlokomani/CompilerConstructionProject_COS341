package targetCodeGeneration;
import java.io.*;
import java.util.List;
import intermediateCodeGeneration.intermediateCodeGeneration;
import parser2.SyntaxTreeNode;
import semanticanalyzer.SemanticAnalyzer;
import semanticanalyzer.SymbolTableAccessor;
import typeChecker.TreeCrawler;

public class targetCodeGenerator {
    private SyntaxTreeNode root;
    private FileWriter outputFile;

    public targetCodeGenerator(String xmlFilePath, SyntaxTreeNode root) throws Exception {
        this.root = root;
        String outputName = xmlFilePath.substring(xmlFilePath.lastIndexOf("/") + 1, xmlFilePath.lastIndexOf("."));
        String outputFilePath = "output/" + outputName + ".txt";
        File output = new File(outputFilePath);
        output.createNewFile();
        output.setWritable(true);
        outputFile = new FileWriter(output, true);  
    }
   
    public void trans() {
        try {
            //Adding the declaration for the runtime stack
            int n = 50;
            outputFile.write("0 \t DIM M(7," + n + ")\n");
            
            transPROG(root);
            outputFile.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void transPROG(SyntaxTreeNode prog) throws IOException {
        //PROG->main GLOBVARS ALGO FUNCTIONS
        transALGO(prog.getChildren().get(2));
        transFUNCTIONS(prog.getChildren().get(3));
    }

    public void transVNAME(SyntaxTreeNode vname) throws IOException {
        SyntaxTreeNode next = vname.getChildren().get(0);
        String newName = SymbolTableAccessor.lookupVariable(next.getTerminalWord()).getName();
        outputFile.write(newName);
    }

    public void transVNAME(SyntaxTreeNode vname, String place) throws IOException {
        SyntaxTreeNode next = vname.getChildren().get(0);
        String newName = SymbolTableAccessor.lookupVariable(next.getTerminalWord()).getName();
        outputFile.write("\nLET " + place  + " = " + newName+ " ");
    }

    public void transALGO(SyntaxTreeNode algo) throws IOException {
        // ALGO -> begin INSTRUC end
        transINSTRUC(algo.getChildren().get(1));
    }

    public void transINSTRUC(SyntaxTreeNode instruc) throws IOException {
        System.out.println("In instruct");
        List<SyntaxTreeNode> children = instruc.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!=null && next.getTerminal().isEmpty()) { //Case 1: INSTRUC -> 
                outputFile.write("\nREM END");
        } else if(next.getSymb().equals("COMMAND")) { //Case 2: INSTRUC -> COMMAND ; INSTRUC
            transCOMMAND(next);
            transINSTRUC(children.get(2));
        } 
    }

    public void transCOMMAND(SyntaxTreeNode command) throws IOException {
        System.out.println("In command");
        List<SyntaxTreeNode> children = command.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>skip</WORD>")) { //Case 1: COMMAND -> skip
            outputFile.write("\nREM DO NOTHING");
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>halt</WORD>")) { //Case 2: COMMAND -> halt
            outputFile.write("\nSTOP");
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>print</WORD>")) {//Case 3: COMMAND -> print ATOMIC
            outputFile.write("\nPRINT ");
            transATOMIC(children.get(1));
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>return</WORD>")) {//Case 4: COMMAND -> return ATOMIC
            //TODO: Implement return
        } else if (next.getSymb().equals("ASSIGN")) {//Case 5: COMMAND -> ASSIGN
           transASSIGN(children.get(0));
        } else if (next.getSymb().equals("CALL")) { //Case 6: COMMAND -> CALL
            transCALL(children.get(0));
        } else if (next.getSymb().equals("BRANCH")) { //Case 7: COMMAND -> BRANCH
            transBRANCH(children.get(0));
        }
    }

    public void transATOMIC(SyntaxTreeNode atomic) throws IOException {
        List<SyntaxTreeNode> children = atomic.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("VNAME")) { //Case 1: ATOMIC -> VNAME
            transVNAME(next);
        } else if (next.getSymb().equals("CONST")) { //Case 2: ATOMIC -> CONST
            transCONST(next);
        }
    }
    
    public void transATOMIC(SyntaxTreeNode atomic, String place) throws IOException {
        List<SyntaxTreeNode> children = atomic.getChildren();
        SyntaxTreeNode next = children.get(0);
        if(next.getSymb().equals("VNAME")) { //Case 1: ATOMIC -> VNAME
            transVNAME(next, place);
        } else if(next.getSymb().equals("CONST")) { //Case 2: ATOMIC -> CONST
            transCONST(next, place);
        } 
    }

    public void transCONST(SyntaxTreeNode cons) throws IOException {
        List<SyntaxTreeNode> children = cons.getChildren();
        SyntaxTreeNode next = children.get(0);
        //Case 1: CONST -> TokenN  && Case 2: CONST -> TokenT
        outputFile.write(next.getTerminalWord());
    }

    public void transCONST(SyntaxTreeNode cons, String place) throws IOException {
        List<SyntaxTreeNode> children = cons.getChildren();
        SyntaxTreeNode next = children.get(0);
        //Case 1: CONST -> TokenN  && Case 2: CONST -> TokenT
        outputFile.write("\nLET " + place + " = " + next.getTerminalWord()  + " ");
    }

    public void transASSIGN(SyntaxTreeNode assign) throws IOException {
        System.out.println("In assign");
        List<SyntaxTreeNode> children = assign.getChildren();
        SyntaxTreeNode symbol = children.get(1);
        
        if (symbol.getTerminal()!=null && symbol.getTerminal().contains("<WORD>&lt;</WORD>")) { // ASSIGN -> VNAME < input
            outputFile.write("INPUT ");
            transVNAME(children.get(0));
        } else if (symbol.getTerminal() != null && symbol.getTerminal().contains("<WORD>=</WORD>")) { //Case 2: ASSIGN -> VNAME := TERM
            String place = newVar();
            transTERM(children.get(2), place);
            outputFile.write("\nLET ");
            transVNAME(children.get(0));
            outputFile.write(" = " + place);
        }
    }

    public void transTERM(SyntaxTreeNode term, String place) throws IOException {
        List<SyntaxTreeNode> children = term.getChildren();
        SyntaxTreeNode next = children.get(0);
        if(next.getSymb().equals("ATOMIC")) { //Case 1: TERM -> ATOMIC
            transATOMIC(next, place);
        }else if(next.getSymb().equals("CALL")) { //Case 2: TERM -> CALL
            transCALL(next, place);
        } else if (next.getSymb().equals("OP")) { //Case 3: TERM -> OP
            transOP(next, place);
        }
    }

    public void transCALL(SyntaxTreeNode call) throws IOException {
        // Call -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
        outputFile.write("CALL ");
        transFNAME(call.getChildren().get(0));
        outputFile.write("(");
        transATOMIC(call.getChildren().get(2));
        outputFile.write(",");
        transATOMIC(call.getChildren().get(4));
        outputFile.write(",");
        transATOMIC(call.getChildren().get(6));
        outputFile.write(")");
    }

    public void transCALL(SyntaxTreeNode call, String place) throws IOException {
        // Call -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
        outputFile.write("\nLET" + place + " = CALL ");
        transFNAME(call.getChildren().get(0));
        outputFile.write("(");
        transATOMIC(call.getChildren().get(2));
        outputFile.write(",");
        transATOMIC(call.getChildren().get(4));
        outputFile.write(",");
        transATOMIC(call.getChildren().get(6));
        outputFile.write(")");
    }

    public void transOP(SyntaxTreeNode op, String place) throws IOException {
        List<SyntaxTreeNode> children = op.getChildren();
        SyntaxTreeNode next = children.get(0);

        if (next.getSymb().equals("UNOP")) { //Case 1: OP -> UNOP ( ARG )
            String place1 = newVar();
            transARG(children.get(2), place1);
            outputFile.write(place);
            outputFile.write(" = ");
            transUNOP(next);
            outputFile.write(place1);
           
        } else if (next.getSymb().equals("BINOP")) { //Case 2: OP -> BINOP ( ARG , ARG )
            String place1 = newVar();
            String place2 = newVar();
            transARG(children.get(2), place1);
            transARG(children.get(4), place2);
            outputFile.write("\nLET " + place + " = " + place1);
            transBINOP(next);
            outputFile.write(place2);
        }
    }

    public void transARG(SyntaxTreeNode arg, String place) throws IOException {
        List<SyntaxTreeNode> children = arg.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("ATOMIC")) { //Case 1: ARG -> ATOMIC
            transATOMIC(next, place);
        } else if (next.getSymb().equals("OP")) { //Case 2: ARG -> OP
            transOP(next, place);
        }
    }

    public void transUNOP(SyntaxTreeNode unop) throws IOException {
        List<SyntaxTreeNode> children = unop.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>not</WORD>")) { //Case 1: UNOP -> not
            //TODO: figure out how to implement not
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>sqrt</WORD>")) { //Case 2: UNOP -> sqrt
            outputFile.write(" SQR ");
        }
    }

    public void transBINOP(SyntaxTreeNode binop) throws IOException {
        List<SyntaxTreeNode> children = binop.getChildren();
        SyntaxTreeNode next = children.get(0);

        if (next.getTerminal() != null && next.getTerminal().contains("<WORD>or</WORD>")) { //Case 1: BINOP -> or
            //TODO: figure out how to implement or
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>and</WORD>")) { //Case 2: BINOP -> and
            //TODO: figure out how to implement and
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>eq</WORD>")) { //Case 3: BINOP -> eq
            outputFile.write(" = ");
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>grt</WORD>")) { //Case 4: BINOP -> grt
            outputFile.write(" > ");
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>add</WORD>")) { //Case 5: BINOP -> add
            outputFile.write(" + ");
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>sub</WORD>")) { //Case 6: BINOP -> sub
            outputFile.write(" - ");
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>mul</WORD>")) { //Case 7: BINOP -> mul
            outputFile.write(" * ");
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>div</WORD>")) { //Case 8: BINOP -> div
            outputFile.write(" / ");
        }
    }

    public void transBRANCH(SyntaxTreeNode branch) throws IOException {
        //BRANCH->if COND then ALGO else ALGO   

        //NOT SURE ABOUT THIS IMPLEMENTATION

        List<SyntaxTreeNode> children = branch.getChildren();
        SyntaxTreeNode cond = children.get(1);
        SyntaxTreeNode next = cond.getChildren().get(0);

        String label1 = newLabel();
        String label2 = newLabel();
        String label3 = newLabel();

        
        if (next.getSymb().equals("SIMPLE")) {//CASE 1 : COND -> SIMPLE
            transSIMPLE(children.get(2), label1, label2);

        } else if (next.getSymb().equals("COMPOSIT")) { //CASE 2: COND -> COMPOSIT
            transCOMPOSIT(children.get(2), label1, label2, "");
        }
        
        outputFile.write("\nLABEL" + label1);
        transALGO(children.get(4));
        outputFile.write("\nGOTO " + label3);
        outputFile.write("\nLABEL" + label2);
        transALGO(children.get(6));
        outputFile.write("\nLABEL" + label3);

    }

    public void transCOND(SyntaxTreeNode cond) throws IOException {
        //NOT NEEDED
    }

    public void transSIMPLE(SyntaxTreeNode simple, String labelt, String labelf) throws IOException {
        //SIMPLE->BINOP ( ATOMIC , ATOMIC )
        List<SyntaxTreeNode> children = simple.getChildren();
        String t1 = newVar();
        String t2 = newVar();

        transARG(children.get(2), t1);
        transARG(children.get(4), t2);
        outputFile.write("IF " + t1);
        transOP(children.get(0), t2);
        outputFile.write(t2 + " THEN " + labelt + " ELSE " + labelf);
    }

    public void transCOMPOSIT(SyntaxTreeNode composit, String labelt, String labelf, String place) throws IOException {
        // NOT SURE ABOUT THIS IMPLEMENTATION
        List<SyntaxTreeNode> children = composit.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("BINOP") ) { //Case 1: COMPOSIT -> BINOP ( SIMPLE , SIMPLE )
            String t1 = newVar();
            String t2 = newVar();
            transSIMPLE(children.get(2), t1, t2);
            transSIMPLE(children.get(4), t1, t2);
            outputFile.write("IF " + t1);
            transOP(children.get(0), t2);
            outputFile.write(t2 +" THEN " + labelt + " ELSE " + labelf);
        } else if (next.getSymb().equals("UNOP")) {//Case 2: COMPOSIT -> UNOP ( SIMPLE )
            String place1 = newVar();
            transSIMPLE(children.get(2), labelt, labelf);
            outputFile.write(place);
            outputFile.write(" = ");
            transUNOP(next);
            outputFile.write(place1);
        } 
    }

    public void transFNAME(SyntaxTreeNode fname) throws IOException {
        SyntaxTreeNode next = fname.getChildren().get(0);
        String newName = SymbolTableAccessor.lookupVariable(next.getTerminalWord()).getGeneratedName();
        outputFile.write(newName);
    }


    public void transFUNCTIONS(SyntaxTreeNode functions) throws IOException {
        List<SyntaxTreeNode> children = functions.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!= null && next.getTerminal().isEmpty()) { //Case 1: FUNCTIONS -> 
            outputFile.write("\nREM END");
        } else if(next.getSymb().equals("DECL")) { //Case 2: FUNCTIONS -> DECL FUNCTIONS
            transDECL(next);
            outputFile.write("\nSTOP ");
            transFUNCTIONS(children.get(1));
        } 
    }

    public void transDECL(SyntaxTreeNode decl) {
        //DECL->HEADER BODY
        List<SyntaxTreeNode> children = decl.getChildren();
        //TODO: Implement this
    }

    public void transHEADER(SyntaxTreeNode header) {
        //HEADER->FTYP FNAME ( VNAME , VNAME , VNAME )
        List<SyntaxTreeNode> children = header.getChildren();
        //TODO: Implement this
    }
    
    public void transBODY(SyntaxTreeNode body) throws IOException {
        //BODY->PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
        List<SyntaxTreeNode> children = body.getChildren();
        transPROLOG(children.get(0));
        transALGO(children.get(2));
        transEPILOG(children.get(3));
        transSUBFUNCS(children.get(4));
    }  

    public void transPROLOG(SyntaxTreeNode  prolog) throws IOException {
        //PROLOG->{
        outputFile.write("\nREM BEGIN");
    }

    public void transEPILOG(SyntaxTreeNode epilog) throws IOException {
        //EPILOG->}
        outputFile.write("\nREM END");
    }

    public void transSUBFUNCS(SyntaxTreeNode subfuncs) throws IOException {
        //SUBFUNCS->FUNCTIONS
        List<SyntaxTreeNode> children = subfuncs.getChildren();
        transFUNCTIONS(children.get(0));
    }

    public static void main(String[] args) {
        //Testing the code generator
        try {
            String xmlString = "src/parser2/output/output2.xml";
            intermediateCodeGeneration gen = new intermediateCodeGeneration(xmlString);
            targetCodeGenerator codeGen = new targetCodeGenerator(xmlString, gen.trans());
            codeGen.trans();
            codeGen.outputFile.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
