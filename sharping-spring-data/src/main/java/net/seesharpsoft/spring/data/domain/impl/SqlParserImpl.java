package net.seesharpsoft.spring.data.domain.impl;

import net.seesharpsoft.spring.data.domain.SqlParser;
import net.seesharpsoft.spring.data.jpa.expression.Operand;
import net.seesharpsoft.spring.data.jpa.expression.Operation;
import net.seesharpsoft.spring.data.jpa.expression.Parser;

import java.text.ParseException;

public class SqlParserImpl implements SqlParser {

    protected final Parser parser;

    public SqlParserImpl(Parser parser) {
        this.parser = parser;
    }

    @Override
    public <T extends Operand> T parseExpression(String sqlExpression) {
        if (sqlExpression == null || sqlExpression.isEmpty()) {
            return null;
        }
        try {
            return parser.parseExpression(sqlExpression);
        } catch (ParseException parseException) {
            throw new RuntimeException(parseException);
        }
    }
}
