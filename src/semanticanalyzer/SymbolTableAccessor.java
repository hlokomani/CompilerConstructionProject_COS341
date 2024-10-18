package semanticanalyzer;

import java.util.List;
import java.util.Map;

public class SymbolTableAccessor {
    public static Map<String, List<Symbol>> getSymbolTable() {
        return SymbolTable.getInstance().getAllSymbols();
    }

    public static Symbol lookupVariable(String name) {
        return SymbolTable.getInstance().lookupVariable(name, false);
    }

    public static Symbol lookupFunction(String name) {
        return SymbolTable.getInstance().lookupFunction(name);
    }

    public static int getSymbolId(String name) {
        Symbol symbol = lookupVariable(name);
        if (symbol != null) {
            return symbol.getUnid();
        }
        symbol = lookupFunction(name);
        if (symbol != null) {
            return symbol.getUnid();
        }
        return -1; // Return -1 if symbol is not found
    }

    public static String getSymbolType(String name) {
        Symbol symbol = lookupVariable(name);
        if (symbol != null) {
            return symbol.getType();
        }
        symbol = lookupFunction(name);
        if (symbol != null) {
            return symbol.getType();
        }
        return null; // Return null if symbol is not found
    }

    public static String getKind(String name) {
        Symbol symbol = lookupVariable(name);
        if (symbol != null) {
            return symbol.getKind();
        }
        symbol = lookupFunction(name);
        if (symbol != null) {
            return symbol.getKind();
        }
        return null;
    }

    public static boolean updateSymbolType(String name, String newType) {
        return SymbolTable.getInstance().updateSymbolType(name, newType);
    }
}