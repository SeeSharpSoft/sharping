package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.data.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.seesharpsoft.spring.data.jpa.expression.Dialect.Token;

public class Dialects {

    private Dialects() {
        // static
    }

    public static final Dialect JAVA;
    public static final Dialect ODATA;
    public static final Dialect SHARP;

    static {
        Dialects.Base dialect = new Dialects.Base();
        dialect.addOperators(
                Pair.of("and", Operators.AND),
                Pair.of("or", Operators.OR),

                Pair.of("eq", Operators.EQUALS),
                Pair.of("ne", Operators.NOT_EQUALS),
                Pair.of("gt", Operators.GREATER_THAN),
                Pair.of("ge", Operators.GREATER_THAN_OR_EQUALS),
                Pair.of("lt", Operators.LESS_THAN),
                Pair.of("le", Operators.LESS_THAN_OR_EQUALS),
                Pair.of("in", Operators.IN),

                Pair.of("add", Operators.ADD),
                Pair.of("sub", Operators.SUB),
                Pair.of("mul", Operators.MUL),
                Pair.of("div", Operators.DIV),
                Pair.of("mod", Operators.MOD)
        );
        dialect.generateOperatorTokenPattern();
        dialect.addTokenPatterns(
                Pair.of(Token.BINARY_OPERATOR_METHOD, "startswith|endswith|substring"),
                Pair.of(Token.UNARY_OPERATOR, "not"),
                Pair.of(Token.NULL, "null"),
                Pair.of(Token.OPERAND, "'.+?'|(?!not|startswith|endswith|substring|null)[^ (),\\[\\]]+|\\[.+?\\]")
        );
        dialect.addOperators(
                Pair.of("substring", Operators.IS_SUBSTRING),
                Pair.of("startsWith", Operators.STARTS_WITH),
                Pair.of("endsWith", Operators.ENDS_WITH),
                Pair.of("not", Operators.NOT)
        );
        ODATA = dialect;

        dialect = new Dialects.Base();
        dialect.addOperators(
                Pair.of("==", Operators.EQUALS),
                Pair.of("!=", Operators.NOT_EQUALS),
                Pair.of(">=", Operators.GREATER_THAN_OR_EQUALS),
                Pair.of("<=", Operators.LESS_THAN_OR_EQUALS),
                Pair.of("&&", Operators.AND),
                Pair.of("||", Operators.OR),

                Pair.of("+", Operators.ADD),
                Pair.of("-", Operators.SUB),
                Pair.of("*", Operators.MUL),
                Pair.of("/", Operators.DIV),
                Pair.of("%", Operators.MOD),

                Pair.of(">", Operators.GREATER_THAN),
                Pair.of("<", Operators.LESS_THAN),

                Pair.of("!", Operators.NOT)
        );
        dialect.addTokenPatterns(
                Pair.of(Token.BINARY_OPERATOR, "==|!=|>=|<=|&&|\\|\\||[+]|[-]|[*]|/|%|>|<"),
                Pair.of(Token.BINARY_OPERATOR_METHOD, ""),
                Pair.of(Token.UNARY_OPERATOR, "[!]"),
                Pair.of(Token.NULL, "null"),
                Pair.of(Token.OPERAND, "'.+?'|(?!null)[^ !(),\\[\\]]+|\\[.+?\\]")
        );
        JAVA = dialect;

        dialect = new Dialects.Base();
        dialect.addOperators(
                Pair.of("==", Operators.EQUALS),
                Pair.of("!=", Operators.NOT_EQUALS),
                Pair.of(">=", Operators.GREATER_THAN_OR_EQUALS),
                Pair.of("<=", Operators.LESS_THAN_OR_EQUALS),
                Pair.of("&&", Operators.AND),
                Pair.of("||", Operators.OR),

                Pair.of("+", Operators.ADD),
                Pair.of("-", Operators.SUB),
                Pair.of("*", Operators.MUL),
                Pair.of("/", Operators.DIV),
                Pair.of("%", Operators.MOD),

                Pair.of(">", Operators.GREATER_THAN),
                Pair.of("<", Operators.LESS_THAN),

                Pair.of("!", Operators.NOT),

                Pair.of("substring", Operators.IS_SUBSTRING),
                Pair.of("startsWith", Operators.STARTS_WITH),
                Pair.of("endsWith", Operators.ENDS_WITH),
                Pair.of("if", Operators.IF),
                Pair.of("count", Operators.COUNT)
        );
        dialect.addTokenPatterns(
                Pair.of(Token.UNARY_OPERATOR, "[!]"),
                Pair.of(Token.BINARY_OPERATOR, "==|!=|>=|<=|&&|\\|\\||[+]|[-]|[*]|/|%|>|<"),
                Pair.of(Token.UNARY_OPERATOR_METHOD, "count"),
                Pair.of(Token.BINARY_OPERATOR_METHOD, "startswith|endswith|substring"),
                Pair.of(Token.TERTIARY_OPERATOR_METHOD, "if"),
                Pair.of(Token.NULL, "null"),
                Pair.of(Token.OPERAND, "'.+?'|(?!startswith|endswith|substring|if|count|null)[^ !(),\\[\\]]+|\\[.+?\\]")
        );
        SHARP = dialect;
    }

    public static class Base implements Dialect {

        private Map<String, Operator> operatorMap;
        private Map<Token, String> tokenPatternMap;
        private Parser parser;

        public Base() {
            this.operatorMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            this.tokenPatternMap = new HashMap<>();
            this.parser = new Parser(this);

            addTokenPatterns(
                    Pair.of(Token.METHOD_PARAMETER_SEPARATOR, ","),
                    Pair.of(Token.BRACKET_OPEN, "\\("),
                    Pair.of(Token.BRACKET_CLOSE, "\\)")
            );
        }

        @Override
        public Operator getOperator(String sequence) {
            return operatorMap.get(sequence);
        }

        protected void addOperator(String sequence, Operator operator) {
            this.operatorMap.put(sequence, operator);
        }

        protected void addOperators(Pair<String, Operator>... operators) {
            Arrays.stream(operators).forEach(pair -> addOperator(pair.getFirst(), pair.getSecond()));
        }

        @Override
        public String getRegexPattern(Token token) {
            return tokenPatternMap.get(token);
        }

        protected void addTokenPattern(Token token, String pattern) {
            this.tokenPatternMap.put(token, pattern);
        }

        protected void addTokenPatterns(Pair<Token, String>... tokenPatterns) {
            Arrays.stream(tokenPatterns).forEach(pair -> addTokenPattern(pair.getFirst(), pair.getSecond()));
        }

        protected void generateOperatorTokenPattern() {
            addTokenPattern(Token.BINARY_OPERATOR, String.join("|",
                    this.operatorMap.keySet().stream().map(key -> Pattern.quote(key)).collect(Collectors.toList()))
            );
        }
    }
}
