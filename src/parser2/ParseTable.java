package parser2;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParseTable {

    List<String[]> actionTable = new ArrayList<>();
    List<List<Integer>> gotoTable = new ArrayList<>();
    List<String> grammar = new ArrayList<>();
    List<String> terminal = new ArrayList<>();
    List<String> nonTerminal = new ArrayList<>();

    public ParseTable() {
        populateGrammar();
        populateTerminals();
        populateNonTerminals();
        populateActionTable();
        populateGotoTable();
    }

    private java.io.InputStream getResourceStream(String resourcePath) throws IOException {
        // Try to load from resources first
        java.io.InputStream is = getClass().getResourceAsStream(resourcePath);

        // If not found in resources, try to load from file system (for development)
        if (is == null) {
            is = getClass().getResourceAsStream("/parser2/input/" + resourcePath);
        }

        // If still not found, try direct file access (for development)
        if (is == null) {
            File file = new File("src/parser2/input/" + resourcePath);
            if (file.exists()) {
                is = new FileInputStream(file);
            }
        }

        if (is == null) {
            throw new IOException("Could not find resource: " + resourcePath);
        }

        return is;
    }

    private void populateGrammar() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getResourceStream("grammar.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                grammar.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error loading grammar.txt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateTerminals() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getResourceStream("terminals.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                terminal.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error loading terminals.txt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateNonTerminals() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getResourceStream("nonTerminals.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                nonTerminal.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error loading nonTerminals.txt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateActionTable() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getResourceStream("actionTable.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] entries = line.split(";", -1);
                for (int i = 0; i < entries.length; i++) {
                    if (entries[i].trim().isEmpty()) {
                        entries[i] = null;
                    }
                }
                actionTable.add(entries);
            }
        } catch (IOException e) {
            System.err.println("Error loading actionTable.csv: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateGotoTable() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getResourceStream("gotoTable.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] entries = line.split(";", -1);
                List<Integer> row = new ArrayList<>();
                for (String entry : entries) {
                    if (entry.trim().isEmpty()) {
                        row.add(null);
                    } else {
                        row.add(Integer.parseInt(entry));
                    }
                }
                gotoTable.add(row);
            }
        } catch (IOException e) {
            System.err.println("Error loading gotoTable.csv: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getAction(int state, String terminal) {
        int index = -1;
        for (int i = 0; i < this.terminal.size(); i++) {
            if (this.terminal.get(i).equals(terminal)) {
                index = i;
                break;
            }
        }
        // System.out.println("State: " + state + " Terminal: " + terminal + " Index: " + index);
        return actionTable.get(state)[index];
    }

    public int getGoto(int state, String nonTerminal) {
        // System.out.println("State: " + state + " NonTerminal: " + nonTerminal);
        int index = -1;
        for (int i = 0; i < this.nonTerminal.size(); i++) {
            if (this.nonTerminal.get(i).equals(nonTerminal)) {
                index = i;
                break;
            }
        }
        // System.out.println("Index: " + index);
        // System.out.println("Goto: " + gotoTable.get(state).get(index));
        if(gotoTable.get(state).get(index) == null) {
            return -1;
        }
        return gotoTable.get(state).get(index);
    }

    public String getRule(int index) {
        return grammar.get(index);
    }

    public String getLHS(int index) {
        return grammar.get(index).split("->")[0];
    }

    public String getRHS(int index) {
        return grammar.get(index).split("->")[1];
    }

    public int numSymbolsRHS(int index) {
        //checking if there is something after -> in the grammar
        if (grammar.get(index).split("->").length < 2) {
            return 0;
        }
        return grammar.get(index).split("->")[1].split(" ").length;
    }

    public void printGrammar() {
        System.out.println("Grammar:");
        for (String s : grammar) {
            System.out.println(s);
        }
        System.out.println();
    }

    public void printTerminals() {
        System.out.println("Terminals:");
        for (String s : terminal) {
            System.out.println(s);
        }
        System.out.println();
    }

    public void printNonTerminals() {
        System.out.println("Non-Terminals:");
        for (String s : nonTerminal) {
            System.out.println(s);
        }
        System.out.println();
    }

    public void printActionTable() {
        System.out.println("Action Table:");
        for (String[] s : actionTable) {
            for (String t : s) {
                System.out.print(t + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void printGotoTable() {
        System.out.println("Goto Table:");
        for (List<Integer> s : gotoTable) {
            for (Integer t : s) {
                System.out.print(t + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    //  public static void main(String[] args) {
    //     ParseTable parseTable = new ParseTable();
        
    //     // parseTable.printGrammar();
    //     // parseTable.printTerminals();
    //     // parseTable.printNonTerminals();
    //     // parseTable.printActionTable();
    //     // parseTable.printGotoTable();
    // }
}
