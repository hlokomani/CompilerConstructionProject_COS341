package semanticanalyzer;

import java.util.List;
import java.util.Map;

public class SymbolTableAccessor {
    public static Map<String, List<Symbol>> getSymbolTable() {
        return SymbolTable.getInstance().getAllSymbols();
    }

    public static Symbol lookupVariable(String name, String scope) {
        List<Symbol> symbols = SymbolTable.getInstance().getAllSymbols().get(name + ":" + scope);
        return (symbols != null && !symbols.isEmpty()) ? symbols.get(0) : null;
    }

    public static Symbol lookupFunction(String name) {
        return SymbolTable.getInstance().lookupFunction(name);
    }
}