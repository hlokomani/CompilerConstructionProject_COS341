package lexer;

import java.util.*;
import java.util.regex.*;

public class LexicalAnalyzer {
    private static final String[] KEYWORDS = {"main", "begin", "end", "if", "then", "else", "num", "text", "void", "print", "input", "skip", "halt", "return"};
    private static final Set<String> KEYWORD_SET = new HashSet<>(Arrays.asList(KEYWORDS));
    private static final String[] OPERATORS = {"<", "=", "(", ")", ",", ";", "{", "}", "or", "and", "eq", "grt", "add", "sub", "mul", "div", "not", "sqrt"};
    private static final Set<String> OPERATOR_SET = new HashSet<>(Arrays.asList(OPERATORS));

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("V_[a-z]([a-z]|[0-9])*");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("F_[a-z]([a-z]|[0-9])*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("0|0\\.([0-9])*[1-9]|-0\\.([0-9])*[1-9]|[1-9]([0-9])*|-[1-9]([0-9])*|[1-9]([0-9])*\\.([0-9])*[1-9]|-[1-9]([0-9])*\\.([0-9])*[1-9]");
    private static final Pattern TEXT_PATTERN = Pattern.compile("\"[A-Z][a-z]{0,7}\"");

    private static int lineNumber = 1;
    private static int columnNumber = 1;

    public static List<Token> analyze(String input) throws LexicalException {
        List<Token> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inString) {
                currentToken.append(c);
                if (c == '"') {
                    tokens.add(createToken(currentToken.toString()));
                    currentToken = new StringBuilder();
                    inString = false;
                }
            } else if (c == '"') {
                if (currentToken.length() > 0) {
                    tokens.add(createToken(currentToken.toString()));
                    currentToken = new StringBuilder();
                }
                currentToken.append(c);
                inString = true;
            } else if (Character.isWhitespace(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(createToken(currentToken.toString()));
                    currentToken = new StringBuilder();
                }
                if (c == '\n') {
                    lineNumber++;
                    columnNumber = 1;
                } else {
                    columnNumber++;
                }
            } else if (isPunctuation(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(createToken(currentToken.toString()));
                    currentToken = new StringBuilder();
                }
                tokens.add(createToken(String.valueOf(c)));
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(createToken(currentToken.toString()));
        }

        tokens.add(new Token("END", "$", lineNumber, columnNumber));

        return tokens;
    }

    private static boolean isPunctuation(char c) {
        return "(),;{}".indexOf(c) != -1;
    }

    private static Token createToken(String word) throws LexicalException {
        String tokenClass = getTokenClass(word);
        if (tokenClass == null) {
            throw new LexicalException("Unknown token: " + word + " at line " + lineNumber + ", column " + columnNumber);
        }
        Token token = new Token(tokenClass, word, lineNumber, columnNumber);
        columnNumber += word.length();
        return token;
    }

    private static String getTokenClass(String token) {
        if (KEYWORD_SET.contains(token)) {
            return "reserved_keyword";
        } else if (OPERATOR_SET.contains(token)) {
            return "operator";
        } else if (VARIABLE_PATTERN.matcher(token).matches()) {
            return "V";
        } else if (FUNCTION_PATTERN.matcher(token).matches()) {
            return "F";
        } else if (NUMBER_PATTERN.matcher(token).matches()) {
            return "N";
        } else if (TEXT_PATTERN.matcher(token).matches()) {
            return "T";
        } else if (token.length() == 1 && isPunctuation(token.charAt(0))) {
            return "punctuation";
        }
        return null;
    }
}