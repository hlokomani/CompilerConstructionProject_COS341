package semanticanalyzer;

import java.util.*;

public class SymbolTable {
    private static SymbolTable instance = null;
    private Stack<String> scopes;
    private Map<String, List<Symbol>> symbolMap;

    private SymbolTable() {
        this.scopes = new Stack<>();
        this.symbolMap = new HashMap<>();
    }

    public static SymbolTable getInstance() {
        if (instance == null) {
            instance = new SymbolTable();
        }
        return instance;
    }

    public void clear() {
        scopes.clear();
        symbolMap.clear();
    }

    public void enterScope(String scopeName) {
        scopes.push(scopeName);
    }

    public void exitScope() {
        scopes.pop();
    }

    public void addSymbol(Symbol symbol) {
        String key = scopes.peek() + ":" + symbol.getName();
        symbolMap.computeIfAbsent(key, k -> new ArrayList<>()).add(symbol);
    }

    public Symbol lookupVariable(String name, boolean currentScopeOnly) {
        if (currentScopeOnly) {
            String key = scopes.peek() + ":" + name;
            List<Symbol> symbols = symbolMap.get(key);
            return (symbols != null && !symbols.isEmpty()) ? symbols.get(0) : null;
        } else {
            for (int i = scopes.size() - 1; i >= 0; i--) {
                String key = scopes.get(i) + ":" + name;
                List<Symbol> symbols = symbolMap.get(key);
                if (symbols != null && !symbols.isEmpty()) {
                    return symbols.get(0);
                }
            }
        }
        return null;
    }

    public Symbol lookupFunction(String name) {
        String key = "global:" + name;
        List<Symbol> symbols = symbolMap.get(key);
        return (symbols != null && !symbols.isEmpty()) ? symbols.get(0) : null;
    }

    public Map<String, List<Symbol>> getAllSymbols() {
        return new HashMap<>(symbolMap);
    }
}