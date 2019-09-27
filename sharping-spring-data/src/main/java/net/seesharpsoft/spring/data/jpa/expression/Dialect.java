package net.seesharpsoft.spring.data.jpa.expression;

public interface Dialect {
    enum Token {
        OPERAND,
        BINARY_OPERATOR,
        UNARY_OPERATOR,
        UNARY_OPERATOR_METHOD,
        BINARY_OPERATOR_METHOD,
        TERTIARY_OPERATOR_METHOD,
        METHOD_PARAMETER_SEPARATOR,
        BRACKET_OPEN,
        BRACKET_CLOSE,
        NULL
    }

    default boolean isCaseSensitive() {
        return false;
    }

    Operator getOperator(String sequence);

    String getRegexPattern(Token token);

    Parser getParser();
}
