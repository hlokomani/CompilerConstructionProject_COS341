import java.nio.file.Paths;
import java.util.logging.*;
import java.util.*;

public class Lexer {
    private static final Logger logger = Logger.getLogger(Lexer.class.getName());

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Lexer <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile = Paths.get(args[0]).toString();
        String outputFile = Paths.get(args[1]).toString();

        try {
            String input = FileHandler.readFile(inputFile);
            List<Token> tokens = LexicalAnalyzer.analyze(input);
            XMLFormatter.writeXMLOutput(tokens, outputFile);
            System.out.println("Lexical analysis completed successfully. Output written to " + outputFile);
        } catch (LexicalException e) {
            System.err.println("Lexical Error: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred", e);
        }
    }
}