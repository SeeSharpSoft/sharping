package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BinaryOperation implements Operation {

    private final Operand left;
    private final Operand right;
    private final Operator operator;

    public BinaryOperation(Operand left, Operator operator, Operand right) {
        Assert.isTrue(operator.getNAry() == Operator.NAry.BINARY, "binary operator expected!");
        this.left = left == null ? new Operand(left) : left;
        this.operator = operator;
        this.right = right == null ? new Operand(right) : right;
    }

    public BinaryOperation(Object left, Operator operator, Object right) {
        this(new Operand(left), operator, new Operand(right));
    }

    public Operand getLeftOperand() {
        return left;
    }
    public Operand getRightOperand() {
        return right;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public List<Operand> getOperands() {
        return Arrays.asList(getLeftOperand(), getRightOperand());
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", getLeftOperand(), getOperator(), getRightOperand());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BinaryOperation)) {
            return false;
        }
        BinaryOperation otherOperation = (BinaryOperation) other;
        return Objects.equals(getLeftOperand(), otherOperation.getLeftOperand()) &&
                Objects.equals(getOperator(), otherOperation.getOperator()) &&
                Objects.equals(getRightOperand(), otherOperation.getRightOperand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeftOperand(), getOperator(), getRightOperand());
    }

}
