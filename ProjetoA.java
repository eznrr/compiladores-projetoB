import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * Programa (a)
 * Analisador lexico, analisador sintatico descendente recursivo e geracao
 * direta de codigo C para a linguagem LPS1.
 */
public class ProjetoA {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: java ProjetoA arquivo.lps1");
            return;
        }

        Path inputPath = Paths.get(args[0]);
        String fonte = new String(Files.readAllBytes(inputPath), StandardCharsets.UTF_8);
        Parser parser = new Parser(new Lexer(fonte));
        String codigoC = parser.parseProgram();
        Path outputPath = outputPath(inputPath, "a");
        Files.write(outputPath, codigoC.getBytes(StandardCharsets.UTF_8));
        System.out.print(codigoC);
        System.err.println("Codigo C gerado em: " + outputPath);
    }

    private static Path outputPath(Path inputPath, String suffix) {
        String fileName = inputPath.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot >= 0 ? fileName.substring(0, dot) : fileName;
        Path parent = inputPath.getParent();
        String outputName = "saida-" + baseName + "-" + suffix + ".c";
        return parent == null ? Paths.get(outputName) : parent.resolve(outputName);
    }

    enum TokenType {
        ASSIGN, GET, ADD, SUB, MULT, DIV, MOD, PRINT, IF, WHILE,
        LBRACE, RBRACE, LT, NEQ, VARIABLE, NUMBER, EOF
    }

    static class Token {
        final TokenType type;
        final String lexeme;
        final int line;
        final int column;

        Token(TokenType type, String lexeme, int line, int column) {
            this.type = type;
            this.lexeme = lexeme;
            this.line = line;
            this.column = column;
        }
    }

    static class Lexer {
        private final String source;
        private int pos;
        private int line = 1;
        private int column = 1;

        Lexer(String source) {
            this.source = source;
        }

        Token nextToken() {
            skipWhitespace();
            int startLine = line;
            int startColumn = column;

            if (isAtEnd()) {
                return new Token(TokenType.EOF, "", startLine, startColumn);
            }

            char c = advance();
            switch (c) {
                case '=': return new Token(TokenType.ASSIGN, "=", startLine, startColumn);
                case 'G': return new Token(TokenType.GET, "G", startLine, startColumn);
                case '+': return new Token(TokenType.ADD, "+", startLine, startColumn);
                case '-': return new Token(TokenType.SUB, "-", startLine, startColumn);
                case '*': return new Token(TokenType.MULT, "*", startLine, startColumn);
                case '/': return new Token(TokenType.DIV, "/", startLine, startColumn);
                case '%': return new Token(TokenType.MOD, "%", startLine, startColumn);
                case 'P': return new Token(TokenType.PRINT, "P", startLine, startColumn);
                case 'I': return new Token(TokenType.IF, "I", startLine, startColumn);
                case 'W': return new Token(TokenType.WHILE, "W", startLine, startColumn);
                case '{': return new Token(TokenType.LBRACE, "{", startLine, startColumn);
                case '}': return new Token(TokenType.RBRACE, "}", startLine, startColumn);
                case '<': return new Token(TokenType.LT, "<", startLine, startColumn);
                case '#': return new Token(TokenType.NEQ, "#", startLine, startColumn);
                default:
                    if (c >= 'a' && c <= 'z') {
                        return new Token(TokenType.VARIABLE, String.valueOf(c), startLine, startColumn);
                    }
                    if (c >= '0' && c <= '9') {
                        return new Token(TokenType.NUMBER, String.valueOf(c), startLine, startColumn);
                    }
                    throw error("simbolo invalido '" + c + "'", startLine, startColumn);
            }
        }

        private void skipWhitespace() {
            while (!isAtEnd()) {
                char c = source.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\r') {
                    advance();
                } else if (c == '\n') {
                    advance();
                } else {
                    break;
                }
            }
        }

        private char advance() {
            char c = source.charAt(pos++);
            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            return c;
        }

        private boolean isAtEnd() {
            return pos >= source.length();
        }
    }

    static class Parser {
        private final Lexer lexer;
        private Token current;
        private final StringBuilder code = new StringBuilder();
        private int indent = 0;

        Parser(Lexer lexer) {
            this.lexer = lexer;
            this.current = lexer.nextToken();
        }

        String parseProgram() {
            emit("#include <stdio.h>");
            emit("int main() {");
            indent++;
            emit("int a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;");
            emit("char str[512];");

            while (current.type != TokenType.EOF) {
                parseCommand();
            }

            emit("return 0;");
            indent--;
            emit("}");
            return code.toString();
        }

        private void parseCommand() {
            switch (current.type) {
                case ASSIGN: parseAssign(); break;
                case GET: parseGet(); break;
                case ADD: parseBinary("+"); break;
                case SUB: parseBinary("-"); break;
                case MULT: parseBinary("*"); break;
                case DIV: parseBinary("/"); break;
                case MOD: parseBinary("%"); break;
                case PRINT: parsePrint(); break;
                case IF: parseIf(); break;
                case WHILE: parseWhile(); break;
                case LBRACE: parseComposite(); break;
                default:
                    throw syntaxError("comando esperado");
            }
        }

        private void parseAssign() {
            consume(TokenType.ASSIGN, "'=' esperado");
            String variable = consume(TokenType.VARIABLE, "variavel esperada apos '='").lexeme;
            String value = parseValue();
            emit(variable + " = " + value + ";");
        }

        private void parseGet() {
            consume(TokenType.GET, "'G' esperado");
            String variable = consume(TokenType.VARIABLE, "variavel esperada apos 'G'").lexeme;
            emit("{");
            indent++;
            emit("gets(str);");
            emit("sscanf(str, \"%d\", &" + variable + ");");
            indent--;
            emit("}");
        }

        private void parseBinary(String operator) {
            consume(current.type, "operador esperado");
            String target = consume(TokenType.VARIABLE, "variavel de destino esperada").lexeme;
            String left = parseValue();
            String right = parseValue();
            emit(target + " = " + left + " " + operator + " " + right + ";");
        }

        private void parsePrint() {
            consume(TokenType.PRINT, "'P' esperado");
            String value = parseValue();
            emit("printf(\"%d\\n\", " + value + ");");
        }

        private void parseIf() {
            consume(TokenType.IF, "'I' esperado");
            String comparison = parseComparison();
            emit("if ( " + comparison + " ) {");
            indent++;
            parseCommand();
            indent--;
            emit("}");
        }

        private void parseWhile() {
            consume(TokenType.WHILE, "'W' esperado");
            String comparison = parseComparison();
            emit("while ( " + comparison + " ) {");
            indent++;
            parseCommand();
            indent--;
            emit("}");
        }

        private void parseComposite() {
            consume(TokenType.LBRACE, "'{' esperado");
            while (current.type != TokenType.RBRACE && current.type != TokenType.EOF) {
                parseCommand();
            }
            consume(TokenType.RBRACE, "'}' esperado");
        }

        private String parseComparison() {
            String variable = consume(TokenType.VARIABLE, "variavel esperada na comparacao").lexeme;
            String operator;
            if (current.type == TokenType.ASSIGN) {
                consume(TokenType.ASSIGN, "'=' esperado");
                operator = "==";
            } else if (current.type == TokenType.LT) {
                consume(TokenType.LT, "'<' esperado");
                operator = "<";
            } else if (current.type == TokenType.NEQ) {
                consume(TokenType.NEQ, "'#' esperado");
                operator = "!=";
            } else {
                throw syntaxError("operador de comparacao esperado");
            }
            String value = parseValue();
            return variable + " " + operator + " " + value;
        }

        private String parseValue() {
            if (current.type == TokenType.VARIABLE || current.type == TokenType.NUMBER) {
                String value = current.lexeme;
                advance();
                return value;
            }
            throw syntaxError("valor esperado");
        }

        private Token consume(TokenType type, String message) {
            if (current.type != type) {
                throw syntaxError(message);
            }
            Token token = current;
            advance();
            return token;
        }

        private void advance() {
            current = lexer.nextToken();
        }

        private void emit(String text) {
            for (int i = 0; i < indent; i++) {
                code.append("    ");
            }
            code.append(text).append('\n');
        }

        private RuntimeException syntaxError(String message) {
            return error(message + " perto de '" + current.lexeme + "'", current.line, current.column);
        }
    }

    static RuntimeException error(String message, int line, int column) {
        return new RuntimeException("Erro na linha " + line + ", coluna " + column + ": " + message);
    }
}
