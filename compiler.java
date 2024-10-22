package compiler;

public class compiler {
    //This is the finale class that will be used to compile the code
    public static void main(String[] args) {
        //using command line arguments to get the input file name
        String inputFileName = args[0];
        try {
            //lexing

            //parsing

            //semantic analysis

            //type checking

            //code generation
            
        } catch (Exception e) {
            //Print the error message
            e.printStackTrace();
        }
    }
}
