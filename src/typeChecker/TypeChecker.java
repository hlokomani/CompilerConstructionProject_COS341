package typeChecker;

import java.util.HashMap;
import java.util.Map;

public class TypeChecker {
    private Map<String, Character> symbolTable = new HashMap<>(); //Needs to be replaced by actual SymbolTable
    private TreeCrawler treeCrawler;
    
    public TypeChecker() {
        //Initialize symbol table 
    }

    public boolean typecheckProg() {

        return false;
    }

    public boolean typecheckGLOBVARS() {
        //Case 1: GLOBVARS -> 

        //Case 2: GLOBVARS -> VTYP VNAME , GLOBVARS
        return false;
    }

    public boolean typecheckVTYP() {
        //Case 1: VTYP -> num
        //Case 2: VTYP -> text
        return false;
    }
    
    public String typeOfVNAME() {

        return "";
    }
    
    public boolean typecheckALGO() {
        // ALGO -> begin INSTRUC end
        return false;
    }

    public boolean typecheckINSTRUC() {
        //Case 1: INSTRUC -> 

        //Case 2: INSTRUC -> COMMAND ; INSTRUC
        return false;
    }

    public boolean typecheckCOMMAND() {
        //Case 1: COMMAND -> skip

        //Case 2: COMMAND -> halt

        //Case 3: COMMAND -> print ATOMIC

        //Case 4: COMMAND -> return ATOMIC

        //Case 5: COMMAND -> ASSIGN

        //Case 6: COMMAND -> CALL

        //Case 7: COMMAND -> BRANCH
        return false;
    }

    public boolean typecheckATOMIC() {
        //Case 1: ATOMIC -> VNAME

        //Case 2: ATOMIC -> CONST
        return false;
    }

    public boolean typecheckCONST() {
        //Case 1: CONST -> TokenN

        //Case 2: CONST -> TokenT
        return false;
    }

    public boolean typecheckASSIGN() {
        //Case 1: ASSIGN -> VNAME < input
        
        //Case 2: ASSIGN -> VNAME := TERM
        return false;
    }

    public boolean typecheckTERM() {
        //Case 1: TERM -> ATOMIC

        //Case 2: TERM -> CALL

        //Case 3: TERM -> OP
        return false;
    }

    public boolean typecheckCALL() {
        return false;
    }

    public boolean typecheckARG() {
        //Case 1: ARG -> ATOMIC

        //Case 2: ARG -> OP
        return false;
    }

    public boolean typecheckUNOP() {
        //Case 1: UNOP -> not

        //Case 2: UNOP -> sqrt
        return false;
    }

    public boolean typecheckOP() {
        return false;
    }

    public boolean typecheckBINOP() {
        //Case 1: BINOP -> or

        //Case 2: BINOP -> and

        //Case 3: BINOP -> eq

        //Case 4: BINOP -> grt

        //Case 5: BINOP -> add

        //Case 6: BINOP -> sub

        //Case 7: BINOP -> mul

        //Case 8: BINOP -> div
        return false;
    }

    public boolean typecheckBRANCH() {
        return false;
    }

    public boolean typecheckCOND() {
        //Case 1: COND -> SIMPLE

        //Case 2: COND -> COMPOSIT
        return false;
    }

    public boolean typecheckSIMPLE() {
        return false;
    }

    public boolean typecheckCOMPOSIT() {
        //Case 1: COMPOSIT -> BINOP ( SIMPLE , SIMPLE )

        //Case 2: COMPOSIT -> UNOP ( SIMPLE )
        return false;
    }

    public String typeOfFNAME() {
        return "";
    }

    public boolean typecheckFUNCTIONS() {
        //Case 1: FUNCTIONS -> 

        //Case 2: FUNCTIONS -> DECL FUNCTIONS
        return false;
    }

    public boolean typecheckDECL() {
        return false;
    }

    public boolean typecheckHEADER() {
        return false;
    }

    public boolean typecheckFTYP() {
        //Case 1: FTYP -> num 

        //Case 2: FTYP -> void 
        return false;
    }

    public boolean typecheckBODY() {
        return false;
    }
    
    public boolean typecheckPROLOG() {
        return false;
    }

    public boolean typecheckEPILOG() {
        return false;
    }

    public boolean typecheckLOCVARS() {
        return false;
    }

    public boolean typecheckSUBFUNCS() {
        return false;
    }
}

