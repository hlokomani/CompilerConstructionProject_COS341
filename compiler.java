package compiler;

public class compiler {
    //This is the finale class that will be used to compile the code
    public static void main(String[] args) {
        //using command line arguments to get the input file name
        String inputFile = args[0];
        try {
            //lexing
            String input = FileHandler.readFile(inputFile);
            List<Token> tokens = LexicalAnalyzer.analyze(input);
            String outputFile = "src/lexer/output/output.xml";
            XMLFormatter.writeXMLOutput(tokens, outputFile);
            System.out.println("Lexical analysis completed successfully. Output written to " + outputFile);

            //parsing
            SLRParser parser = new SLRParser();
            parser.parse(outputFile);
            outputFile = "src/parser2/output/output.xml";
            System.out.println("Parsing completed successfully. Output written to " + outputFile);

            //semantic analysis
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(outputFile);
            semanticAnalyzer.analyze();
            System.out.println("Semantic and scope completed successfully.");

            //type checking
            typeChecker typeChecker = new typeChecker(outputFile);
            if (typeChecker.typeCheck())
            {
                System.out.println("Type checking completed successfully.");
                //code generation
                codeGenerator codeGenerator = new codeGenerator(outputFile);
                codeGenerator.translate();
                System.out.println("Code generation completed successfully. Output written to output/output.txt");

            } else {
                System.out.println("Type checking failed.");
            }
            
        } catch (Exception e) {
            //Print the error message
            e.printStackTrace();
        }
    }
}
