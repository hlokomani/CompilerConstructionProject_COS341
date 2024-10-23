package targetCodeGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import parser2.SyntaxTreeNode;
import semanticanalyzer.SemanticAnalyzer;
import semanticanalyzer.SymbolTableAccessor;
import typeChecker.TreeCrawler;

public class targetCodeGenerator {
    private FileWriter outputFile;
    private int lineNumber = 10;  // Start from line 10
    private String inputFilePath, outputFilePath;
    private Map<String, String> tempVarMap = new HashMap<>();
    private Map<String, Integer> labelLineNumbers = new HashMap<>();
    private List<String> generatedCode = new ArrayList<>();
    private Set<String> stringVariables = new HashSet<>();

    public targetCodeGenerator(String intermediateCodeFile) throws Exception {
        this.inputFilePath = intermediateCodeFile;


        File inputFile = new File(intermediateCodeFile);
        String outputName = inputFile.getName();
        outputName = outputName.substring(0, outputName.lastIndexOf("."));
        outputFilePath = "src/targetCodeGeneration/output/" + outputName + "Basic.txt";
        File output = new File(outputFilePath);
        PrintWriter writer = new PrintWriter(output);
        writer.print("");
        writer.close();
        outputFile = new FileWriter(output, true);
    }

    public void generateBasicCode() throws IOException {
        //first pass: Generate all code with placeholder GOTOs
        generateInitialCode();

        //second pass: Find all label positions
        findLabelPositions();

        //third pass: Update GOTO statements with correct line numbers
        updateGotoStatements();

        writeToFile();
    }

