package semanticanalyzer;

import java.util.List;
import java.util.Map;

public class SymbolTableAccessor {
    public static Map<String, List<Symbol>> getSymbolTable() {
        return SymbolTable.getInstance().getAllSymbols();
    }

    public static Symbol lookupVariable(String name, String scope) {
        return SymbolTable.getInstance().lookupVariable(name, false);
    }

    public static Symbol lookupVariableInCurrentScope(String name, String scope) {
        return SymbolTable.getInstance().lookupVariable(name, true);
    }

    public static Symbol lookupFunction(String name) {
        return SymbolTable.getInstance().lookupFunction(name);
    }
}