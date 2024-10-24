# Instructions

In this folder you will find both the source code as well as the .jar file for the compiler. The source code can be found in the "src" folder and the .jar can be found in the "dist" folder.

## How to use the compiler

Ensure that you have a suitable JVM installed on your machine. To run the .jar, open a terminal in the directory containing the .jar file (it should be something like ".../compiler/dist"). Then run the following command:

```bash
java -jar 341SemesterProject.jar arg1
```

Where arg1 is the directory (relative to the current directory) of the input text file.

## Compiled successfully

Once the compiler has successfully run, you will see a new output folder in the directory that the .jar was run. This output folder will contain a .txt that has the target code (Basic) for the given input. To inspect the output of any of the components of the compiler, simply navigate to the respective folder in the output folder.

### Running the output program

Download and install [this](https://robhagemans.github.io/pcbasic/). After installing and opening the "Andy Parkes BBCEdit" IDE, copy and paste the contents of the of the output file into the editor and click on the run button.
If you are unable to obtain this software [QB 64](https://qb64.com/) and run the qb64.exe.

## Compiled unsuccessfully

If the input program does not match the requirements the parser will output (to the terminal) and error message along with where in the input program the error occurred. Note that the location may not be exactly where the error occurred, but should be close enough to allow for debugging.

## Input program requirements

This compiler is very strict on syntax, therefore any input program must perfectly match the grammar of the language. The type checking is similarly strict, and must match the description provided for type checking.
