import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*
 * Programa (b)
 * Analisador lexico, analisador sintatico descendente recursivo e construcao
 * da ASA. A geracao de codigo C fica nos metodos dos nos da ASA.
 */
public class ProjetoB {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: java ProjetoB arquivo.lps1");
            return;
        }

        String fonte = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
        Parser parser = new Parser(new Lexer(fonte));
        ProgramNode program = parser.parseProgram();
        System.out.print(program.toC());
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
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
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

    interface Node {
        void generate(CodeWriter out);
    }

    interface CommandNode extends Node {
    }

    static class ProgramNode implements Node {
        final List<CommandNode> commands;

        ProgramNode(List<CommandNode> commands) {
            this.commands = commands;
        }

        String toC() {
            CodeWriter out = new CodeWriter();
            generate(out);
            return out.toString();
        }

        public void generate(CodeWriter out) {
            out.line("#include <stdio.h>");
            out.line("int main() {");
            out.indent();
            out.line("int a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;");
            out.line("char str[512];");
            for (CommandNode command : commands) {
                command.generate(out);
            }
            out.line("return 0;");
            out.dedent();
            out.line("}");
        }
    }

    static class AssignNode implements CommandNode {
        final String variable;
        final ValueNode value;

        AssignNode(String variable, ValueNode value) {
            this.variable = variable;
            this.value = value;
        }

        public void generate(CodeWriter out) {
            out.line(variable + " = " + value.toC() + ";");
        }
    }

    static class GetNode implements CommandNode {
        final String variable;

        GetNode(String variable) {
            this.variable = variable;
        }

        public void generate(CodeWriter out) {
            out.line("{");
            out.indent();
            out.line("gets(str);");
            out.line("sscanf(str, \"%d\", &" + variable + ");");
            out.dedent();
            out.line("}");
        }
    }

    static class BinaryNode implements CommandNode {
        final String operator;
        final String target;
        final ValueNode left;
        final ValueNode right;

        BinaryNode(String operator, String target, ValueNode left, ValueNode right) {
            this.operator = operator;
            this.target = target;
            this.left = left;
            this.right = right;
        }

        public void generate(CodeWriter out) {
            out.line(target + " = " + left.toC() + " " + operator + " " + right.toC() + ";");
        }
    }

    static class PrintNode implements CommandNode {
        final ValueNode value;

        PrintNode(ValueNode value) {
            this.value = value;
        }

        public void generate(CodeWriter out) {
            out.line("printf(\"%d\\n\", " + value.toC() + ");");
        }
    }

    static class IfNode implements CommandNode {
        final ComparisonNode comparison;
        final CommandNode command;

        IfNode(ComparisonNode comparison, CommandNode command) {
            this.comparison = comparison;
            this.command = command;
        }

        public void generate(CodeWriter out) {
            out.line("if ( " + comparison.toC() + " ) {");
            out.indent();
            command.generate(out);
            out.dedent();
            out.line("}");
        }
    }

    static class WhileNode implements CommandNode {
        final ComparisonNode comparison;
        final CommandNode command;

        WhileNode(ComparisonNode comparison, CommandNode command) {
            this.comparison = comparison;
            this.command = command;
        }

        public void generate(CodeWriter out) {
            out.line("while ( " + comparison.toC() + " ) {");
            out.indent();
            command.generate(out);
            out.dedent();
            out.line("}");
        }
    }

    static class CompositeNode implements CommandNode {
        final List<CommandNode> commands;

        CompositeNode(List<CommandNode> commands) {
            this.commands = commands;
        }

        public void generate(CodeWriter out) {
            for (CommandNode command : commands) {
                command.generate(out);
            }
        }
    }

    static class ComparisonNode {
        final String variable;
        final String operator;
        final ValueNode value;

        ComparisonNode(String variable, String operator, ValueNode value) {
            this.variable = variable;
            this.operator = operator;
            this.value = value;
        }

        String toC() {
            return variable + " " + operator + " " + value.toC();
        }
    }

    static class ValueNode {
        final String text;

        ValueNode(String text) {
            this.text = text;
        }

        String toC() {
            return text;
        }
    }

    static class CodeWriter {
        private final StringBuilder text = new StringBuilder();
        private int indentation;

        void indent() {
            indentation++;
        }

        void dedent() {
            indentation--;
        }

        void line(String line) {
            for (int i = 0; i < indentation; i++) {
                text.append("    ");
            }
            text.append(line).append('\n');
        }

        public String toString() {
            return text.toString();
        }
    }

    static class Parser {
        private final Lexer lexer;
        private Token current;

        Parser(Lexer lexer) {
            this.lexer = lexer;
            this.current = lexer.nextToken();
        }

        ProgramNode parseProgram() {
            List<CommandNode> commands = new ArrayList<CommandNode>();
            while (current.type != TokenType.EOF) {
                commands.add(parseCommand());
            }
            return new ProgramNode(commands);
        }

        private CommandNode parseCommand() {
            switch (current.type) {
                case ASSIGN: return parseAssign();
                case GET: return parseGet();
                case ADD: return parseBinary("+");
                case SUB: return parseBinary("-");
                case MULT: return parseBinary("*");
                case DIV: return parseBinary("/");
                case MOD: return parseBinary("%");
                case PRINT: return parsePrint();
                case IF: return parseIf();
                case WHILE: return parseWhile();
                case LBRACE: return parseComposite();
                default:
                    throw syntaxError("comando esperado");
            }
        }

        private CommandNode parseAssign() {
            consume(TokenType.ASSIGN, "'=' esperado");
            String variable = consume(TokenType.VARIABLE, "variavel esperada apos '='").lexeme;
            return new AssignNode(variable, parseValue());
        }

        private CommandNode parseGet() {
            consume(TokenType.GET, "'G' esperado");
            String variable = consume(TokenType.VARIABLE, "variavel esperada apos 'G'").lexeme;
            return new GetNode(variable);
        }

        private CommandNode parseBinary(String operator) {
            consume(current.type, "operador esperado");
            String target = consume(TokenType.VARIABLE, "variavel de destino esperada").lexeme;
            ValueNode left = parseValue();
            ValueNode right = parseValue();
            return new BinaryNode(operator, target, left, right);
        }

        private CommandNode parsePrint() {
            consume(TokenType.PRINT, "'P' esperado");
            return new PrintNode(parseValue());
        }

        private CommandNode parseIf() {
            consume(TokenType.IF, "'I' esperado");
            ComparisonNode comparison = parseComparison();
            return new IfNode(comparison, parseCommand());
        }

        private CommandNode parseWhile() {
            consume(TokenType.WHILE, "'W' esperado");
            ComparisonNode comparison = parseComparison();
            return new WhileNode(comparison, parseCommand());
        }

        private CommandNode parseComposite() {
            consume(TokenType.LBRACE, "'{' esperado");
            List<CommandNode> commands = new ArrayList<CommandNode>();
            while (current.type != TokenType.RBRACE && current.type != TokenType.EOF) {
                commands.add(parseCommand());
            }
            consume(TokenType.RBRACE, "'}' esperado");
            return new CompositeNode(commands);
        }

        private ComparisonNode parseComparison() {
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
            return new ComparisonNode(variable, operator, parseValue());
        }

        private ValueNode parseValue() {
            if (current.type == TokenType.VARIABLE || current.type == TokenType.NUMBER) {
                String value = current.lexeme;
                advance();
                return new ValueNode(value);
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

        private RuntimeException syntaxError(String message) {
            return error(message + " perto de '" + current.lexeme + "'", current.line, current.column);
        }
    }

    static RuntimeException error(String message, int line, int column) {
        return new RuntimeException("Erro na linha " + line + ", coluna " + column + ": " + message);
    }
}
