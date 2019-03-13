package net.seesharpsoft.spring.data.domain;

import net.seesharpsoft.spring.data.jpa.expression.Operation;

public interface SqlParser {
    Operation parseExpression(String sqlExpression);
}
