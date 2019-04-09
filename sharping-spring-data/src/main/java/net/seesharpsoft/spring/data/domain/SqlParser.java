package net.seesharpsoft.spring.data.domain;

import net.seesharpsoft.spring.data.jpa.expression.Operand;

public interface SqlParser  {
    <T extends Operand> T parseExpression(String sqlExpression);
}
