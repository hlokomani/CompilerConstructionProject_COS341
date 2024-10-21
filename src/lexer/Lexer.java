package lexer;
import java.util.*;

public class Lexer {

    public static void main(String[] args) {
        String inputFile = "input/input_program4.txt";
        String outputFile = "src/lexer/output/output4.xml";

        try {
            String input = FileHandler.readFile(inputFile);
            List<Token> tokens = LexicalAnalyzer.analyze(input);
            XMLFormatter.writeXMLOutput(tokens, outputFile);
            System.out.println("Lexical analysis completed successfully. Output written to " + outputFile);
        } catch (LexicalException e) {
            System.err.println("Lexical Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}