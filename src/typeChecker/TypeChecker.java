package typeChecker;

import java.util.HashMap;
import java.util.Map;

import parser.SyntaxTreeBuilder;
import parser2.SyntaxTreeNode;

public class TypeChecker {
    private Map<String, Character> symbolTable = new HashMap<>(); //Needs to be replaced by actual SymbolTable
    private TreeCrawler treeCrawler;
    
    public TypeChecker(String xmlFilePath) throws Exception {
        //Initialize symbol table 
        treeCrawler = new TreeCrawler(xmlFilePath);
    }

    public boolean typecheckProg() throws Exception {
        boolean result = true;
        treeCrawler.getNext(); //PROG
        treeCrawler.getNext(); //main
        treeCrawler.getNext(); //GLOBVARS
        result = result && typecheckGLOBVARS();
        treeCrawler.getNext(); //ALGO
        result = result && typecheckALGO();
        treeCrawler.getNext(); //FUNCTIONS
        result = result && typecheckFUNCTIONS();
        return result;
    }

    public boolean typecheckGLOBVARS() {
        SyntaxTreeNode node = treeCrawler.getNext();
        boolean result = true;
        //Case 1: GLOBVARS -> 
        if (node.getTerminal() == "") { //Not sure if this will work
            return result;
        } else {//Case 2: GLOBVARS -> VTYP VNAME , GLOBVARS
            SyntaxTreeNode vtyp = treeCrawler.getNext();
            SyntaxTreeNode vname = treeCrawler.getNext();
            String T = typeOfVTYP(vtyp);
            String id = symbolTable.get(vname.getTerminal());
            //Linking the type to the variable in the table
            symbolTable.put(T, id);
            if (this.typeOfVNAME(vname) != this.typeOfVTYP(vtyp)) {
                result = false;
            }
            return typecheckGLOBVARS();
        }
    }

    public String typeOfVTYP(SyntaxTreeNode node) {
        if(node.getTerminal()=="num") { //Case 1: VTYP -> num
            return "n";
        } else if(node.getTerminal()=="text") { //Case 2: VTYP -> text
            return "t";
        } else {
            return "u";
        }
    }
    
    public String typeOfVNAME(SyntaxTreeNode node) {
        return node.getTerminal();

    }
    
    public boolean typecheckALGO() {
        // ALGO -> begin INSTRUC end
        treeCrawler.getNext(); //begin
        boolean result = typecheckINSTRUC();
        treeCrawler.getNext(); //end
        return result;
    }

    public boolean typecheckINSTRUC() {
        SyntaxTreeNode node = treeCrawler.getNext();
        if (node.getTerminal() == "") { //Case 1: INSTRUC -> 
            return true;
        } else { //Case 2: INSTRUC -> COMMAND ; INSTRUC
            return typecheckCOMMAND() && typecheckINSTRUC();
        }        
    }

    public boolean typecheckCOMMAND() {
        SyntaxTreeNode node = treeCrawler.getNext();
        //Case 1: COMMAND -> skip
        if (node.getTerminal() == "skip") {
            return true;
        }

        //Case 2: COMMAND -> halt
        if (node.getTerminal() == "halt") {
            return true;
        }

        //Case 3: COMMAND -> print ATOMIC
        if (node.getTerminal() == "print") {
            SyntaxTreeNode atomic = treeCrawler.getNext();
            if (typeOfATOMIC(atomic) == "n") {
                return true;
            } else if (typeOfATOMIC(atomic) == "t") {
                return true;
            } else {
                return false;
            }
        }

        //Case 4: COMMAND -> return ATOMIC
        if (node.getTerminal() == "return") {
            //TODO: Find X
            SyntaxTreeNode X;

            if (typeOfATOMIC(node) == typeOf(X) && typeOfATOMIC(node) == "n") {
                return true;
            } else {
                return false;
            }
        }

        //Case 5: COMMAND -> ASSIGN
        if (node.getSymb() == "ASSIGN") {
            return typecheckASSIGN();
        }

        //Case 6: COMMAND -> CALL
        if (node.getSymb() == "CALL") {
            SyntaxTreeNode call = treeCrawler.getNext();
            if (this.typeOfCall(call) == "v") {
                return true;
            } else {
                return false;
            }
        }

        //Case 7: COMMAND -> BRANCH
        if (node.getSymb() == "BRANCH") {
            return typecheckBRANCH();
        }
    }
    
