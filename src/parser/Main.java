package parser;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Ensure you have listed the XML File as an argument: java Main <input_xml_file>");
            return;
        }

        String inputFile = args[0];

        try {
            Parser parser = new Parser(inputFile);
            parser.parse();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
