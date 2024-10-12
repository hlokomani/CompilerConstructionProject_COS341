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

    }

    public boolean typecheckGLOBVARS() {
        //Case 1: GLOBVARS -> 

        //Case 2: GLOBVARS -> VTYP VNAME , GLOBVARS
    }

    public boolean typecheckVTYP() {
        //Case 1: VTYP -> num
        //Case 2: VTYP -> text
    }
    
    public String typeOfVNAME() {

    }
    
    public boolean typecheckALGO() {
        // ALGO -> begin INSTRUC end
    }

    public boolean typecheckINSTRUC() {
        //Case 1: INSTRUC -> 

        //Case 2: INSTRUC -> COMMAND ; INSTRUC
    }

    public boolean typecheckCOMMAND() {
        //Case 1: COMMAND -> skip

        //Case 2: COMMAND -> halt

        //Case 3: COMMAND -> print ATOMIC

        //Case 4: COMMAND -> return ATOMIC

        //Case 5: COMMAND -> ASSIGN

        //Case 6: COMMAND -> CALL

        //Case 7: COMMAND -> BRANCH
    }

    public boolean typecheckATOMIC() {
        //Case 1: ATOMIC -> VNAME

        //Case 2: ATOMIC -> CONST
    }

    public boolean typecheckCONST() {
        //Case 1: CONST -> TokenN

        //Case 2: CONST -> TokenT
    }

    public boolean typecheckASSIGN() {
        //Case 1: ASSIGN -> VNAME < input
        
        //Case 2: ASSIGN -> VNAME := TERM
    }

    public boolean typecheckTERM() {
        //Case 1: TERM -> ATOMIC

        //Case 2: TERM -> CALL

        //Case 3: TERM -> OP
    }

    public boolean typecheckCALL() {
    }

    public boolean typecheckARG() {
        //Case 1: ARG -> ATOMIC

        //Case 2: ARG -> OP
    }

    public boolean typecheckUNOP() {
        //Case 1: UNOP -> not

        //Case 2: UNOP -> sqrt
    }

    public boolean typecheckOP() {
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
    }

    public boolean typecheckBRANCH() {
    }

    public boolean typecheckCOND() {
        //Case 1: COND -> SIMPLE

        //Case 2: COND -> COMPOSIT
    }

    public boolean typecheckSIMPLE() {
    }

    public boolean typecheckCOMPOSIT() {
        //Case 1: COMPOSIT -> BINOP ( SIMPLE , SIMPLE )

        //Case 2: COMPOSIT -> UNOP ( SIMPLE )
    }

    public String typeOfFNAME() {
    }

    public boolean typecheckFUNCTIONS() {
        //Case 1: FUNCTIONS -> 

        //Case 2: FUNCTIONS -> DECL FUNCTIONS
    }

    public boolean typecheckDECL() {
    }

    public boolean typecheckHEADER() {
    }

    public boolean typecheckFTYP() {
        //Case 1: FTYP -> num 

        //Case 2: FTYP -> void 
    }

    public boolean typecheckBODY() {
    }
    
    public boolean typecheckPROLOG() {
    }

    public boolean typecheckEPILOG() {
    }

    public boolean typecheckLOCVARS() {
    }

    public boolean typecheckSUBFUNCS() {
    }
}

