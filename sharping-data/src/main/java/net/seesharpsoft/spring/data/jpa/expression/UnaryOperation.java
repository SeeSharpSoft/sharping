package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UnaryOperation implements Operation {
    private final Operator operator;
    private final Operand operand;

    public UnaryOperation(Operator operator, Operand operand) {
        Assert.isTrue(operator.getNAry() == Operator.NAry.UNARY, "unary operator expected!");
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public List<Operand> getOperands() {
        return Collections.singletonList(getOperand());
    }

    public Operand getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return String.format("%s %s", getOperator(), getOperand());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UnaryOperation)) {
            return false;
        }
        UnaryOperation otherOperation = (UnaryOperation) other;
        return Objects.equals(getOperator(), otherOperation.getOperator()) &&
                Objects.equals(getOperand(), otherOperation.getOperand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperator(), getOperand());
    }

}
