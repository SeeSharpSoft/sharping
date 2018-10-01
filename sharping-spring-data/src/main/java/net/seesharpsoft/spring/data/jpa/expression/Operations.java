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
            return (Class) getOperands().stream()
                    .filter(operand -> operand instanceof Operand)
                    .map(operand -> ((Operand) operand).getJavaType(root, contexts))
                    .filter(type -> type != null)
                    .findFirst().orElse(null);
        }
    }

}
