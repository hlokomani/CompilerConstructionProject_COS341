package typeChecker;

import java.util.HashMap;
import java.util.Map;

import semanticanalyzer.Symbol;
import semanticanalyzer.SymbolTableAccessor;
import parser2.SyntaxTreeNode;

public class typeChecker {
    private SymbolTableAccessor symbolTable; //Needs to be replaced by actual SymbolTable
    private TreeCrawler treeCrawler;

    
    public typeChecker(String xmlFilePath) throws Exception {
        //Initialize symbol table 
        treeCrawler = new TreeCrawler(xmlFilePath);
        symbolTable = new SymbolTableAccessor();       
    }

    public boolean typeCheck() throws Exception {
        System.out.println("Type checking started");
        return typeCheckPROG(treeCrawler.getNext());
    }

    private boolean typeCheckPROG(SyntaxTreeNode prog) throws Exception {
        System.out.println("Type checking PROG");
        System.out.println("prog: " + prog.toString());
        //PROG->main GLOBVARS ALGO FUNCTIONS
        boolean result = true;
        treeCrawler.getNext(); //getting "main" since it is not used in type checking
        result = result && typeCheckGLOBVARS(treeCrawler.getNext());
        result = result && typeCheckALGO(treeCrawler.getNext());
        result = result && typeCheckFUNCTIONS(treeCrawler.getNext());
        return result;
    }

    private boolean typeCheckGLOBVARS(SyntaxTreeNode globVars) {
        System.out.println("Type checking GLOBVARS");
        System.out.println("globVars: " + globVars.toString());

        SyntaxTreeNode next = treeCrawler.getNext();
        System.out.println("next: " + next.toString());
        if (next.getTerminal() != null && next.getTerminal().isEmpty()) {//Case 1: GLOBVARS ->
            System.out.println("GLOBVARS -> epsilon");
            return true;
        } else if (next.getSymb().equals("VTYP")) {//Case 2: GLOBVARS -> VTYP VNAME , GLOBVARS
            System.out.println("GLOBVARS -> VTYP VNAME , GLOBVARS");
            SyntaxTreeNode vtyp = next;
            String vtypType = typeOfVTYP(vtyp);
            SyntaxTreeNode vname = treeCrawler.getNext();
            String vnameType = this.typeOfVNAME(vname);

            Symbol id = SymbolTableAccessor.lookupFunction(vname.getTerminal());
            // symbolTable.put(T, id);//Linking the type to the variable in the table
            if (vnameType != vtypType) {
                System.out.println("Type checking failed for GLOBVARS -> VTYP VNAME , GLOBVARS");
                return false;
            }
            return typeCheckGLOBVARS(treeCrawler.getNext());
        } else {
            System.out.println("Type checking failed for GLOBVARS");
            return false;
        }
    }

