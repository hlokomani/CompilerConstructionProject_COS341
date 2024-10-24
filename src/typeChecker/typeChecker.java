package typeChecker;
import semanticanalyzer.SemanticAnalyzer;
import semanticanalyzer.Symbol;
import semanticanalyzer.SymbolTable;
import semanticanalyzer.SymbolTableAccessor;

import java.util.List;

import parser2.SyntaxTreeNode;

public class typeChecker {
    private TreeCrawler treeCrawler;
    private SyntaxTreeNode lastFame = null;

    
    public typeChecker(String xmlFilePath) throws Exception {
        //Initialize symbol table 
        treeCrawler = new TreeCrawler(xmlFilePath);     
    }

    public typeCheck typeCheck() throws Exception {
        return typeCheckPROG(treeCrawler.getNext());
    }

    private typeCheck typeCheckPROG(SyntaxTreeNode prog) throws Exception {
        //System.out.println("Type checking PROG");
        //System.out.println("prog: " + prog.toString());
        //PROG->main GLOBVARS ALGO FUNCTIONS
        boolean result = true;
        List<SyntaxTreeNode> children = prog.getChildren();
        typeCheck globVars = typeCheckGLOBVARS(children.get(1));
        typeCheck algo = typeCheckALGO(children.get(2));
        typeCheck functions = typeCheckFUNCTIONS(children.get(3));
        result = result && globVars.type && algo.type && functions.type;
        String message = globVars.message + " " + algo.message + " " + functions.message;
        if (result) {
            return new typeCheck(true, "Program is correctly typed.");
        }
        //removing leading whitespace
        message = message.replaceAll("^\\s+", "");
        return new typeCheck(result, message);
    }

