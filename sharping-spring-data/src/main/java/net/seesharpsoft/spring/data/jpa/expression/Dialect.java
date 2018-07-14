package net.seesharpsoft.spring.data.jpa.expression;

public interface Dialect {
    enum Token {
        OPERAND,
        OPERATOR,
        METHOD,
        METHOD_PARAMETER_SEPARATOR,
        UNARY_OPERATOR,
        BRACKET_OPEN,
        BRACKET_CLOSE,
        NULL;
    }

    default boolean isCaseSensitive() {
        return false;
    }

    Operator getOperator(String sequence);

    String getRegexPattern(Token token);
}
