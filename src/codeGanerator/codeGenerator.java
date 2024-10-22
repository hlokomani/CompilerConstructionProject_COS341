package codeGanerator;

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

public class codeGenerator {
    private TreeCrawler treeCrawler;
    private FileWriter outputFile;
    private int varCounter, labelCounter;
    private String inputFilePath, outputFilePath;

    public codeGenerator(String xmlFilePath) throws Exception {
        //Initialize symbol table 
        treeCrawler = new TreeCrawler(xmlFilePath);
        varCounter = 0;
        labelCounter = 0;
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(xmlFilePath);
        semanticAnalyzer.analyze();
        //Create text file to write the translated code
        //extracting the output name from the xml file name from src/parser2/output1.xml
        String outputName = xmlFilePath.substring(xmlFilePath.lastIndexOf("/") + 1, xmlFilePath.lastIndexOf("."));
        String outputFilePath = "output/" + outputName + "Temp.txt";
        File output = new File(outputFilePath);
        output.createNewFile();
        output.setWritable(true);
        outputFile = new FileWriter(output, true);
        this.inputFilePath = outputFilePath;
        this.outputFilePath = "output/" + outputName + ".txt";  
    }
   
    public void translate() {
        try {
            translatePROG(treeCrawler.getNext());
            outputFile.close();
            addLineNumbers(inputFilePath, outputFilePath);
            //delete inputFilePath
            File file = new File(inputFilePath);
            file.delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void addLineNumbers(String inputFilePath, String outputFilePath) {
        // Check if input file exists and is readable
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            System.err.println("Input file does not exist or is not a file: " + inputFilePath);
            return;
        }

        // Check if we can write to the output file
        File outputFile = new File(outputFilePath);
        try {
            if (outputFile.exists() && !outputFile.canWrite()) {
                System.err.println("Cannot write to output file: " + outputFilePath);
                return;
            }
        } catch (SecurityException e) {
            System.err.println("Security exception when checking output file: " + e.getMessage());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath))) {

            String line;
            int lineNumber = 1;

            System.out.println("Starting to process the file...");

            // Extra check: Ensure the file isn't empty
            if (inputFile.length() == 0) {
                System.out.println("Input file is empty!");
                return;
            }

            // Reading and processing each line
            while ((line = reader.readLine()) != null) {
                // Debugging: Print the raw line content for each read
                System.out.println("Raw line read: '" + line + "' (length: " + line.length() + ")");

                // Skip empty lines
                if (!line.trim().isEmpty()) {
                    System.out.println("Writing line: " + lineNumber + ": " + line);
                    writer.println(lineNumber + "\t" + line);
                    lineNumber++;
                } else {
                    System.out.println("Skipped empty/whitespace line.");
                }
            }

            System.out.println("Finished writing to the file.");

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
    
    public void translatePROG(SyntaxTreeNode prog) throws IOException {
        //PROG->main GLOBVARS ALGO FUNCTIONS
        translateALGO(prog.getChildren().get(2));
        translateFUNCTIONS(prog.getChildren().get(3));
    }
    
    public void translateGLOBVARS() {
        //Not translated
        //Case 1: GLOBVARS ->
        //Case 2: GLOBVARS -> VTYP VNAME , GLOBVARS
    }

    public void translateVTYP() {
        //Not translated
        //Case 1: VTYP -> num
        //Case 2: VTYP -> text
    }

    public void translateVNAME(SyntaxTreeNode vname) throws IOException {
        SyntaxTreeNode next = vname.getChildren().get(0);
        String newName = SymbolTableAccessor.lookupVariable(next.getTerminalWord()).getName();
        outputFile.write(newName);
    }

    public void translateVNAME(SyntaxTreeNode vname, String place) throws IOException {
        SyntaxTreeNode next = vname.getChildren().get(0);
        String newName = SymbolTableAccessor.lookupVariable(next.getTerminalWord()).getName();
        outputFile.write("\nLET " + place  + " = " + newName+ " ");
    }

    public void translateALGO(SyntaxTreeNode algo) throws IOException {
        // ALGO -> begin INSTRUC end
        translateINSTRUC(algo.getChildren().get(1));
    }

    public void translateINSTRUC(SyntaxTreeNode instruc) throws IOException {
        System.out.println("In instruct");
        List<SyntaxTreeNode> children = instruc.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!=null && next.getTerminal().isEmpty()) { //Case 1: INSTRUC -> 
                outputFile.write("\nREM END");
        } else if(next.getSymb().equals("COMMAND")) { //Case 2: INSTRUC -> COMMAND ; INSTRUC
            translateCOMMAND(next);
            translateINSTRUC(children.get(2));
        } 
    }

