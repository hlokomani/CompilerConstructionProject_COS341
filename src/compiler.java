import lexer.FileHandler;
import lexer.LexicalAnalyzer;
import lexer.Token;
import lexer.XMLFormatter;
import parser2.SLRParser;
import semanticanalyzer.SemanticAnalyzer;
import typeChecker.typeCheck;
import typeChecker.typeChecker;
import targetCodeGeneration.targetCodeGenerator;
import intermediateCodeGeneration.intermediateCodeGeneration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class compiler {
    private static String baseOutputDir;
    private static String compilerOutputDir;
    private static String lexerOutputDir;
    private static String parserOutputDir;
    private static String intermediateOutputDir;
    private static String targetOutputDir;

    private static void setupDirectories(String inputFile) throws Exception {
        // Get the parent directory of the input file
        File inputFileObj = new File(inputFile).getAbsoluteFile();
        File inputParentDir = inputFileObj.getParentFile();
        if (inputParentDir == null) {
            inputParentDir = new File(".");
        }

        // Create compilerOutputs directory as a sibling to the input file
        compilerOutputDir = inputParentDir.getPath() + File.separator + "compilerOutputs";

        // Create output directories under compilerOutputs
        lexerOutputDir = compilerOutputDir + File.separator + "lexer";
        parserOutputDir = compilerOutputDir + File.separator + "parser";
        intermediateOutputDir = compilerOutputDir + File.separator + "intermediate";
        targetOutputDir = compilerOutputDir + File.separator + "target";

        // Create directories if they don't exist
        createDirectoryIfNotExists(compilerOutputDir);
        createDirectoryIfNotExists(lexerOutputDir);
        createDirectoryIfNotExists(parserOutputDir);
        createDirectoryIfNotExists(intermediateOutputDir);
        createDirectoryIfNotExists(targetOutputDir);

        // Set baseOutputDir to the parent directory
        baseOutputDir = inputParentDir.getPath();
    }

    private static void createDirectoryIfNotExists(String dir) throws Exception {
        Path path = Paths.get(dir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    private static String getOutputPath(String directory, String filename) {
        return directory + File.separator + filename;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java compiler <input-file>");
            System.exit(1);
        }

        String inputFile = args[0];
        try {
            // Setup output directories
            setupDirectories(inputFile);

            // Phase 1: Lexical Analysis
            System.out.println("Starting lexical analysis...");
            String input = FileHandler.readFile(inputFile);
            List<Token> tokens = LexicalAnalyzer.analyze(input);
            String lexerOutput = getOutputPath(lexerOutputDir, "output.xml");
            XMLFormatter.writeXMLOutput(tokens, lexerOutput);
            System.out.println("Lexical analysis completed successfully.");

            // Phase 2: Parsing
            System.out.println("Starting parsing...");
            SLRParser parser = new SLRParser();
            parser.setOutputDirectory(parserOutputDir);
            parser.parse(lexerOutput);
            String parserOutput = getOutputPath(parserOutputDir, "output.xml");
            System.out.println("Parsing completed successfully.");

            // Phase 3: Semantic Analysis
            System.out.println("Starting semantic analysis...");
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(parserOutput);
            semanticAnalyzer.analyze();
            System.out.println("Semantic analysis completed successfully.");

            // Phase 4: Type Checking
            System.out.println("Starting type checking...");
            typeChecker typeChecker = new typeChecker(parserOutput);
            typeCheck programType = typeChecker.typeCheck();
            if (!programType.type) {
                throw new Exception(programType.message);
            }
            System.out.println("Type checking completed successfully.");

            // Phase 5a: Intermediate Code Generation
            System.out.println("Starting intermediate code generation...");
            intermediateCodeGeneration intermediateCodeGenerator = new intermediateCodeGeneration(
                    parserOutput,
                    intermediateOutputDir
            );
            intermediateCodeGenerator.trans();
            String intermediateOutput = getOutputPath(intermediateOutputDir, "output.txt");
            System.out.println("Intermediate code generation completed successfully.");

            // Phase 5b: Code Generation
            System.out.println("Starting code generation...");
            targetCodeGenerator codeGenerator = new targetCodeGenerator(intermediateOutput, targetOutputDir);
            codeGenerator.generateBasicCode();
            System.out.println("Code generation completed successfully.");

            System.out.println("\nCompilation completed successfully!");
            System.out.println("Output files are located in: " + compilerOutputDir);

        } catch (Exception e) {
            System.err.println("\nCompilation failed:");
            System.err.println(e.getMessage());
            if (e.getMessage() == null || e.getMessage().isEmpty()) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}