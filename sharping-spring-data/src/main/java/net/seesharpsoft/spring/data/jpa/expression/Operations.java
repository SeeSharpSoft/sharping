package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.util.Assert;

import javax.persistence.TupleElement;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Operations {

    private Operations() {
        // static
    }

    public static Operation equals(Object first, Object second) {
        return new Binary(Operators.EQUALS, first, second);
    }

    public static Operation lessThan(Object first, Object second) {
        return new Binary(Operators.LESS_THAN, first, second);
    }

    public static Operation lessThanOrEquals(Object first, Object second) {
        return new Binary(Operators.LESS_THAN_OR_EQUALS, first, second);
    }

    public static Operation greaterThan(Object first, Object second) {
        return new Binary(Operators.GREATER_THAN, first, second);
    }

    public static Operation greaterThanOrEquals(Object first, Object second) {
        return new Binary(Operators.GREATER_THAN_OR_EQUALS, first, second);
    }

    public static Operation not(Object first) {
        return new Unary(Operators.NOT, first);
    }

    public static Operation in(Object first, Object second) {
        return new Binary(Operators.IN, first, second);
    }

    public static Operation and(Object first, Object second) {
        return new Binary(Operators.AND, first, second);
    }

    public static Operation or(Object first, Object second) {
        return new Binary(Operators.OR, first, second);
    }

    public static Operation ifElse(Object condition, Object ifCase, Object elseCase) { return new Tertiary(Operators.IF, condition, ifCase, elseCase); }

    public static class Unary extends Base {
        public Unary(Operator operator, Object operand) {
            super(operator, operand);
            Assert.isTrue(operator.getNAry() == Operator.NAry.UNARY, "unary operator expected!");
        }

        public Object getOperand() {
            return getOperands().get(0);
        }

        @Override
        public String toString() {
            return String.format("%s %s", getOperator(), getOperand());
        }
    }

    public static class Binary extends Base {
        public Binary(Operator operator, Object firstOperand, Object secondOperand) {
            super(operator, firstOperand, secondOperand);
            Assert.isTrue(operator.getNAry() == Operator.NAry.BINARY, "binary operator expected!");
        }

        public Object getLeftOperand() {
            return getOperands().get(0);
        }

        public Object getRightOperand() {
            return getOperands().get(1);
        }

        @Override
        public String toString() {
            return String.format("(%s %s %s)", getLeftOperand(), getOperator(), getRightOperand());
        }
    }

    public static class Tertiary extends Base {
        public Tertiary(Operator operator, Object firstOperand, Object secondOperand, Object thirdOperand) {
            super(operator, firstOperand, secondOperand, thirdOperand);
            Assert.isTrue(operator.getNAry() == Operator.NAry.TERTIARY, "tertiary operator expected!");
        }

        public Object getFirstOperand() {
            return getOperands().get(0);
        }

        public Object getSecondOperand() {
            return getOperands().get(1);
        }

        public Object getThirdOperand() {
            return getOperands().get(2);
        }

        @Override
        public String toString() {
            return String.format("%s(%s,%s,%s)", getOperator(), getFirstOperand(), getSecondOperand(), getThirdOperand());
        }
    }

    public static class Base implements Operation {
        private final Operator operator;
        private final List operands;

        public Base(Operator operatorArg, Object... operandsArg) {
            Assert.notNull(operatorArg, "operator must not be null!");
            this.operator = operatorArg;
            this.operands = Arrays.asList(operandsArg);
        }

        @Override
        public Operator getOperator() {
            return operator;
        }

        @Override
        public List getOperands() {
            return Collections.unmodifiableList(operands);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Operation)) {
                return false;
            }
            Operation otherOperation = (Operation) other;
            return Objects.equals(getOperator(), otherOperation.getOperator()) &&
                    Objects.equals(getOperands(), otherOperation.getOperands());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getOperator(), Arrays.hashCode(getOperands().toArray()));
        }

        @Override
        public Class getJavaType(Root root, List<TupleElement> contexts) {
            return getOperator().getJavaType(root, contexts, getOperands());
        }
    }

}