    private void generateInitialCode() throws IOException {
        generatedCode.add(lineNumber + " DIM M(7,30)");
        incrementLineNumber();
        generatedCode.add(lineNumber + " SP = 0");
        incrementLineNumber();

        // Generate code with placeholder GOTOs
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String basicCode = translateLineToBasic(line.trim());
                    if (!basicCode.isEmpty()) {
                        for (String l : basicCode.split("\n")) {
                            if (!l.trim().isEmpty()) {
                                generatedCode.add(l);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateGotoStatements() {
        // Go through and update all GOTO statements with correct line numbers
        for (int i = 0; i < generatedCode.size(); i++) {
            String line = generatedCode.get(i);
            if (line.contains("GOTO NULL_")) {
                //extract label name from NULL_labelname
                String label = line.substring(line.indexOf("NULL_") + 5).trim();
                Integer targetLine = labelLineNumbers.get(label);
                if (targetLine != null) {
                    //replace NULL_labelname with the actual line number
                    String updatedLine = line.replace("NULL_" + label, targetLine.toString());
                    generatedCode.set(i, updatedLine);
                }
            }
            else if (line.contains("THEN GOTO NULL_")) {
                // handle IF statement GOTOs
                String label = line.substring(line.indexOf("NULL_") + 5).trim();
                Integer targetLine = labelLineNumbers.get(label);
                if (targetLine != null) {
                    String updatedLine = line.replace("NULL_" + label, targetLine.toString());
                    generatedCode.set(i, updatedLine);
                }
            }
        }
    }

    private void findLabelPositions() {
        labelLineNumbers.clear();

        for (int i = 0; i < generatedCode.size(); i++) {
            String line = generatedCode.get(i);
            if (line.contains("REM label")) {
                String label = line.substring(line.indexOf("REM ") + 4).trim();
                // Line numbers start at 10 and increment by 10
                int actualLineNumber = 10 + (i * 10);
                labelLineNumbers.put(label, actualLineNumber);
                System.out.println("Found " + label + " at line " + actualLineNumber);
            }
        }
    }

    private void writeToFile() throws IOException {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            for (String line : generatedCode) {
                writer.write(line + "\n");
            }
        }
    }

    private String translateLineToBasic(String line) {
        if (line.isEmpty()) return "";

        String translatedLine = "";

        if (line.startsWith("REM")) {
            translatedLine = lineNumber + " " + line;
        }
        else if (line.startsWith("PRINT")) {
            translatedLine = lineNumber + " " + line;
        }
        else if (line.startsWith("INPUT")) {
            translatedLine = lineNumber + " " + line;
        }
        else if (line.startsWith("STOP")) {
            translatedLine = lineNumber + " END";
        }
        else if (line.startsWith("LABEL")) {
            String label = line.substring(6);
            translatedLine = lineNumber + " REM " + label;
        }
        else if (line.startsWith("GOTO")) {
            String label = line.substring(5).trim();
            // Use placeholder that we'll replace later
            translatedLine = lineNumber + " GOTO NULL_" + label;
        }
        else if (line.startsWith("num F") || line.startsWith("void F")) {
            String[] parts = line.split("[\\(\\)]");
            String funcType = parts[0].trim().split("\\s+")[0]; // "num" or "void"
            String funcName = parts[0].trim().split("\\s+")[1];  // F1, F2, etc.
            translatedLine = lineNumber + " REM " + funcType + " " + funcName;
        }
        else if (line.startsWith("IF")) {
            String[] parts = line.split("\\s+");
            String var1 = parts[1];
            String op = parts[2];
            String var2 = parts[3];
            String thenLabel = parts[5];
            String elseLabel = parts[7];

            if (op.equals("==")) op = "=";

            translatedLine = lineNumber + " IF " + var1 + " " + op + " " + var2 +
                    " THEN GOTO NULL_" + thenLabel + "\n";
            incrementLineNumber();
            translatedLine += lineNumber + " GOTO NULL_" + elseLabel;
        }
        else if (line.startsWith("CALL")) {
            translatedLine = translateFunctionCall(line);
        }
        else if (line.contains("=")) {
            translatedLine = translateAssignment(line);
        }
        else if (line.startsWith("return")) {
            // Get the value being returned (after "return ")
            String returnValue = line.substring(7).trim();
            // Store return value in M(0,SP)
            translatedLine = lineNumber + " LET M(0,SP) = " + returnValue + "\n";
            incrementLineNumber();
            // Return to caller
            translatedLine += lineNumber + " RETURN";
        }

        incrementLineNumber();
        return translatedLine;
    }

    private String translateAssignment(String line) {
        String[] parts = line.split("=", 2);
        String lhs = parts[0].trim();
        String rhs = parts[1].trim();

        // Handle string assignments (check if rhs is a quoted string)
        if (rhs.startsWith("\"") && rhs.endsWith("\"")) {
            // Add the variable to our string tracking set
            stringVariables.add(lhs);
            // Only add $ if it's not already there
            String lhsWithDollar = lhs.endsWith("$") ? lhs : lhs + "$";
            return lineNumber + " LET " + lhsWithDollar + " = " + rhs;
        }

        if (rhs.contains("SQR")) {
            String arg = rhs.substring(4).trim();
            return lineNumber + " LET " + lhs + " = SQR(" + arg + ")";
        }

        // Handle normal assignments
        if (rhs.contains(" + ") || rhs.contains(" - ") ||
                rhs.contains(" * ") || rhs.contains(" / ")) {
            tempVarMap.put(lhs, rhs);
            return lineNumber + " LET " + lhs + " = " + rhs;
        }

        // Check if the rhs variable is a string variable
        if (stringVariables.contains(rhs)) {
            // If assigning from a string variable, the target becomes a string variable too
            stringVariables.add(lhs);
            String lhsWithDollar = lhs.endsWith("$") ? lhs : lhs + "$";
            String rhsWithDollar = rhs.endsWith("$") ? rhs : rhs + "$";
            return lineNumber + " LET " + lhsWithDollar + " = " + rhsWithDollar;
        }

        return lineNumber + " LET " + lhs + " = " + rhs;
    }

    private String getVariableName(String var) {
        return stringVariables.contains(var) ? var + "$" : var;
    }

    private String translateFunctionCall(String line) {
        if (line.contains("=")) {
            // Assignment with function call: var1 = CALL F1(A,1,1)
            // This must be a num function since we're assigning its result
            String[] parts = line.split("=", 2);
            String resultVar = parts[0].trim();
            String call = parts[1].trim();

            // Parse function call
            String[] callParts = call.substring(5).split("[\\(\\)]"); // Remove "CALL "
            String funcName = callParts[0].trim();  // F1, F2, etc.
            String[] params = callParts[1].split(",");

            StringBuilder basic = new StringBuilder();

            // Increment stack pointer for new frame
            basic.append(lineNumber).append(" SP = SP + 1\n");
            incrementLineNumber();

            // Store parameters in positions 1-3 of current frame
            for (int i = 0; i < params.length; i++) {
                basic.append(lineNumber).append(" LET M(").append(i+1).append(",SP) = ")
                        .append(params[i].trim()).append("\n");
                incrementLineNumber();
            }

            // Call the function
            basic.append(lineNumber).append(" GOSUB ").append(funcName).append("\n");
            incrementLineNumber();

            // Get return value from M(0,SP)
            basic.append(lineNumber).append(" LET ").append(resultVar)
                    .append(" = M(0,SP)\n");
            incrementLineNumber();

            // Decrement stack pointer to remove frame
            basic.append(lineNumber).append(" SP = SP - 1");
            incrementLineNumber();

            return basic.toString();
        } else {
            // Direct function call without assignment: CALL F1(1,1,1)
            // This could be either void or num function
            String[] callParts = line.substring(5).split("[\\(\\)]");
            String funcName = callParts[0].trim();  // F1, F2, etc.
            String[] params = callParts[1].split(",");

            StringBuilder basic = new StringBuilder();

            // Increment stack pointer for new frame
            basic.append(lineNumber).append(" SP = SP + 1\n");
            incrementLineNumber();

            // Store parameters in positions 1-3 of current frame
            for (int i = 0; i < params.length; i++) {
                basic.append(lineNumber).append(" LET M(").append(i+1).append(",SP) = ")
                        .append(params[i].trim()).append("\n");
                incrementLineNumber();
            }

            // Call the function
            basic.append(lineNumber).append(" GOSUB ").append(funcName).append("\n");
            incrementLineNumber();

            // Decrement stack pointer to remove frame
            basic.append(lineNumber).append(" SP = SP - 1");
            incrementLineNumber();

            return basic.toString();
        }
    }

    private void incrementLineNumber() {
        lineNumber += 10;
    }

    public static void main(String[] args) {
        try {
            targetCodeGenerator generator = new targetCodeGenerator("src/intermediateCodeGeneration/output/output2.txt");
            generator.generateBasicCode();
            generator.showGeneratedCode();

        } catch (Exception e) {
            System.err.println("Error during code generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showGeneratedCode() {
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
