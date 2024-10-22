package semanticanalyzer;

import java.util.*;

public class SymbolTable {
    private static SymbolTable instance = null;
    private Stack<String> scopes;
    private Map<String, List<Symbol>> symbolMap;
    private List<Symbol> globalSymbols;

    private SymbolTable() {
        this.scopes = new Stack<>();
        this.symbolMap = new HashMap<>();
        this.globalSymbols = new ArrayList<>();
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
        globalSymbols.clear();
    }

    public void enterScope(String scopeName) {
        scopes.push(scopeName);
    }

    public void exitScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }

    public void addSymbol(Symbol symbol, boolean isGlobal) {
        if (isGlobal) {
            globalSymbols.add(symbol);
        } else {
            String key = getCurrentScopeKey() + ":" + symbol.getName();
            symbolMap.computeIfAbsent(key, k -> new ArrayList<>()).add(symbol);
        }
    }

    public Symbol lookupVariable(String name, boolean currentScopeOnly) {
        if (currentScopeOnly) {
            if (scopes.isEmpty() || scopes.peek().equals("global")) {
                // We're in the global scope, so check global symbols
                for (Symbol symbol : globalSymbols) {
                    if (symbol.getName().equals(name)) {
                        return symbol;
                    }
                }
            } else {
                // We're in a local scope, so check only that scope
                String key = getCurrentScopeKey() + ":" + name;
                List<Symbol> symbols = symbolMap.get(key);
                if (symbols != null && !symbols.isEmpty()) {
                    return symbols.get(0);
                }
            }
        } else {
            // Check global symbols first
            for (Symbol symbol : globalSymbols) {
                if (symbol.getName().equals(name)) {
                    return symbol;
                }
            }
            // Then check local scopes
            for (int i = scopes.size() - 1; i >= 0; i--) {
                String key = scopes.get(i) + ":" + name;
                List<Symbol> symbols = symbolMap.get(key);
                if (symbols != null && !symbols.isEmpty()) {
                    return symbols.get(0);
                }
            }

            for (String scope : symbolMap.keySet()) {
                if (scope.endsWith(":" + name)) {
                    List<Symbol> scopedSymbols = symbolMap.get(scope);
                    if (scopedSymbols != null && !scopedSymbols.isEmpty()) {
                        return scopedSymbols.get(0);
                    }
                }
            }
        }
        return null;
    }

    public boolean isGlobalVariable(String name) {
        for (Symbol symbol : globalSymbols) {
            if (symbol.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Symbol lookupFunction(String name) {
        // Check global symbols first (where functions should be)
        for (Symbol symbol : globalSymbols) {
            if (symbol.getName().equals(name) && symbol.getKind().equals("function")) {
                return symbol;
            }
        }

        // Also check symbol map with global scope
        String key = "global:" + name;
        List<Symbol> symbols = symbolMap.get(key);
        if (symbols != null && !symbols.isEmpty()) {
            Symbol symbol = symbols.get(0);
            if (symbol.getKind().equals("function")) {
                return symbol;
            }
        }
        return null;
    }

    public Symbol lookupParameter(String name, String functionScope) {
        String key = functionScope + ":" + name;
        List<Symbol> symbols = symbolMap.get(key);
        if (symbols != null && !symbols.isEmpty()) {
            Symbol symbol = symbols.get(0);
            if (symbol.getKind().equals("parameter")) {
                return symbol;
            }
        }
        return null;
    }

    public Map<String, List<Symbol>> getAllSymbols() {
        Map<String, List<Symbol>> allSymbols = new HashMap<>(symbolMap);
        allSymbols.put("global", globalSymbols);
        return allSymbols;
    }

    private String getCurrentScopeKey() {
        return scopes.isEmpty() ? "global" : scopes.peek();
    }

    public boolean updateSymbolType(String name, String newType) {
        //check global symbols first
        for (Symbol symbol : globalSymbols) {
            if (symbol.getName().equals(name)) {
                symbol.setType(newType);
                return true;
            }
        }

        //then check local scopes
        for (int i = scopes.size() - 1; i >= 0; i--) {
            String key = scopes.get(i) + ":" + name;
            List<Symbol> symbols = symbolMap.get(key);
            if (symbols != null && !symbols.isEmpty()) {
                symbols.get(0).setType(newType);
                return true;
            }
        }

        return false; //symbol not found
    }

}