    private typeCheck typeCheckGLOBVARS(SyntaxTreeNode globVars) {
        List<SyntaxTreeNode> children = globVars.getChildren();
        //System.out.println("Type checking GLOBVARS");
        //System.out.println("globVars: " + globVars.toString());

        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.toString());
        if (next.getTerminal() != null && next.getTerminal().isEmpty()) {//Case 1: GLOBVARS ->
            //System.out.println("GLOBVARS -> epsilon");
            return new typeCheck(true, "");
        } else if (next.getSymb().equals("VTYP")) {//Case 2: GLOBVARS -> VTYP VNAME , GLOBVARS
            //System.out.println("GLOBVARS -> VTYP VNAME , GLOBVARS");
            String vtypType = typeOfVTYP(next);
            String vnameType = this.typeOfVNAME(children.get(1));

            if (!vnameType.equals(vtypType)) {
                //System.out.println("Type checking failed for GLOBVARS -> VTYP VNAME , GLOBVARS");
                return new typeCheck(false, "Type checking failed. Expected " + vtypType + " but got " + vnameType + ".\n Occured at: " + next.getChildren().get(0).getPosition());
            }
            treeCrawler.getNext(); //,
            return typeCheckGLOBVARS(children.get(3));
        } else {
            //System.out.println("Type checking failed for GLOBVARS");
            return new typeCheck(false, "Type checking failed.");
        }
    }

    private String typeOfVTYP(SyntaxTreeNode vtyp) {
        List<SyntaxTreeNode> children = vtyp.getChildren();
        //System.out.println("Type of VTYP");
        //System.out.println("vtyp: " + vtyp.toString());
        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.toString());
        if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>num</WORD>")) { //Case 1: VTYP -> num
            //System.out.println("Returning n");
            return "n";
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>text</WORD>")) { //Case 2: VTYP -> text
            //System.out.println("Returning t");
            return "t";
        } else {
            //System.out.println("Returning u");
            return "u";
        }
    }
    
    private String typeOfVNAME(SyntaxTreeNode vname) {
        List<SyntaxTreeNode> children = vname.getChildren();
        //System.out.println("Type of VNAME");
        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.getTerminalWord());
        String type = SymbolTableAccessor.getSymbolType(next.getTerminalWord());
        // if (type == null) {
        //     return "u";
        // }
        //System.out.println(type);
        //Not sure about this one
        if (type.equals("num")) {
            //System.out.println("Returning n");
            return "n";
        } else if (type.equals("text")) {
            //System.out.println("Returning t");
            return "t";
        } else {
            //System.out.println("Returning u");
            return "u";
        }
        
    }
    
    private typeCheck typeCheckALGO(SyntaxTreeNode algo) {
        List<SyntaxTreeNode> children = algo.getChildren();
        //System.out.println("Type checking ALGO");
        //System.out.println("algo: " + algo.toString());
        // ALGO -> begin INSTRUC end
        typeCheck result = typeCheckINSTRUC(children.get(1));
        return new typeCheck(result.type, result.message);
    }

    private typeCheck typeCheckINSTRUC(SyntaxTreeNode instruc) {
        List<SyntaxTreeNode> children = instruc.getChildren();
        //System.out.println("Type checking INSTRUC");
        //System.out.println("instruc: " + instruc.toString());
        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.getSymb());
        if (next.getTerminal()!=null && next.getTerminal().isEmpty()) { //Case 1: INSTRUC ->
            return new typeCheck(true, "");
        } else if (next.getSymb().equals("COMMAND")) { //Case 2: INSTRUC -> COMMAND ; INSTRUC
            typeCheck command = typeCheckCOMMAND(next);
            typeCheck instruc2 = typeCheckINSTRUC(children.get(2));
            return new typeCheck(command.type && instruc2.type, command.message + " " + instruc2.message);
        } else {
            return new typeCheck(false, "Type checking failed.");
        }
    }

    private typeCheck typeCheckCOMMAND(SyntaxTreeNode command) {
        List<SyntaxTreeNode> children = command.getChildren();
        //System.out.println("Type checking COMMAND");
        //System.out.println("command: " + command.toString());

        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.toString());
        if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>skip</WORD>")) { //Case 1: COMMAND -> skip
            return new typeCheck(true, "");
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>halt</WORD>")) { //Case 2: COMMAND -> halt
            return new typeCheck(true, "");
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>print</WORD>")) {//Case 3: COMMAND -> print ATOMIC
            String atomic = typeOfATOMIC(children.get(1));
            if (atomic.equals("n") ) {
                return new typeCheck(true, "");
            } else if (atomic.equals("t")) {
                return new typeCheck(true, "");
            } else {
                return new typeCheck(false, "Type checking failed. Can only print num or text. \nOccurred at: " + next.getPosition());
            }
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>return</WORD>")) {//Case 4: COMMAND -> return ATOMIC
            if (this.lastFame == null) {
                return new typeCheck(false, "Type checking failed.");
            }
            String typeAtomic = typeOfATOMIC(children.get(1));
            if (typeAtomic.equals(typeOfFNAME(this.lastFame)) && typeAtomic.equals("n") ) {
                return new typeCheck(true, "");
            } else {
                return new typeCheck(false, "Type checking failed. Can only return num. \nOccurred at: " + next.getPosition());
            }
        } else if (next.getSymb().equals("ASSIGN")) {//Case 5: COMMAND -> ASSIGN
            return typeCheckASSIGN(next);
        } else if (next.getSymb().equals("CALL")) { //Case 6: COMMAND -> CALL
            if (this.typeOfCall(next).equals("v")) {
                //System.out.println("Returning true from COMMAND");
                return new typeCheck(true, "");
            } else {
                //System.out.println("Returning false from COMMAND");
                return new typeCheck(false, "Type checking failed. Must assign the result of a non-void function to a variable. \nOccurred at: " + next.getChildren().get(0).getChildren().get(0).getPosition());
            }
        } else if (next.getSymb().equals("BRANCH")) { //Case 7: COMMAND -> BRANCH
            return typeCheckBRANCH(next);
        }
        return new typeCheck(false, "Type checking failed.");
    }

    private String typeOfATOMIC(SyntaxTreeNode atomic) {
        List<SyntaxTreeNode> children = atomic.getChildren();
        //System.out.println("Type checking ATOMIC");
        //System.out.println("atomic: " + atomic.toString());
        SyntaxTreeNode next = children.get(0);
        if(next.getSymb().equals("VNAME")) { //Case 1: ATOMIC -> VNAME
            return this.typeOfVNAME(next);
        } else if(next.getSymb().equals("CONST")) { //Case 2: ATOMIC -> CONST
            return this.typeOfCONST(next);
        } else {
            return "u";
        }        
    }

    private String typeOfCONST(SyntaxTreeNode cons) {
        List<SyntaxTreeNode> children = cons.getChildren();
        //System.out.println("Type checking CONST");
        //System.out.println("cons: " + cons.toString());
        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.toString());
        if (next.getTerminal() != null && next.getTerminal().contains("<CLASS>N</CLASS>")) { //Case 1: CONST -> TokenN
            //System.out.println("CONST -> TokenN");
            return "n";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<CLASS>T</CLASS>")) { //Case 2: CONST -> TokenT
            //System.out.println("CONST -> TokenT");
            return "t";
        } else {
            //System.out.println("returning u");
            return "u";
        }
    }

    private typeCheck typeCheckASSIGN(SyntaxTreeNode assign) {
        List<SyntaxTreeNode> children = assign.getChildren();
        //System.out.println("Type checking ASSIGN");
        //System.out.println("assign: " + assign.toString());
        SyntaxTreeNode symbol = children.get(1);
        //System.out.println("symbol: " + symbol.toString());
        
        if (symbol.getTerminal()!=null && symbol.getTerminal().contains("<WORD>&lt;</WORD>")) { // ASSIGN -> VNAME < input
            //Case 1: ASSIGN -> VNAME < input
            if (this.typeOfVNAME(children.get(0)).equals("n")) {
                //System.out.println("Returning true from ASSIGN");
                return new typeCheck(true, "");
            } else {
                //System.out.println("Returning false from ASSIGN");
                return new typeCheck(false, "Type checking failed. Can only input to a num variable. \nOccurred at: " + children.get(0).getChildren().get(0).getPosition());
            }
        } else if (symbol.getTerminal() != null && symbol.getTerminal().contains("<WORD>=</WORD>")) { //Case 2: ASSIGN -> VNAME := TERM
            //System.out.println("ASSIGN -> VNAME := TERM");
            //System.out.println("VNAME: " + children.get(0).toString());
            //System.out.println("TERM: " + children.get(2).toString());
            String typeVname = this.typeOfVNAME(children.get(0));
            String typeTerm = this.typeOfTERM(children.get(2));
            //System.out.println("typeVname: " + typeVname);
            //System.out.println("typeTerm: " + typeTerm);
            if (typeVname.equals(typeTerm)) {
                //System.out.println("Returning true from ASSIGN");
                return new typeCheck(true, "");
            } else {
                //System.out.println("Returning false from ASSIGN");
                return new typeCheck(false, "Type checking failed. Expected " + typeVname + " but got " + typeTerm + ".\n Occurred at: " + children.get(0).getChildren().get(0).getPosition());
            }
        } else {
            //System.out.println("Returning false from ASSIGN");
            return new typeCheck(false, "Type checking failed.");
        }
    }

    private String typeOfTERM(SyntaxTreeNode term) {
        List<SyntaxTreeNode> children = term.getChildren();
        //System.out.println("Type checking TERM");
        //System.out.println("term: " + term.toString());
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("ATOMIC")) { //Case 1: TERM -> ATOMIC
            //System.out.println("TERM -> ATOMIC");
            return this.typeOfATOMIC(next);
        } else if (next.getSymb().equals("CALL")) { //Case 2: TERM -> CALL
            //System.out.println("TERM -> CALL");
            return this.typeOfCall(next);
        } else if (next.getSymb().equals("OP")) { //Case 3: TERM -> OP
            //System.out.println("TERM -> OP");
            return this.typeOfOP(next);
        } else {
            //System.out.println("Returning u");
            return "u";
        }
    }

    private String typeOfCall(SyntaxTreeNode call) {
        // Call -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
        List<SyntaxTreeNode> children = call.getChildren();
        //System.out.println("Type checking CALL");
        //System.out.println("call: " + call.toString());
        String typeAtomic1 = typeOfATOMIC(children.get(2));
        String typeAtomic2 = typeOfATOMIC(children.get(4));
        String typeAtomic3 = typeOfATOMIC(children.get(6));

        if (typeAtomic1.equals("n") && typeAtomic2.equals("n") && typeAtomic3.equals("n")) {
            String typeFname = typeOfFNAME(children.get(0));
            //System.out.println("typeFname: " + typeFname);
            return typeFname;
        } else {
            return "u";
        }
    }
    
    private String typeOfOP(SyntaxTreeNode op) {
        List<SyntaxTreeNode> children = op.getChildren();
        //System.out.println("Type checking OP");
        //System.out.println("op: " + op.toString());
        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.toString());
        if (next.getSymb().equals("UNOP")) { //Case 1: OP -> UNOP ( ARG )
            String unop = typeOfUNOP(next);
            String arg = typeOfARG(children.get(2));
            if (unop.equals("b") && arg.equals("b")) {
                //System.out.println("Returning b");
                return "b";
            } else if (unop.equals("n") && arg.equals("n")){
                //System.out.println("Returning n");
                return "n";
            } else {
                //System.out.println("Returning u");
                return "u";
            }
        } else if(next.getSymb().equals("BINOP")) { //Case 2: OP -> BINOP ( ARG , ARG )
            String typeBinop = typeOfBINOP(next);
            String typeArg1 = typeOfARG(children.get(2));
            String typeArg2 = typeOfARG(children.get(4));

            if (typeBinop.equals("b") && typeArg1.equals("b") && typeArg2.equals("b")) {
                //System.out.println("Returning b");
                return "b";
            } else if (typeBinop.equals("n") && typeArg1.equals("n") && typeArg2.equals("n")) {
                //System.out.println("Returning n");
                return "n";
            } else if (typeBinop.equals("c") && typeArg1.equals("n") && typeArg2.equals("n")) {
                //System.out.println("Returning b");
                return "b";
            } else {
                //System.out.println("Returning u");
                return "u";
            }
        } else {
            return "u";
        }
    }

    private String typeOfARG(SyntaxTreeNode arg) {
        List<SyntaxTreeNode> children = arg.getChildren();
        //System.out.println("Type checking ARG");
        //System.out.println("arg: " + arg.toString());
        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.toString());
        if (next.getSymb().equals("ATOMIC")) { //Case 1: ARG -> ATOMIC
            //System.out.println("ARG -> ATOMIC");
            return this.typeOfATOMIC(next);
        } else if (next.getSymb().equals("OP")) { //Case 2: ARG -> OP
            //System.out.println("ARG -> OP");
            return this.typeOfOP(next);
        } else {
            return "u";
        }       
    }

    private String typeOfUNOP(SyntaxTreeNode unop) {
        List<SyntaxTreeNode> children = unop.getChildren();
        //System.out.println("Type checking UNOP");
        //System.out.println("unop: " + unop.toString());
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal() != null && next.getTerminal().contains("<WORD>not</WORD>")) { //Case 1: UNOP -> not
            //System.out.println("UNOP -> not");
            return "b";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>sqrt</WORD>")) { //Case 2: UNOP -> sqrt
            //System.out.println("UNOP -> sqrt");
            return "n";
        } else {
            return "u";
        }
    }

    private String typeOfBINOP(SyntaxTreeNode binop) {
        List<SyntaxTreeNode> children = binop.getChildren();
        //System.out.println("Type checking BINOP");
        //System.out.println("binop: " + binop.toString());
        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.toString());
        if (next.getTerminal() != null && next.getTerminal().contains("<WORD>or</WORD>")) { //Case 1: BINOP -> or
            //System.out.println("BINOP -> or");
            return "b";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>and</WORD>")) { //Case 2: BINOP -> and
            //System.out.println("BINOP -> and");
            return "b";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>eq</WORD>")) { //Case 3: BINOP -> eq
            //System.out.println("BINOP -> eq");
            return "c";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>grt</WORD>")) { //Case 4: BINOP -> grt
            //System.out.println("BINOP -> grt");
            return "c";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>add</WORD>")) { //Case 5: BINOP -> add
            //System.out.println("BINOP -> add");
            return "n";
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>sub</WORD>")) { //Case 6: BINOP -> sub
            //System.out.println("BINOP -> sub");
            return "n";
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>mul</WORD>")) { //Case 7: BINOP -> mul
            //System.out.println("BINOP -> mul");
            return "n";
        } else if (next.getTerminal()!=null && next.getTerminal().contains("<WORD>div</WORD>")) { //Case 8: BINOP -> div
            //System.out.println("BINOP -> div");
            return "n";
        } else {
            //System.out.println("Returning u");
            return "u";
        }
    }

    private typeCheck typeCheckBRANCH(SyntaxTreeNode branch) {
        List<SyntaxTreeNode> children = branch.getChildren();
        //System.out.println("Type checking BRANCH");
        //System.out.println("branch: " + branch.toString());
        //BRANCH->if COND then ALGO else ALGO        
        if (this.typeOfCOND(children.get(1)).equals("b")) {
            typeCheck algo1 = typeCheckALGO(children.get(3));
            typeCheck algo2 = typeCheckALGO(children.get(5));
            return new typeCheck(algo1.type && algo2.type, algo1.message + " " + algo2.message);
        } else {
            return new typeCheck(false, "Type checking failed. Condition must be of type boolean. \nOccurred at: " + children.get(0).getPosition());
        }
    }

    private String typeOfCOND(SyntaxTreeNode cond) {
        List<SyntaxTreeNode> children = cond.getChildren();
        //System.out.println("Type checking COND");
        //System.out.println("cond: " + cond.toString());
        SyntaxTreeNode next = children.get(0);
        if(next.getSymb().equals("SIMPLE") ) { //Case 1: COND -> SIMPLE
            return this.typeOfSIMPLE(next);
        } else if(next.getSymb().equals("COMPOSIT")) { //Case 2: COND -> COMPOSIT
            return this.typeOfCOMPOSIT(next);
        } else {
            return "u";
        }
    }

    private String typeOfSIMPLE(SyntaxTreeNode simple) {
        List<SyntaxTreeNode> children = simple.getChildren();
        //System.out.println("Type checking SIMPLE");
        //System.out.println("simple: " + simple.toString());
        //SIMPLE->BINOP ( ATOMIC , ATOMIC )
        String typeBinop = this.typeOfBINOP(children.get(0));
        String typeAtomic1 = this.typeOfATOMIC(children.get(2));
        String typeAtomic2 = this.typeOfATOMIC(children.get(4));

        if(typeBinop.equals("b") && typeAtomic1.equals("b") && typeAtomic2.equals("b")) {
            return "b";
        } else if(typeBinop.equals("c") && typeAtomic1.equals("n") && typeAtomic2.equals("n")) {
            return "b";
        } else {
            return "u";
        }
    }

    private String typeOfCOMPOSIT(SyntaxTreeNode composit) {
        List<SyntaxTreeNode> children = composit.getChildren();
        //System.out.println("Type checking COMPOSIT");
        //System.out.println("composit: " + composit.toString());
        SyntaxTreeNode next = children.get(0);
        if (next.getSymb().equals("BINOP") ) { //Case 1: COMPOSIT -> BINOP ( SIMPLE , SIMPLE )
            if(this.typeOfBINOP(next).equals("b") && this.typeOfSIMPLE(children.get(2)).equals("b") && this.typeOfSIMPLE(children.get(4)).equals("b")) {
                return "b";
            } else {
                return "u";
            }

        } else if (next.getSymb().equals("UNOP")) {//Case 2: COMPOSIT -> UNOP ( SIMPLE )
            if(this.typeOfUNOP(next).equals("b")  && this.typeOfSIMPLE(children.get(2)).equals("b") ) {
                return "b";
            } else {
                return "u";
            }
        } else {
            return "u";
        }
    }

    private String typeOfFNAME(SyntaxTreeNode fname) {
        this.lastFame = fname;
        //System.out.println("Type checking FNAME");
        //System.out.println("fname: " + fname.toString());
        SyntaxTreeNode next = fname.getChildren().get(0);
        //System.out.println("next terminal: " + next.getTerminalWord());
        String type = SymbolTableAccessor.getSymbolType(next.getTerminalWord());
        //System.out.println(type);
        //Not sure about this one
        if (type.equals("num")) {
            //System.out.println("Returning n");
            return "n";
        } else if (type.equals("void")) {
            //System.out.println("Returning v");
            return "v";
        } else if (type.equals("text")) {
            //System.out.println("Returning t");
            return "t";
        } else {
            //System.out.println("Returning u");
            return "u";
        }
        
    }

    private typeCheck typeCheckFUNCTIONS(SyntaxTreeNode functions) {
        List<SyntaxTreeNode> children = functions.getChildren();
        //System.out.println("Type checking FUNCTIONS");
        //System.out.println("functions: " + functions.toString());
        SyntaxTreeNode next = children.get(0);
        if (next.getTerminal()!= null && next.getTerminal().isEmpty()) { //Case 1: FUNCTIONS -> 
            return new typeCheck(true, "");
        } else if (next.getSymb().equals("DECL")) { //Case 2: FUNCTIONS -> DECL FUNCTIONS
            typeCheck decl = typeCheckDECL(next);
            typeCheck functions2 = typeCheckFUNCTIONS(children.get(1));
            return new typeCheck(decl.type && functions2.type, decl.message + " " + functions2.message);
        } else {
            return new typeCheck(false, "Type checking failed.");
        }
    }

    private typeCheck typeCheckDECL(SyntaxTreeNode decl) {
        //DECL->HEADER BODY
        List<SyntaxTreeNode> children = decl.getChildren();
        //System.out.println("Type checking DECL");
        //System.out.println("decl: " + decl.toString());
        typeCheck header = typeCheckHEADER(children.get(0));
        typeCheck body = typeCheckBODY(children.get(1));
        return new typeCheck(header.type && body.type, header.message + " " + body.message);
    }

    private typeCheck typeCheckHEADER(SyntaxTreeNode header) {
        //HEADER->FTYP FNAME ( VNAME , VNAME , VNAME )
        List<SyntaxTreeNode> children = header.getChildren();
        //System.out.println("Type checking HEADER");
        //System.out.println("header: " + header.toString());
        
        if (this.typeOfFNAME(children.get(1)) != this.typeOfFTYP(children.get(0))) {
            return new typeCheck(false, "Type checking failed. Type of your function and return type do not match. \nOccurred at: " + children.get(0).getChildren().get(0).getPosition());
        }
        //Adding the types to the symbol table for the function arguments
        //System.out.println("children.get(3).getChildren().get(0).getTerminal(): " + children.get(3).getChildren().get(0).getTerminalWord());
        SymbolTableAccessor.updateSymbolType(children.get(3).getChildren().get(0).getTerminalWord(),"num");
        SymbolTableAccessor.updateSymbolType(children.get(5).getChildren().get(0).getTerminalWord(),"num");
        SymbolTableAccessor.updateSymbolType(children.get(7).getChildren().get(0).getTerminalWord(), "num");
        
        //System.out.println("Id: " + SymbolTableAccessor.getSymbolId(children.get(3).getChildren().get(0).getTerminalWord()));

        if(this.typeOfVNAME(children.get(3)).equals("n")  && this.typeOfVNAME(children.get(5)).equals("n")  && this.typeOfVNAME(children.get(7)).equals("n") ) {
            return new typeCheck(true, "");
        } else {
            return new typeCheck(false, "Type checking failed. Function arguments must be of type num. \nOccurred at: " + children.get(3).getChildren().get(0).getPosition());
        }
    }

    private String typeOfFTYP(SyntaxTreeNode ftyp) {
        List<SyntaxTreeNode> children = ftyp.getChildren();
        //System.out.println("Type checking FTYP");
        //System.out.println("ftyp: " + ftyp.toString());
        SyntaxTreeNode next = children.get(0);
        //System.out.println("next: " + next.toString());
        if (next.getTerminal() != null && next.getTerminal().contains("<WORD>num</WORD>")) {//Case 1: FTYP -> num 
            //System.out.println("FTYP -> num");
            return "n";
        } else if (next.getTerminal() != null && next.getTerminal().contains("<WORD>void</WORD>")) { //Case 3: FTYP -> void
            //System.out.println("FTYP -> void");
            return "v";
        } else {
            //System.out.println("Returning u from FTYP"); 
            return "u";
        }
    }

    private typeCheck typeCheckBODY(SyntaxTreeNode body) {
        //BODY->PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
        List<SyntaxTreeNode> children = body.getChildren();
        //System.out.println("Type checking BODY");
        //System.out.println("body: " + body.toString());
        typeCheck prolog = typeCheckPROLOG(children.get(0));
        typeCheck locvars = typeCheckLOCVARS(children.get(1));
        typeCheck algo = typeCheckALGO(children.get(2));
        typeCheck epilog = typeCheckEPILOG(children.get(3));
        typeCheck subfuncs = typeCheckSUBFUNCS(children.get(4));

        return new typeCheck(prolog.type && locvars.type && algo.type && epilog.type && subfuncs.type, prolog.message
                + " " + locvars.message + " " + algo.message + " " + epilog.message + " " + subfuncs.message);
    }
    
    private typeCheck typeCheckPROLOG(SyntaxTreeNode prolog) {
        //PROLOG->{
        //System.out.println("Type checking PROLOG");
        //System.out.println("prolog: " + prolog.toString());
        
        return new typeCheck(true, "");
    }

    private typeCheck typeCheckEPILOG(SyntaxTreeNode epilog) {
        //EPILOG->}
        //System.out.println("Type checking EPILOG");
        //System.out.println("epilog: " + epilog.toString());
        
        return new typeCheck(true, "");
    }

    private typeCheck typeCheckLOCVARS(SyntaxTreeNode locvars) {
        //LOCVARS->VTYP VNAME , VTYP VNAME , VTYP VNAME ,
        List<SyntaxTreeNode> children = locvars.getChildren();
        //System.out.println("Type checking LOCVARS");
        //System.out.println("locvars: " + locvars.toString());
        
        if (this.typeOfVNAME(children.get(1)) != this.typeOfVTYP(children.get(0))) {
            return new typeCheck(false, "Type checking failed. The type of the variable does not match the type declared. \nOccurred at: " + children.get(1).getChildren().get(0).getPosition());
        }
        if (this.typeOfVNAME(children.get(4)) != this.typeOfVTYP(children.get(3))) {
            return new typeCheck(false, "Type checking failed. The type of the variable does not match the type declared. \nOccurred at: " + children.get(4).getChildren().get(0).getPosition());
        }
        if (this.typeOfVNAME(children.get(7)) != this.typeOfVTYP(children.get(6))) {
            return new typeCheck(false, "Type checking failed. The type of the variable does not match the type declared. \nOccurred at: " + children.get(7).getChildren().get(0).getPosition());
        }
        return new typeCheck(true, "");
    }

    private typeCheck typeCheckSUBFUNCS(SyntaxTreeNode subfuncs) {
        //SUBFUNCS->FUNCTIONS
        List<SyntaxTreeNode> children = subfuncs.getChildren();
        //System.out.println("Type checking SUBFUNCS");
        //System.out.println("subfuncs: " + subfuncs.toString());
        
        return this.typeCheckFUNCTIONS(children.get(0));
    }

    public static void main(String[] args) {
       // Crawling a tree and printing the nodes
       try {
            String xmlFilePath = "src/parser2/output/output1.xml";
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(xmlFilePath);
            semanticAnalyzer.analyze();
            typeChecker typeChecker = new typeChecker(xmlFilePath);
            typeCheck typeCheck = typeChecker.typeCheck();
            System.out.println("The result of the type checker: " + typeCheck.type + "\n" + typeCheck.message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}