    private String typeOfVTYP(SyntaxTreeNode vtyp) {
        System.out.println("Type of VTYP");
        System.out.println("vtyp: " + vtyp.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        System.out.println("next: " + next.toString());
        if (next.getTerminal().contains("<WORD>num</WORD>")) { //Case 1: VTYP -> num
            System.out.println("Returning n");
            return "n";
        } else if (next.getTerminal().contains("<WORD>text</WORD>")) { //Case 2: VTYP -> text
            System.out.println("Returning t");
            return "t";
        } else {
            System.out.println("Returning u");
            return "u";
        }
    }
    
    private String typeOfVNAME(SyntaxTreeNode vname) {
        System.out.println("Type of VNAME");
        SyntaxTreeNode next = treeCrawler.getNext();
        System.out.println("next: " + next.getTerminalWord());
        System.out.println(SymbolTableAccessor.lookupVariable(vname.getTerminalWord(), "global"));

        //Not sure about this one
        return vname.getTerminal();
    }
    
    private boolean typeCheckALGO(SyntaxTreeNode algo) {
        System.out.println("Type checking ALGO");
        System.out.println("algo: " + algo.toString());
        // ALGO -> begin INSTRUC end
        treeCrawler.getNext(); //getting begin since it is not used in type checking
        boolean result = typeCheckINSTRUC(treeCrawler.getNext());
        treeCrawler.getNext(); //getting end since it is not used in type checking
        return result;
    }

    private boolean typeCheckINSTRUC(SyntaxTreeNode instruc) {
        System.out.println("Type checking INSTRUC");
        System.out.println("instruc: " + instruc.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if (next.getTerminal()!=null && next.getTerminal().isEmpty()) { //Case 1: INSTRUC -> 
            return true;
        } else if(next.getSymb() == "COMMAND") { //Case 2: INSTRUC -> COMMAND ; INSTRUC
            return typeCheckCOMMAND(treeCrawler.getNext()) && typeCheckINSTRUC(treeCrawler.getNext());
        } else {
            return false;
        }        
    }

    private boolean typeCheckCOMMAND(SyntaxTreeNode command) {
        System.out.println("Type checking COMMAND");
        System.out.println("command: " + command.toString());

        SyntaxTreeNode next = treeCrawler.getNext();
        if (next.getTerminal().contains("<WORD>skip</WORD>")) { //Case 1: COMMAND -> skip
            return true;
        } else if (next.getTerminal().contains("<WORD>halt</WORD>")) { //Case 2: COMMAND -> halt
            return true;
        } else if (next.getTerminal().contains("<WORD>print</WORD>")) {//Case 3: COMMAND -> print ATOMIC
            String atomic = typeOfATOMIC(treeCrawler.getNext());
            if (atomic.equals("n") ) {
                return true;
            } else if (atomic == "t") {
                return true;
            } else {
                return false;
            }
        } else if (next.getTerminal().contains("<WORD>return</WORD>")) {//Case 4: COMMAND -> return ATOMIC
            SyntaxTreeNode fname = treeCrawler.getFnameForReturnCommand(command);
            if(fname == null) {
                return false;
            }
            if (typeOfATOMIC(command) == typeOfFNAME(fname) && typeOfATOMIC(command).equals("n") ) {
                return true;
            } else {
                return false;
            }
        } else if (next.getSymb().equals("ASSIGN")) {//Case 5: COMMAND -> ASSIGN
            return typeCheckASSIGN(next);
        } else if (next.getSymb().equals("CALL")) { //Case 6: COMMAND -> CALL
            if (this.typeOfCall(next) == "v") {
                return true;
            } else {
                return false;
            }
        } else if (next.getSymb().equals("BRANCH")) { //Case 7: COMMAND -> BRANCH
            return typeCheckBRANCH(next);
        }
        return false;
    }

    private String typeOfATOMIC(SyntaxTreeNode atomic) {
        System.out.println("Type checking ATOMIC");
        System.out.println("atomic: " + atomic.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if(next.getSymb().equals("VNAME")) { //Case 1: ATOMIC -> VNAME
            return this.typeOfVNAME(next);
        } else if(next.getSymb().equals("CONST")) { //Case 2: ATOMIC -> CONST
            return this.typeOfCONST(next);
        } else {
            return "u";
        }        
    }

    private String typeOfCONST(SyntaxTreeNode cons) {
        System.out.println("Type checking CONST");
        System.out.println("cons: " + cons.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if(next.getTerminal().contains("<WORD>TokenN</WORD>")) { //Case 1: CONST -> TokenN
            return "n";
        } else if(next.getTerminal().contains("<WORD>TokenT</WORD>")) { //Case 2: CONST -> TokenT
            return "t";
        } else {
            return "u";
        }
    }

    private boolean typeCheckASSIGN(SyntaxTreeNode assign) {
        System.out.println("Type checking ASSIGN");
        System.out.println("assign: " + assign.toString());
        SyntaxTreeNode next1 = treeCrawler.getNext();
        SyntaxTreeNode next2 = treeCrawler.getNext();
        SyntaxTreeNode next3 = treeCrawler.getNext();
        
        if (next2.getTerminal().contains("<WORD>&lt;</WORD>")) { // ASSIGN -> VNAME < input
            //Case 1: ASSIGN -> VNAME < input
            if(this.typeOfVNAME(next1).equals("n")) {
                return true;
            } else {
                return false;
            }
        } else if (next2.getTerminal().contains("<WORD>:=</WORD>")) { //Case 2: ASSIGN -> VNAME := TERM
            if(this.typeOfVNAME(next1) == this.typeOfTERM(next3)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private String typeOfTERM(SyntaxTreeNode term) {
        System.out.println("Type checking TERM");
        System.out.println("term: " + term.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if(next.getSymb().equals("ATOMIC")) { //Case 1: TERM -> ATOMIC
            return this.typeOfATOMIC(next);
        }else if(next.getSymb().equals("CALL")) { //Case 2: TERM -> CALL
            return this.typeOfCall(next);
        } else if (next.getSymb().equals("OP")) { //Case 3: TERM -> OP
            return this.typeOfOP(next);
        }else {
            return "u";
        }
    }

    private String typeOfCall(SyntaxTreeNode call) {
        System.out.println("Type checking CALL");
        System.out.println("call: " + call.toString());
        // Call -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
        SyntaxTreeNode fname = treeCrawler.getNext();
        treeCrawler.getNext(); //(
        SyntaxTreeNode atomic1 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode atomic2 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode atomic3 = treeCrawler.getNext();
        treeCrawler.getNext(); //)

        if(typeOfATOMIC(atomic1).equals("n") && typeOfATOMIC(atomic2).equals("n") && typeOfATOMIC(atomic3).equals("n")) {
            return typeOfFNAME(fname);
        } else {
            return "u";
        }
    }
    
    private String typeOfOP(SyntaxTreeNode op) {
        System.out.println("Type checking OP");
        System.out.println("op: " + op.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if(next.getSymb().equals("UNOP")) { //Case 1: OP -> UNOP ( ARG )
            treeCrawler.getNext(); //(
            SyntaxTreeNode arg = treeCrawler.getNext();
            treeCrawler.getNext(); //)

            if( this.typeOfUNOP(next).equals("b") && this.typeOfARG(arg).equals("b")) {
                return "n";
            } else {
                return "u";
            }
        } else if(next.getSymb().equals("BINOP")) { //Case 2: OP -> BINOP ( ARG , ARG )
            treeCrawler.getNext(); //(
            SyntaxTreeNode arg1 = treeCrawler.getNext();
            treeCrawler.getNext(); //,
            SyntaxTreeNode arg2 = treeCrawler.getNext();
            treeCrawler.getNext(); //)

            if (this.typeOfBINOP(next).equals("b")  && this.typeOfARG(arg1).equals("b") && this.typeOfARG(arg2).equals("b")) {
                return "b";
            } else if(this.typeOfBINOP(next).equals("n") && this.typeOfARG(arg1).equals("n") && this.typeOfARG(arg2).equals("n")) {
                return "n";
            } else if(this.typeOfBINOP(next).equals("c") && this.typeOfARG(arg1).equals("n") && this.typeOfARG(arg2).equals("n")) {
                return "b";
            } else {
                return "u";
            }
        } else {
            return "u";
        }
    }

    private String typeOfARG(SyntaxTreeNode arg) {
        System.out.println("Type checking ARG");
        System.out.println("arg: " + arg.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if (next.getSymb() == "ATOMIC") { //Case 1: ARG -> ATOMIC
            return this.typeOfATOMIC(next);
        } else if(next.getSymb() == "OP") { //Case 2: ARG -> OP
            return this.typeOfOP(next);
        } else {
            return "u";
        }       
    }

    private String typeOfUNOP(SyntaxTreeNode unop) {
        System.out.println("Type checking UNOP");
        System.out.println("unop: " + unop.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if (next.getTerminal().contains("<WORD>not</WORD>")) { //Case 1: UNOP -> not
            return "b";
        } else if (next.getTerminal().contains("<WORD>sqrt</WORD>")) { //Case 2: UNOP -> sqrt
            return "n";
        } else {
            return "u";
        }
    }

    private String typeOfBINOP(SyntaxTreeNode binop) {
        System.out.println("Type checking BINOP");
        System.out.println("binop: " + binop.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if (next.getTerminal().contains("<WORD>or</WORD>")) { //Case 1: BINOP -> or
            return "b";
        } else if (next.getTerminal().contains("<WORD>and</WORD>")) { //Case 2: BINOP -> and
            return "b";
        } else if (next.getTerminal().contains("<WORD>eq</WORD>")) { //Case 3: BINOP -> eq
            return "c";
        } else if (next.getTerminal().contains("<WORD>grt</WORD>")) { //Case 4: BINOP -> grt
            return "c";
        } else if (next.getTerminal().contains("<WORD>add</WORD>")) { //Case 5: BINOP -> add
            return "n";
        } else if (next.getTerminal().contains("<WORD>sub</WORD>")) { //Case 6: BINOP -> sub
            return "n";
        } else if (next.getTerminal().contains("<WORD>mul</WORD>")) { //Case 7: BINOP -> mul
            return "n";
        } else if (next.getTerminal().contains("<WORD>div</WORD>")) { //Case 8: BINOP -> div
            return "n";
        } else {
            return "u";
        }
    }

    private boolean typeCheckBRANCH(SyntaxTreeNode branch) {
        System.out.println("Type checking BRANCH");
        System.out.println("branch: " + branch.toString());
        //BRANCH->if COND then ALGO else ALGO
        treeCrawler.getNext(); //if
        SyntaxTreeNode cond = treeCrawler.getNext();
        treeCrawler.getNext(); //then
        SyntaxTreeNode algo1 = treeCrawler.getNext();
        treeCrawler.getNext(); //else
        SyntaxTreeNode algo2 = treeCrawler.getNext();
        
        if( this.typeOfCOND(cond).equals("b")) {
            return typeCheckALGO(algo1) && typeCheckALGO(algo2);
        } else {
            return false;
        }
    }

    private String typeOfCOND(SyntaxTreeNode cond) {
        System.out.println("Type checking COND");
        System.out.println("cond: " + cond.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if(next.getSymb().equals("SIMPLE") ) { //Case 1: COND -> SIMPLE
            return this.typeOfSIMPLE(next);
        } else if(next.getSymb().equals("COMPOSIT")) { //Case 2: COND -> COMPOSIT
            return this.typeOfCOMPOSIT(next);
        } else {
            return "u";
        }
    }

    private String typeOfSIMPLE(SyntaxTreeNode simple) {
        System.out.println("Type checking SIMPLE");
        System.out.println("simple: " + simple.toString());
        //SIMPLE->BINOP ( ATOMIC , ATOMIC )
        SyntaxTreeNode binop = treeCrawler.getNext();
        treeCrawler.getNext(); //(
        SyntaxTreeNode atmoic1 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode atmoic2 = treeCrawler.getNext();
        treeCrawler.getNext(); //)

        if(this.typeOfBINOP(binop).equals("b") && this.typeOfATOMIC(atmoic1).equals("b") && this.typeOfATOMIC(atmoic2).equals("b")) {
            return "b";
        } else if(this.typeOfBINOP(binop).equals("c") && this.typeOfATOMIC(atmoic1).equals("n") && this.typeOfATOMIC(atmoic2).equals("n")) {
            return "b";
        } else {
            return "u";
        }
    }

    private String typeOfCOMPOSIT(SyntaxTreeNode composit) {
        System.out.println("Type checking COMPOSIT");
        System.out.println("composit: " + composit.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if (next.getSymb() == "BINOP") { //Case 1: COMPOSIT -> BINOP ( SIMPLE , SIMPLE )
            treeCrawler.getNext(); //(
            SyntaxTreeNode simple1 = treeCrawler.getNext();
            treeCrawler.getNext(); //,
            SyntaxTreeNode simple2 = treeCrawler.getNext();
            treeCrawler.getNext(); //)

            if(this.typeOfBINOP(next).equals("b") && this.typeOfSIMPLE(simple1).equals("b") && this.typeOfSIMPLE(simple2).equals("b")) {
                return "b";
            } else {
                return "u";
            }

        } else if (next.getSymb() == "UNOP") {//Case 2: COMPOSIT -> UNOP ( SIMPLE )
            treeCrawler.getNext(); //(
            SyntaxTreeNode simple = treeCrawler.getNext();
            treeCrawler.getNext(); //)

            if(this.typeOfUNOP(next).equals("b")  && this.typeOfSIMPLE(simple).equals("b") ) {
                return "b";
            } else {
                return "u";
            }
        } else {
            return "u";
        }
    }

    private String typeOfFNAME(SyntaxTreeNode fname) {
        System.out.println("Type checking FNAME");
        System.out.println("fname: " + fname.toString());
        return fname.getTerminal();
    }

    private boolean typeCheckFUNCTIONS(SyntaxTreeNode functions) {
        System.out.println("Type checking FUNCTIONS");
        System.out.println("functions: " + functions.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if (next.getTerminal()!= null && next.getTerminal().isEmpty()) { //Case 1: FUNCTIONS -> 
            return true;
        } else if(next.getSymb().equals("DECL")) { //Case 2: FUNCTIONS -> DECL FUNCTIONS
            return typeCheckDECL(treeCrawler.getNext()) && typeCheckFUNCTIONS(treeCrawler.getNext());
        } else {
            return false;
        }
    }

    private boolean typeCheckDECL(SyntaxTreeNode decl) {
        System.out.println("Type checking DECL");
        System.out.println("decl: " + decl.toString());
        //DECL->HEADER BODY
        return typeCheckHEADER(treeCrawler.getNext()) && typeCheckBODY(treeCrawler.getNext());
    }

    private boolean typeCheckHEADER(SyntaxTreeNode header) {
        System.out.println("Type checking HEADER");
        System.out.println("header: " + header.toString());
        //HEADER->FTYP FNAME ( VNAME , VNAME , VNAME )
        SyntaxTreeNode ftyp = treeCrawler.getNext();
        SyntaxTreeNode fname = treeCrawler.getNext();
        treeCrawler.getNext(); //(
        SyntaxTreeNode vname1 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode vname2 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode vname3 = treeCrawler.getNext();
        treeCrawler.getNext(); //)

        String T = typeOfFTYP(ftyp);
        Symbol id = symbolTable.lookupFunction(fname.getTerminal());
        // symbolTable.put(t, id);
        if (this.typeOfFNAME(fname) != this.typeOfFTYP(ftyp)) {
            return false;
        }
        if(this.typeOfVNAME(vname1).equals("n")  && this.typeOfVNAME(vname2).equals("n")  && this.typeOfVNAME(vname3).equals("n") ) {
            return true;
        } else {
            return false;
        }
    }

    private String typeOfFTYP(SyntaxTreeNode ftyp) {
        System.out.println("Type checking FTYP");
        System.out.println("ftyp: " + ftyp.toString());
        SyntaxTreeNode next = treeCrawler.getNext();
        if(next.getTerminal().contains("<WORD>num</WORD>")) {//Case 1: FTYP -> num 
            return "n";
        } else if(next.getTerminal().contains("<WORD>text</WORD>")) { //Case 2: FTYP -> text
            return "t";
        } else {
            return "u";
        }
    }

    private boolean typeCheckBODY(SyntaxTreeNode body) {
        System.out.println("Type checking BODY");
        System.out.println("body: " + body.toString());
        //BODY->PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
        return this.typeCheckPROLOG(treeCrawler.getNext()) && this.typeCheckLOCVARS(treeCrawler.getNext())
                && this.typeCheckALGO(treeCrawler.getNext()) && this.typeCheckEPILOG(treeCrawler.getNext())
                && this.typeCheckSUBFUNCS(treeCrawler.getNext());
    }
    
    private boolean typeCheckPROLOG(SyntaxTreeNode prolog) {
        System.out.println("Type checking PROLOG");
        System.out.println("prolog: " + prolog.toString());
        //PROLOG->{
        return true;
    }

    private boolean typeCheckEPILOG(SyntaxTreeNode epilog) {
        System.out.println("Type checking EPILOG");
        System.out.println("epilog: " + epilog.toString());
        //EPILOG->}
        return true;
    }

    private boolean typeCheckLOCVARS(SyntaxTreeNode locvars) {
        System.out.println("Type checking LOCVARS");
        System.out.println("locvars: " + locvars.toString());
        //LOCVARS->VTYP VNAME , VTYP VNAME , VTYP VNAME ,
        SyntaxTreeNode vtype1 = treeCrawler.getNext();
        SyntaxTreeNode vname1 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode vtype2 = treeCrawler.getNext();
        SyntaxTreeNode vname2 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode vtype3 = treeCrawler.getNext();
        SyntaxTreeNode vname3 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
                        
        String t = typeOfVTYP(vtype1);
        Symbol id = SymbolTableAccessor.lookupFunction(vname1.getTerminal());
        // symbolTable.put(t, id);
        if (this.typeOfVNAME(vname1) != this.typeOfVTYP(vtype1)) {
            return false;
        }
        
        t = typeOfVTYP(vtype2);
        id = SymbolTableAccessor.lookupFunction(vname2.getTerminal());
        // symbolTable.put(t, id);
        if (this.typeOfVNAME(vname2) != this.typeOfVTYP(vtype2)) {
            return false;
        }

        t = typeOfVTYP(vtype3);
        id = SymbolTableAccessor.lookupFunction(vname3.getTerminal());
        // symbolTable.put(t, id);
        if (this.typeOfVNAME(vname3) != this.typeOfVTYP(vtype3)) {
            return false;
        }
        return true;
    }

    private boolean typeCheckSUBFUNCS(SyntaxTreeNode subfuncs) {
        System.out.println("Type checking SUBFUNCS");
        System.out.println("subfuncs: " + subfuncs.toString());
        //SUBFUNCS->FUNCTIONS
        return this.typeCheckFUNCTIONS(treeCrawler.getNext());
    }

    public static void main(String[] args) {
       // Crawling a tree and printing the nodes
        try {
            typeChecker typeChecker = new typeChecker("src/parser2/output/output2.xml");
            System.out.println("The result of the type checker: " + typeChecker.typeCheck());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

