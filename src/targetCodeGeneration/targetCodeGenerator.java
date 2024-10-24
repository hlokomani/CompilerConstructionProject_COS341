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
    private String inputFilePath, outputDirectory;
    private Map<String, String> tempVarMap = new HashMap<>();
    private Map<String, Integer> labelLineNumbers = new HashMap<>();
    private List<String> generatedCode = new ArrayList<>();
    private Set<String> stringVariables = new HashSet<>();

    public targetCodeGenerator(String intermediateCodeFile, String outputDirectory) throws Exception {
        this.inputFilePath = intermediateCodeFile;
        this.outputDirectory = outputDirectory;

        // Create the output file
        File inputFile = new File(intermediateCodeFile);
        String outputName = inputFile.getName();
        outputName = outputName.substring(0, outputName.lastIndexOf(".")) + "Basic.txt";

        // Ensure output directory exists
        new File(outputDirectory).mkdirs();

        String outputFilePath = outputDirectory + File.separator + outputName;
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
        // Go through and update all GOTO and GOSUB statements with correct line numbers
        for (int i = 0; i < generatedCode.size(); i++) {
            String line = generatedCode.get(i);
            if (line.contains("GOTO NULL_")) {
                String label = line.substring(line.indexOf("NULL_") + 5).trim();
                Integer targetLine = labelLineNumbers.get(label);
                if (targetLine != null) {
                    String updatedLine = line.replace("NULL_" + label, targetLine.toString());
                    generatedCode.set(i, updatedLine);
                }
            }
            else if (line.contains("GOSUB NULL_")) {
                String funcName = line.substring(line.indexOf("NULL_") + 5).trim();
                Integer targetLine = labelLineNumbers.get(funcName);
                if (targetLine != null) {
                    String updatedLine = line.replace("NULL_" + funcName, targetLine.toString());
                    generatedCode.set(i, updatedLine);
                }
            }
            else if (line.contains("THEN GOTO NULL_")) {
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
                int actualLineNumber = 10 + (i * 10);
                labelLineNumbers.put(label, actualLineNumber);
//                System.out.println("Found " + label + " at line " + actualLineNumber);
            }
            else if (line.contains("REM num ") || line.contains("REM void ")) {
                // Extract function name (F1, F2, etc.)
                String[] parts = line.split("\\s+");
                String funcName = parts[parts.length - 1];
                int actualLineNumber = 10 + (i * 10);
                labelLineNumbers.put(funcName, actualLineNumber);
//                System.out.println("Found function " + funcName + " at line " + actualLineNumber);
            }
        }
    }

    private void writeToFile() throws IOException {
        String outputPath = outputDirectory + File.separator + "outputBasic.txt";
        try (FileWriter writer = new FileWriter(outputPath)) {
            int expectedLineNumber = 10;
            for (String line : generatedCode) {
                int currentLineNumber = Integer.parseInt(line.split("\\s+")[0]);
                if (currentLineNumber != expectedLineNumber) {
                    throw new RuntimeException("Line number mismatch: expected " + expectedLineNumber +
                            " but got " + currentLineNumber + " for line: " + line);
                }
                writer.write(line + "\n");
                expectedLineNumber += 10;
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

            // Get parameter names
            String[] params = parts[1].split(",");

            // Function declaration
            translatedLine = lineNumber + " REM " + funcType + " " + funcName + "\n";
            incrementLineNumber();

            // Add REM BEGIN
            translatedLine += lineNumber + " REM BEGIN\n";
            incrementLineNumber();

            // Initialize parameters from stack frame
            for (int i = 0; i < params.length; i++) {
                String param = params[i].trim();
                translatedLine += lineNumber + " LET " + param + " = M(" + (i+1) + ",SP)\n";
                incrementLineNumber();
            }

            return translatedLine;
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
            return translatedLine;
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

        // If the right hand side contains a CALL, handle it specially
        if (rhs.trim().startsWith("CALL")) {
            // Parse function call
            String[] callParts = rhs.substring(5).split("[\\(\\)]"); // Remove "CALL "
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

            // Call the function using NULL_ placeholder like GOTO
            basic.append(lineNumber).append(" GOSUB NULL_").append(funcName).append("\n");
            incrementLineNumber();

            // Get return value from M(0,SP) into the target variable
            basic.append(lineNumber).append(" LET ").append(lhs)
                    .append(" = M(0,SP)\n");
            incrementLineNumber();

            // Decrement stack pointer to remove frame
            basic.append(lineNumber).append(" SP = SP - 1");

            return basic.toString();
        }

        // Handle string assignments (check if rhs is a quoted string)
        if (rhs.startsWith("\"") && rhs.endsWith("\"")) {
            stringVariables.add(lhs);
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
            stringVariables.add(lhs);
            String lhsWithDollar = lhs.endsWith("$") ? lhs : lhs + "$";
            String rhsWithDollar = rhs.endsWith("$") ? rhs : rhs + "$";
            return lineNumber + " LET " + lhsWithDollar + " = " + rhsWithDollar;
        }

        return lineNumber + " LET " + lhs + " = " + rhs;
    }


    private String translateFunctionCall(String line) {
        if (line.contains("=")) {
            // This case is now handled in translateAssignment
            return "";
        } else {
            // Direct function call without assignment: CALL F1(1,1,1)
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

            // Call the function using NULL_ placeholder like GOTO
            basic.append(lineNumber).append(" GOSUB NULL_").append(funcName).append("\n");
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

    private String getOutputFilePath() {
        return outputDirectory + File.separator + "outputBasic.txt";
    }

    public static void main(String[] args) {
        try {
            String currentDir = new File(".").getCanonicalPath();
            String outputDir = currentDir + File.separator + "compilerOutputs" + File.separator + "target";

            new File(outputDir).mkdirs();

            targetCodeGenerator generator = new targetCodeGenerator(
                    currentDir + File.separator + "compilerOutputs" + File.separator + "intermediate" + File.separator + "output.txt",
                    outputDir
            );

            generator.generateBasicCode();
            System.out.println("Generated BASIC code:");
            System.out.println("---------------------");
            generator.showGeneratedCode();
            System.out.println("---------------------");
            System.out.println("Output file location: " + generator.getOutputFilePath());

        } catch (Exception e) {
            System.err.println("Error during code generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showGeneratedCode() {
        try (BufferedReader reader = new BufferedReader(new FileReader(getOutputFilePath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading generated code: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