    public String typeOf(SyntaxTreeNode node) {
                
    }

    public String typeOfATOMIC(SyntaxTreeNode node) {
        if(node.getSymb() == "VNAME") { //Case 1: ATOMIC -> VNAME
            return this.typeOfVNAME(node);
        } else if(node.getSymb() == "CONST") { //Case 2: ATOMIC -> CONST
            return this.typeOfCONST(node);
        } else {
            return "u";
        }        
    }

    public String typeOfCONST(SyntaxTreeNode node) {
        if(node.getTerminal() == "TokenN") { //Case 1: CONST -> TokenN
            return "n";
        } else if(node.getTerminal() == "TokenT") { //Case 2: CONST -> TokenT
            return "t";
        } else {
            return "u";
        }
    }

    public boolean typecheckASSIGN() {
        SyntaxTreeNode node1 = treeCrawler.getNext();
        SyntaxTreeNode node2 = treeCrawler.getNext();
        if (node2.getTerminal() == "<") {
            treeCrawler.getNext(); //input
            //Case 1: ASSIGN -> VNAME < input
            if(this.typeOfVNAME(node1) == "n") {
                return true;
            } else {
                return false;
            }
        } else if (node2.getTerminal() == ":=") {
            //Case 2: ASSIGN -> VNAME := TERM
            SyntaxTreeNode node3 = treeCrawler.getNext();
            if(this.typeOfVNAME(node1) == this.typeOfTerm(node3)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String typeOfTERM(SyntaxTreeNode node) {  
        if(node.getSymb() == "ATOMIC") { //Case 1: TERM -> ATOMIC
            return this.typeOfATOMIC(node);
        }else if(node.getSymb() == "CALL") { //Case 2: TERM -> CALL
            return this.typeOfCall(node);
        } else if (node.getSymb() == "OP") { //Case 3: TERM -> OP
            return this.typeOfOP(node);
        }else {
            return "u";
        }
    }

    public String typeOfCall(SyntaxTreeNode node) {
        SyntaxTreeNode fname = treeCrawler.getNext();
        treeCrawler.getNext(); //(
        SyntaxTreeNode atomic1 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode atomic2 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode atomic3 = treeCrawler.getNext();
        treeCrawler.getNext(); //)

        if(typeOfATOMIC(atomic1) == "n" && typeOfATOMIC(atomic2) == "n" && typeOfATOMIC(atomic3) == "n") {
            return typeOfFNAME(fname);
        } else {
            return "u";
        }
    }
    
    public String typeOfOp(SyntaxTreeNode node) {
        SyntaxTreeNode node2 = treeCrawler.getNext();
        if(node2.getSymb() == "UNOP") { //Case 1: OP -> UNOP
            treeCrawler.getNext(); //(
            SyntaxTreeNode arg = treeCrawler.getNext();
            treeCrawler.getNext(); //)

            if( this.typeOfUNOP(node2) == "b" && this.typeOfArg(arg) == "b") {
                return "n";
            } else {
                return "u";
            }
        } else if(node2.getSymb() == "BINOP") { //Case 2: OP -> BINOP
            treeCrawler.getNext(); //(
            SyntaxTreeNode arg1 = treeCrawler.getNext();
            treeCrawler.getNext(); //,
            SyntaxTreeNode arg2 = treeCrawler.getNext();
            treeCrawler.getNext(); //)

            if (this.typeOfBINOP(node2) == "b" && this.typeOfArg(arg1) == "b" && this.typeOfArg(arg2) == "b") {
                return "b";
            } else if(this.typeOfBINOP(node2) == "n" && this.typeOfArg(arg1) == "n" && this.typeOfArg(arg2) == "n") {
                return "n";
            } else if(this.typeOfBINOP(node2) == "c" && this.typeOfArg(arg1) == "n" && this.typeOfArg(arg2) == "n") {
                return "b";
            } else {
                return "u";
            }
        } else {
            return "u";
        }
    }

    public String typeOfArg(SyntaxTreeNode node) {
        if (node.getSymb() == "ATOMIC") { //Case 1: ARG -> ATOMIC
            return this.typeOfATOMIC(node);
        } else if(node.getSymb() == "OP") { //Case 2: ARG -> OP
            return this.typeOfOp(node);
        } else {
            return "u";
        }       
    }

    public String typeOfUNOP(SyntaxTreeNode node) {
        if (node.getTerminal() == "not") { //Case 1: UNOP -> not
            return "b";
        } else if (node.getTerminal() == "sqrt") { //Case 2: UNOP -> sqrt
            return "n";
        } 
        else {
            return "u";
        }
    }

    public String typeOfBINOP(SyntaxTreeNode node) {
        if (node.getTerminal() == "or") { //Case 1: BINOP -> or
            return "b";
        } else if (node.getTerminal() == "and") { //Case 2: BINOP -> and
            return "b";
        } else if (node.getTerminal() == "eq") { //Case 3: BINOP -> eq
            return "c";
        } else if (node.getTerminal() == "grt") { //Case 4: BINOP -> grt
            return "c";
        } else if (node.getTerminal() == "add") { //Case 5: BINOP -> add
            return "n";
        } else if (node.getTerminal() == "sub") { //Case 6: BINOP -> sub
            return "n";
        } else if (node.getTerminal() == "mul") { //Case 7: BINOP -> mul
            return "n";
        } else if (node.getTerminal() == "div") { //Case 8: BINOP -> div
            return "n";
        } else {
            return "u";
        }
    }

    public boolean typecheckBRANCH() {
        SyntaxTreeNode node = treeCrawler.getNext();
        treeCrawler.getNext(); //if
        SyntaxTreeNode cond = treeCrawler.getNext();
        treeCrawler.getNext(); //then
        
        if( this.typeOfCond(cond) == "b") {
            return typecheckALGO() && typecheckALGO();
        } else {
            return false;
        }
    }

    public String typeOfCond(SyntaxTreeNode node) {
        if(node.getSymb() == "SIMPLE") { //Case 1: COND -> SIMPLE
            return this.typeOfSIMPLE(node);
        } else if(node.getSymb() == "COMPOSIT") { //Case 2: COND -> COMPOSIT
            return this.typeOfCOMPOSIT(node);
        } else {
            return "u";
        }
    }

    public String typeOfSIMPLE(SyntaxTreeNode node) {
        SyntaxTreeNode binop = treeCrawler.getNext();
        treeCrawler.getNext(); //(
        SyntaxTreeNode atmoic1 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode atmoic2 = treeCrawler.getNext();
        treeCrawler.getNext(); //)

        if(this.typeOfBINOP(binop) == "b" && this.typeOfATOMIC(atmoic1) == "b" && this.typeOfATOMIC(atmoic2) == "b") {
            return "b";
        } else if(this.typeOfBINOP(binop) == "c" && this.typeOfATOMIC(atmoic1) == "n" && this.typeOfATOMIC(atmoic2) == "n") {
            return "b";
        } else {
            return "u";
        }
    }

    public String typeOfCOMPOSIT(SyntaxTreeNode node) {
        if (node.getSymb() == "BINOP") { //Case 1: COMPOSIT -> BINOP ( SIMPLE , SIMPLE )
            treeCrawler.getNext(); //(
            SyntaxTreeNode simple1 = treeCrawler.getNext();
            treeCrawler.getNext(); //,
            SyntaxTreeNode simple2 = treeCrawler.getNext();
            treeCrawler.getNext(); //)

            if(this.typeOfBINOP(node)== "b" && this.typeOfSIMPLE(simple1) == "b" && this.typeOfSIMPLE(simple2) == "b") {
                return "b";
            } else {
                return "u";
            }

        } else if (node.getSymb() == "UNOP") {//Case 2: COMPOSIT -> UNOP ( SIMPLE )
            treeCrawler.getNext(); //(
            SyntaxTreeNode simple = treeCrawler.getNext();
            treeCrawler.getNext(); //)

            if(this.typeOfUNOP(node) == "b" && this.typeOfSIMPLE(simple) == "b") {
                return "b";
            } else {
                return "u";
            }
        } else {
            return "u";
        }
    }

    public String typeOfFNAME(SyntaxTreeNode node) {
    }

    public boolean typecheckFUNCTIONS() {
        SyntaxTreeNode node = treeCrawler.getNext();
        if (node.getTerminal() == "") { //Case 1: FUNCTIONS -> 
            return true;
        } else if(node.getSymb() == "DECL") { //Case 2: FUNCTIONS -> DECL FUNCTIONS
            return typecheckDECL() && typecheckFUNCTIONS();
        } else {
            return false;
        }
    }

    public boolean typecheckDECL() {
        SyntaxTreeNode node = treeCrawler.getNext();
        return typecheckHEADER() && typecheckBODY();
    }

    public boolean typecheckHEADER() {
        SyntaxTreeNode node = treeCrawler.getNext();
        SyntaxTreeNode ftyp = treeCrawler.getNext();
        SyntaxTreeNode fname = treeCrawler.getNext();
        treeCrawler.getNext(); //(
        SyntaxTreeNode vname1 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode vname2 = treeCrawler.getNext();
        treeCrawler.getNext(); //,
        SyntaxTreeNode vname3 = treeCrawler.getNext();
        treeCrawler.getNext(); //)

        String t = typeOfFTYP(ftyp);
        String id = symbolTable.get(fname.getTerminal());
        symbolTable.put(t, id);
        if (this.typeOfFNAME(fname) != this.typeOfFTYP(ftyp)) {
            return false;
        }
        if(this.typeOfVNAME(vname1) == "n" && this.typeOfVNAME(vname2) == "n" && this.typeOfVNAME(vname3) == "n") {
            return true;
        } else {
            return false;
        }
    }

    public String typeOfFTYP(SyntaxTreeNode node) {
        if(node.getTerminal() == "num") {//Case 1: FTYP -> num 
            return "n";
        } else if(node.getTerminal() == "text") { //Case 2: FTYP -> text
            return "t";
        } else {
            return "u";
        }
    }

    public boolean typecheckBODY() {
        treeCrawler.getNext();
        return this.typecheckPROLOG() && this.typecheckLOCVARS() && this.typecheckALGO() &&  this.typecheckEPILOG() && this.typecheckSUBFUNCS();
    }
    
    public boolean typecheckPROLOG() {
        treeCrawler.getNext();
        return true;
    }

    public boolean typecheckEPILOG() {
        treeCrawler.getNext();
        return true;
    }

    public boolean typecheckLOCVARS() {
        SyntaxTreeNode node = treeCrawler.getNext();
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
        String id = symbolTable.get(vname1.getTerminal());
        symbolTable.put(t, id);
        if (this.typeOfVNAME(vname1) != this.typeOfVTYP(vtype1)) {
            return false;
        }
        
        t = typeOfVTYP(vtype2);
        id = symbolTable.get(vname2.getTerminal());
        symbolTable.put(t, id);
        if (this.typeOfVNAME(vname2) != this.typeOfVTYP(vtype2)) {
            return false;
        }

        t = typeOfVTYP(vtype3);
        id = symbolTable.get(vname3.getTerminal());
        symbolTable.put(t, id);
        if (this.typeOfVNAME(vname3) != this.typeOfVTYP(vtype3)) {
            return false;
        }
        return true;
    }

    public boolean typecheckSUBFUNCS() {
        treeCrawler.getNext();
        return this.typecheckFUNCTIONS();
    }
}

