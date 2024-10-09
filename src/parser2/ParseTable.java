package parser2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    
    private void populateGrammar()
    {
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader("src/parser2/input/grammar.txt"))) {
            while ((line = br.readLine()) != null) {
                grammar.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateTerminals()
    {
        //reading terminals form a text file
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader("src/parser2/input/terminals.txt"))) {
            while ((line = br.readLine()) != null) {
                terminal.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateNonTerminals()
    {
        //reading non-terminals form a text file
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader("src/parser2/input/nonTerminals.txt"))) {
            while ((line = br.readLine()) != null) {
                nonTerminal.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void populateActionTable()
    {
        try (BufferedReader br = new BufferedReader(new FileReader("src/parser2/input/actionTable.csv"))) {
            String line;
            
            // Reading each line of the CSV file
            while ((line = br.readLine()) != null) {
                // Split the line by semicolon
                String[] entries = line.split(";", -1);  // -1 ensures that trailing empty values are preserved
                
                // Replace empty entries with null
                for (int i = 0; i < entries.length; i++) {
                    if (entries[i].trim().isEmpty()) {
                        entries[i] = null;
                    }
                }
                
                // Add the array to the actionTable
                actionTable.add(entries);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateGotoTable()
    {
        try (BufferedReader br = new BufferedReader(new FileReader("src/parser2/input/gotoTable.csv"))) {
            String line;
            
            // Reading each line of the CSV file
            while ((line = br.readLine()) != null) {
                // Split the line by semicolon
                String[] entries = line.split(";", -1);  // -1 ensures that trailing empty values are preserved
                
                List<Integer> row = new ArrayList<>();
                
                // Replace empty entries with null and parse integers
                for (String entry : entries) {
                    if (entry.trim().isEmpty()) {
                        row.add(null);  // Add null for empty entries
                    } else {
                        row.add(Integer.parseInt(entry));  // Parse the integer
                    }
                }
                
                // Add the row to the gotoTable
                gotoTable.add(row);
            }
        } catch (IOException e) {
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
