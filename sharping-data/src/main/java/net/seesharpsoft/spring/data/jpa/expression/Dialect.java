package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.core.convert.ConversionService;

public interface Dialect {
    Operator getOperator(String sequence);

    String getRegexPattern(OperationParser.Token token);

    Operand parseOperand(String value, ConversionService conversionService);
}