    public void translateCOMMAND(SyntaxTreeNode command) throws IOException {
        System.out.println("In command");
        List<SyntaxTreeNode> children = command.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>skip</WORD>")) { //Case 1: COMMAND -> skip
            outputFile.write("\nREM DO NOTHING");
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>halt</WORD>")) { //Case 2: COMMAND -> halt
            outputFile.write("\nSTOP");
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>print</WORD>")) {//Case 3: COMMAND -> print ATOMIC
            outputFile.write("\nPRINT ");
            translateATOMIC(children.get(1));
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>return</WORD>")) {//Case 4: COMMAND -> return ATOMIC
            //TODO: Implement return
        } else if (next.getSymb().equals("ASSIGN")) {//Case 5: COMMAND -> ASSIGN
           translateASSIGN(children.get(0));
        } else if (next.getSymb().equals("CALL")) { //Case 6: COMMAND -> CALL
            translateCALL(children.get(0));
        } else if (next.getSymb().equals("BRANCH")) { //Case 7: COMMAND -> BRANCH
            translateBRANCH(children.get(0));
        }
    }

    public void translateATOMIC(SyntaxTreeNode atomic) throws IOException {
        List<SyntaxTreeNode> children = atomic.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("VNAME")) { //Case 1: ATOMIC -> VNAME
            translateVNAME(next);
        } else if (next.getSymb().equals("CONST")) { //Case 2: ATOMIC -> CONST
            translateCONST(next);
        }
    }
    
    public void translateATOMIC(SyntaxTreeNode atomic, String place) throws IOException {
        List<SyntaxTreeNode> children = atomic.getChildren();
        SyntaxTreeNode next = children.get(0);
        if(next.getSymb().equals("VNAME")) { //Case 1: ATOMIC -> VNAME
            translateVNAME(next, place);
        } else if(next.getSymb().equals("CONST")) { //Case 2: ATOMIC -> CONST
            translateCONST(next, place);
        } 
    }

    public void translateCONST(SyntaxTreeNode cons) throws IOException {
        List<SyntaxTreeNode> children = cons.getChildren();
        SyntaxTreeNode next = children.get(0);
        //Case 1: CONST -> TokenN  && Case 2: CONST -> TokenT
        outputFile.write(next.getTerminalWord());
    }

    public void translateCONST(SyntaxTreeNode cons, String place) throws IOException {
        List<SyntaxTreeNode> children = cons.getChildren();
        SyntaxTreeNode next = children.get(0);
        //Case 1: CONST -> TokenN  && Case 2: CONST -> TokenT
        outputFile.write("\nLET " + place + " = " + next.getTerminalWord()  + " ");
    }

    public void translateASSIGN(SyntaxTreeNode assign) throws IOException {
        System.out.println("In assign");
        List<SyntaxTreeNode> children = assign.getChildren();
        SyntaxTreeNode symbol = children.get(1);
        
        if (symbol.getTerminal()!=null && symbol.getTerminal().contains("<WORD>&lt;</WORD>")) { // ASSIGN -> VNAME < input
            outputFile.write("INPUT ");
            translateVNAME(children.get(0));
        } else if (symbol.getTerminal() != null && symbol.getTerminal().contains("<WORD>=</WORD>")) { //Case 2: ASSIGN -> VNAME := TERM
            String place = newVar();
            translateTERM(children.get(2), place);
            outputFile.write("\nLET ");
            translateVNAME(children.get(0));
            outputFile.write(" = " + place);
        }
    }

    public void translateTERM(SyntaxTreeNode term, String place) throws IOException {
        List<SyntaxTreeNode> children = term.getChildren();
        SyntaxTreeNode next = children.get(0);
        if(next.getSymb().equals("ATOMIC")) { //Case 1: TERM -> ATOMIC
            translateATOMIC(next, place);
        }else if(next.getSymb().equals("CALL")) { //Case 2: TERM -> CALL
            translateCALL(next, place);
        } else if (next.getSymb().equals("OP")) { //Case 3: TERM -> OP
            translateOP(next, place);
        }
    }

    public void translateCALL(SyntaxTreeNode call) throws IOException {
        // Call -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
        outputFile.write("CALL ");
        translateFNAME(call.getChildren().get(0));
        outputFile.write("(");
        translateATOMIC(call.getChildren().get(2));
        outputFile.write(",");
        translateATOMIC(call.getChildren().get(4));
        outputFile.write(",");
        translateATOMIC(call.getChildren().get(6));
        outputFile.write(")");
    }

    public void translateCALL(SyntaxTreeNode call, String place) throws IOException {
        // Call -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
        outputFile.write("\nLET" + place + " = CALL ");
        translateFNAME(call.getChildren().get(0));
        outputFile.write("(");
        translateATOMIC(call.getChildren().get(2));
        outputFile.write(",");
        translateATOMIC(call.getChildren().get(4));
        outputFile.write(",");
        translateATOMIC(call.getChildren().get(6));
        outputFile.write(")");
    }

    public void translateOP(SyntaxTreeNode op, String place) throws IOException {
        List<SyntaxTreeNode> children = op.getChildren();
        SyntaxTreeNode next = children.get(0);

        if (next.getSymb().equals("UNOP")) { //Case 1: OP -> UNOP ( ARG )
            String place1 = newVar();
            translateARG(children.get(2), place1);
            outputFile.write(place);
            outputFile.write(" = ");
            translateUNOP(next);
            outputFile.write(place1);
           
        } else if (next.getSymb().equals("BINOP")) { //Case 2: OP -> BINOP ( ARG , ARG )
            String place1 = newVar();
            String place2 = newVar();
            translateARG(children.get(2), place1);
            translateARG(children.get(4), place2);
            outputFile.write("\nLET " + place + " = " + place1);
            translateBINOP(next);
            outputFile.write(place2);
        }
    }

    public void translateARG(SyntaxTreeNode arg, String place) throws IOException {
        List<SyntaxTreeNode> children = arg.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("ATOMIC")) { //Case 1: ARG -> ATOMIC
            translateATOMIC(next, place);
        } else if (next.getSymb().equals("OP")) { //Case 2: ARG -> OP
            translateOP(next, place);
        }
    }

    public void translateUNOP(SyntaxTreeNode unop) throws IOException {
        List<SyntaxTreeNode> children = unop.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>not</WORD>")) { //Case 1: UNOP -> not
            //TODO: figure out how to implement not
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>sqrt</WORD>")) { //Case 2: UNOP -> sqrt
            outputFile.write(" SQR ");
        }
    }

    public void translateBINOP(SyntaxTreeNode binop) throws IOException {
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

    public void translateBRANCH(SyntaxTreeNode branch) throws IOException {
        //BRANCH->if COND then ALGO else ALGO   

        //NOT SURE ABOUT THIS IMPLEMENTATION

        List<SyntaxTreeNode> children = branch.getChildren();
        SyntaxTreeNode cond = children.get(1);
        SyntaxTreeNode next = cond.getChildren().get(0);

        String label1 = newLabel();
        String label2 = newLabel();
        String label3 = newLabel();

        
        if (next.getSymb().equals("SIMPLE")) {//CASE 1 : COND -> SIMPLE
            translateSIMPLE(children.get(2), label1, label2);

        } else if (next.getSymb().equals("COMPOSIT")) { //CASE 2: COND -> COMPOSIT
            translateCOMPOSIT(children.get(2), label1, label2, "");
        }
        
        outputFile.write("\nLABEL" + label1);
        translateALGO(children.get(4));
        outputFile.write("\nGOTO " + label3);
        outputFile.write("\nLABEL" + label2);
        translateALGO(children.get(6));
        outputFile.write("\nLABEL" + label3);

    }

    public void translateCOND(SyntaxTreeNode cond) throws IOException {
        //NOT NEEDED
    }

    public void translateSIMPLE(SyntaxTreeNode simple, String labelt, String labelf) throws IOException {
        //SIMPLE->BINOP ( ATOMIC , ATOMIC )
        List<SyntaxTreeNode> children = simple.getChildren();
        String t1 = newVar();
        String t2 = newVar();

        translateARG(children.get(2), t1);
        translateARG(children.get(4), t2);
        outputFile.write("IF " + t1);
        translateOP(children.get(0), t2);
        outputFile.write(t2 + " THEN " + labelt + " ELSE " + labelf);
    }

    public void translateCOMPOSIT(SyntaxTreeNode composit, String labelt, String labelf, String place) throws IOException {
        // NOT SURE ABOUT THIS IMPLEMENTATION
        List<SyntaxTreeNode> children = composit.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("BINOP") ) { //Case 1: COMPOSIT -> BINOP ( SIMPLE , SIMPLE )
            String t1 = newVar();
            String t2 = newVar();
            translateSIMPLE(children.get(2), t1, t2);
            translateSIMPLE(children.get(4), t1, t2);
            outputFile.write("IF " + t1);
            translateOP(children.get(0), t2);
            outputFile.write(t2 +" THEN " + labelt + " ELSE " + labelf);
        } else if (next.getSymb().equals("UNOP")) {//Case 2: COMPOSIT -> UNOP ( SIMPLE )
            String place1 = newVar();
            translateSIMPLE(children.get(2), labelt, labelf);
            outputFile.write(place);
            outputFile.write(" = ");
            translateUNOP(next);
            outputFile.write(place1);
        } 
    }

    public void translateFNAME(SyntaxTreeNode fname) throws IOException {
        SyntaxTreeNode next = fname.getChildren().get(0);
        String newName = SymbolTableAccessor.lookupVariable(next.getTerminalWord()).getName();
        outputFile.write(newName);
    }


    public void translateFUNCTIONS(SyntaxTreeNode functions) throws IOException {
        List<SyntaxTreeNode> children = functions.getChildren();
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!= null && next.getTerminal().isEmpty()) { //Case 1: FUNCTIONS -> 
            outputFile.write("\nREM END");
        } else if(next.getSymb().equals("DECL")) { //Case 2: FUNCTIONS -> DECL FUNCTIONS
            translateDECL(next);
            outputFile.write("\nSTOP ");
            translateFUNCTIONS(children.get(1));
        } 
    }

    public void translateDECL(SyntaxTreeNode decl) {
        //DECL->HEADER BODY
        List<SyntaxTreeNode> children = decl.getChildren();
        //TODO: Implement this
    }

    public void translateHEADER(SyntaxTreeNode header) {
        //HEADER->FTYP FNAME ( VNAME , VNAME , VNAME )
        List<SyntaxTreeNode> children = header.getChildren();
        //TODO: Implement this
    }

    public void translateFTYP(SyntaxTreeNode ftyp) {
        //Not translated
    }
    
    public void translateBODY(SyntaxTreeNode body) throws IOException {
        //BODY->PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
        List<SyntaxTreeNode> children = body.getChildren();
        translatePROLOG(children.get(0));
        translateALGO(children.get(2));
        translateEPILOG(children.get(3));
        translateSUBFUNCS(children.get(4));
    }  

    public void translatePROLOG(SyntaxTreeNode  prolog) throws IOException {
        //PROLOG->{
        outputFile.write("\nREM BEGIN");
    }

    public void translateEPILOG(SyntaxTreeNode epilog) throws IOException {
        //EPILOG->}
        outputFile.write("\nREM END");
    }

    public void translateLOCVARS(SyntaxTreeNode locvars) {
        //Not translated
    }

    public void translateSUBFUNCS(SyntaxTreeNode subfuncs) throws IOException {
        //SUBFUNCS->FUNCTIONS
        List<SyntaxTreeNode> children = subfuncs.getChildren();
        translateFUNCTIONS(children.get(0));
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
            codeGenerator codeGen = new codeGenerator("src/parser2/output/output2.xml");
            codeGen.translate();
            codeGen.outputFile.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
