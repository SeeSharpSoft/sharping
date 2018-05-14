package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Dialects {

    private Dialects() {
        // static
    }

    public static final Dialect JAVA;
    public static final Dialect ODATA;

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
                Pair.of(OperationParser.Token.METHOD, "startswith|endswith|substring"),
                Pair.of(OperationParser.Token.UNARY_OPERATOR, "not"),
                Pair.of(OperationParser.Token.OPERAND, "'.+?'|(?!not|startswith|endswith|substring)[^ (),\\[\\]]+|\\[.+?\\]")
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
                Pair.of(OperationParser.Token.OPERATOR, "==|!=|>=|<=|&&|\\|\\||[+]|[-]|[*]|/|%|>|<"),
                Pair.of(OperationParser.Token.METHOD, ""),
                Pair.of(OperationParser.Token.UNARY_OPERATOR, "[!]"),
                Pair.of(OperationParser.Token.OPERAND, "'.+?'|[^ !(),\\[\\]]+|\\[.+?\\]")
        );
        JAVA = dialect;
    }

    public static class Base implements Dialect {

        private Map<String, Operator> operatorMap;
        private Map<OperationParser.Token, String> tokenPatternMap;

        public Base() {
            this.operatorMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            this.tokenPatternMap = new HashMap<>();

            addTokenPatterns(
                    Pair.of(OperationParser.Token.METHOD_PARAMETER_SEPARATOR, ","),
                    Pair.of(OperationParser.Token.BRACKET_OPEN, "\\("),
                    Pair.of(OperationParser.Token.BRACKET_CLOSE, "\\)")
            );
        }

        @Override
        public Operator getOperator(String sequence) {
            return operatorMap.get(sequence);
        }

        public void addOperator(String sequence, Operator operator) {
            this.operatorMap.put(sequence, operator);
        }

        public void addOperators(Pair<String, Operator>... operators) {
            Arrays.stream(operators).forEach(pair -> addOperator(pair.getFirst(), pair.getSecond()));
        }

        public void removeOperator(String sequence) {
            this.operatorMap.remove(sequence);
        }

        public void removeOperators(String... operators) {
            Arrays.stream(operators).forEach(operator -> removeOperator(operator));
        }

        public void clearOperators() {
            this.operatorMap.clear();
        }

        @Override
        public String getRegexPattern(OperationParser.Token token) {
            return tokenPatternMap.get(token);
        }

        @Override
        public Operand parseOperand(String value, ConversionService conversionService) {
            Assert.notNull(value, "value must not be null!");
            OperandType type = OperandType.parse(value);
            if (type == null) {
                return new Operand(value, Operand.Type.PATH);
            }
            return new Operand(type.convert(conversionService, value), Operand.Type.OBJECT, type.getJavaType());
        }

        public void addTokenPattern(OperationParser.Token token, String pattern) {
            this.tokenPatternMap.put(token, pattern);
        }

        public void addTokenPatterns(Pair<OperationParser.Token, String>... tokenPatterns) {
            Arrays.stream(tokenPatterns).forEach(pair -> addTokenPattern(pair.getFirst(), pair.getSecond()));
        }

        public void removeTokenPattern(OperationParser.Token token) {
            this.tokenPatternMap.remove(token);
        }

        public void removeOperators(OperationParser.Token... tokens) {
            Arrays.stream(tokens).forEach(token -> removeTokenPattern(token));
        }

        public void clearTokenPatterns() {
            this.tokenPatternMap.clear();
        }

        public void generateOperatorTokenPattern() {
            addTokenPattern(OperationParser.Token.OPERATOR, String.join("|",
                    this.operatorMap.keySet().stream().map(key -> Pattern.quote(key)).collect(Collectors.toList()))
            );
        }

        public Dialect clone() {
            Dialects.Base result = new Dialects.Base();
            result.addOperators(
                    this.operatorMap.entrySet().stream()
                    .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList())
                    .toArray(new Pair[0])
            );
            result.addTokenPatterns(
                    this.tokenPatternMap.entrySet().stream()
                    .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList())
                    .toArray(new Pair[0])
            );
            return result;
        }
    }
}
