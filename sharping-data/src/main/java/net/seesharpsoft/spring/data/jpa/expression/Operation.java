package net.seesharpsoft.spring.data.jpa.expression;

import java.util.List;

public interface Operation {
    Operator getOperator();

    List<Operand> getOperands();
}
