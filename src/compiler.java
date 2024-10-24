import lexer.FileHandler;
import lexer.LexicalAnalyzer;
import lexer.Token;
import lexer.XMLFormatter;
import parser2.SLRParser;
import semanticanalyzer.SemanticAnalyzer;
import typeChecker.typeChecker;
import targetCodeGeneration.targetCodeGenerator;
import intermediateCodeGeneration.intermediateCodeGeneration;

import java.util.List;

public class compiler {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java compiler <input-file>");
            System.exit(1);
        }

        String inputFile = args[0];
        try {
            // Phase 1: Lexical Analysis
            System.out.println("Starting lexical analysis...");
            String input = FileHandler.readFile(inputFile);
            List<Token> tokens = LexicalAnalyzer.analyze(input);
            String lexerOutput = "src/lexer/output/output.xml";
            XMLFormatter.writeXMLOutput(tokens, lexerOutput);
            System.out.println("Lexical analysis completed successfully.");

            // Phase 2: Parsing
            System.out.println("Starting parsing...");
            SLRParser parser = new SLRParser();
            parser.parse(lexerOutput);
            String parserOutput = "src/parser2/output/output.xml";
            System.out.println("Parsing completed successfully.");

            // Phase 3: Semantic Analysis
            System.out.println("Starting semantic analysis...");
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(parserOutput);
            semanticAnalyzer.analyze();
            System.out.println("Semantic analysis completed successfully.");

            // Phase 4: Type Checking
            System.out.println("Starting type checking...");
            typeChecker typeChecker = new typeChecker(parserOutput);
            if (!typeChecker.typeCheck()) {
                throw new Exception("Type checking failed");
            }
            System.out.println("Type checking completed successfully.");

            // Phase 5a: Intermediate Code Generation
            System.out.println("Starting intermediate code generation...");
            intermediateCodeGeneration intermediateCodeGenerator = new intermediateCodeGeneration(parserOutput);
            intermediateCodeGenerator.trans();
            System.out.println("Intermediate code generation completed successfully.");

            // Phase 5b: Code Generation
            System.out.println("Starting code generation...");
            targetCodeGenerator codeGenerator = new targetCodeGenerator("src/intermediateCodeGeneration/output/output.txt");
            codeGenerator.generateBasicCode();
            System.out.println("Code generation completed successfully.");

            System.out.println("\nCompilation completed successfully!");

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