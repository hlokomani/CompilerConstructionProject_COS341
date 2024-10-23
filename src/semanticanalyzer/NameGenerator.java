package semanticanalyzer;

public class NameGenerator {
    private static NameGenerator instance;
    private char currentTextLetter = 'A';
    private char currentNumLetter = 'A';
    private int currentNumSuffix = 0;
    private int currentFuncNumber = 1;

    private NameGenerator() {}

    public static NameGenerator getInstance() {
        if (instance == null) {
            instance = new NameGenerator();
        }
        return instance;
    }

    public String getNextTextName() {
        String name = currentTextLetter + "$";

        currentTextLetter++;
        if (currentTextLetter > 'Z') {
            currentTextLetter = 'A';
        }

        return name;
    }

    public String getNextFunctionName() {
        String name = "F" + currentFuncNumber;
        currentFuncNumber++;
        return name;
    }

    public String getNextNumName() {
        String name;
        if (currentNumSuffix == 0) {
            name = String.valueOf(currentNumLetter);
        } else {
            name = currentNumLetter + String.valueOf(currentNumSuffix);
        }

        currentNumSuffix++;
        if (currentNumSuffix > 9) {
            currentNumSuffix = 0;
            currentNumLetter++;
            if (currentNumLetter > 'Z') {
                currentNumLetter = 'A';
            }
        }

        return name;
    }

    public void reset() {
        currentTextLetter = 'A';
        currentNumLetter = 'A';
        currentNumSuffix = 0;
        currentFuncNumber = 1;
    }
}