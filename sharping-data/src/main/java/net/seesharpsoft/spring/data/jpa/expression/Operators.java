package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.spring.data.jpa.expression.Operator.NAry;
import net.seesharpsoft.util.TriFunction;
import org.springframework.util.Assert;

import javax.persistence.criteria.*;
import java.util.function.BiFunction;

public class Operators {

    private Operators() {
        // static
    }

    public static final Operator AND = new Operators.Binary("&&", 40, CriteriaBuilder::and);
    public static final Operator OR = new Operators.Binary("||", 30, CriteriaBuilder::or);
    public static final Operator NOT = new Operators.Unary("!", 140, CriteriaBuilder::not) {
        @Override
        public Associativity getAssociativity() {
            return Associativity.RIGHT;
        }
    };

    public static final Operator GREATER_THAN = new Operators.Binary(">", 90, CriteriaBuilder::greaterThan);
    public static final Operator GREATER_THAN_OR_EQUALS = new Operators.Binary(">=", 90, CriteriaBuilder::greaterThanOrEqualTo);
    public static final Operator LESS_THAN = new Operators.Binary("<", 90, CriteriaBuilder::lessThan);
    public static final Operator LESS_THAN_OR_EQUALS = new Operators.Binary("<=", 90, CriteriaBuilder::lessThanOrEqualTo);
    public static final Operator IN = new Operators.Binary("IN", 90, (criteriaBuilder, left, right) -> criteriaBuilder.in(left).value(right));

    public static final Operator ADD = new Operators.Binary("+", 110, CriteriaBuilder::sum);
    public static final Operator SUB = new Operators.Binary("-", 110, CriteriaBuilder::diff);
    public static final Operator MUL = new Operators.Binary("*", 120, CriteriaBuilder::prod);
    public static final Operator DIV = new Operators.Binary("/", 120, CriteriaBuilder::quot);
    public static final Operator MOD = new Operators.Binary("%", 125, CriteriaBuilder::mod);

    public static final Operator EQUALS = new Operators.Base("==", NAry.BINARY, 80) {
        @Override
        public Expression getExpression(Root root, CriteriaQuery query, CriteriaBuilder builder, Operand... operands) {
            Assert.isTrue(operands.length == 2, "exactly one operand expected for unary operator!");
            Operand leftOperand = operands[0];
            Operand rightOperand = operands[1];
            Expression left = leftOperand.asExpression(root, query, builder);
            Expression right = rightOperand.asExpression(root, query, builder);

            if (left instanceof Join || right instanceof Join) {
                query.distinct(true);
            }

            if (rightOperand.getValue() == null && leftOperand.getValue() == null) {
                return builder.and();
            } else if (leftOperand.getValue() == null) {
                return Iterable.class.isAssignableFrom(right.getJavaType()) ? builder.isEmpty(right) : builder.isNull(right);
            } else if (rightOperand.getValue() == null) {
                return Iterable.class.isAssignableFrom(left.getJavaType()) ? builder.isEmpty(left) : builder.isNull(left);
            }
            return builder.equal(left, right);
        }
    };

    public static final Operator NOT_EQUALS = new Operators.Base("!=", NAry.BINARY, 80) {
        @Override
        public Expression getExpression(Root root, CriteriaQuery query, CriteriaBuilder builder, Operand... operands) {
            Assert.isTrue(operands.length == 2, "exactly two operands expected for binary operator!");
            return builder.not(EQUALS.getExpression(root, query, builder, operands));
        }
    };

    public static final Operator IS_SUBSTRING = new Operators.Base("includes", NAry.BINARY, 90) {
        @Override
        public Expression getExpression(Root root, CriteriaQuery query, CriteriaBuilder builder, Operand... operands) {
            Assert.isTrue(operands.length == 2, "exactly two operands expected for binary operator!");
            return builder.like(operands[0].asExpression(root, query, builder), String.format("\\%%s\\%", operands[1].getValueAsString()));
        }
    };
    public static final Operator STARTS_WITH = new Operators.Base("startsWith", NAry.BINARY, 90) {
        @Override
        public Expression getExpression(Root root, CriteriaQuery query, CriteriaBuilder builder, Operand... operands) {
            Assert.isTrue(operands.length == 2, "exactly two operands expected for binary operator!");
            return builder.like(operands[0].asExpression(root, query, builder), String.format("%s\\%", operands[1].getValueAsString()));
        }
    };
    public static final Operator ENDS_WITH = new Operators.Base("endsWith", NAry.BINARY, 90) {
        @Override
        public Expression getExpression(Root root, CriteriaQuery query, CriteriaBuilder builder, Operand... operands) {
            Assert.isTrue(operands.length == 2, "exactly two operands expected for binary operator!");
            return builder.like(operands[0].asExpression(root, query, builder), String.format("\\%%s", operands[1].getValueAsString()));
        }
    };

    public static class Unary extends Operators.Base {
        private final BiFunction<CriteriaBuilder, Expression, Expression> expressionFunction;

        public Unary(String name, int precedence, BiFunction<CriteriaBuilder, Expression, Expression> expressionFunction) {
            super(name, NAry.UNARY, precedence);
            this.expressionFunction = expressionFunction;
        }

        protected Expression getExpression(Root root, CriteriaQuery query, CriteriaBuilder builder, Expression expression) {
            Assert.notNull(this.expressionFunction, "function must be defined!");
            return expressionFunction.apply(builder, expression);
        }

        @Override
        public Expression getExpression(Root root, CriteriaQuery query, CriteriaBuilder builder, Operand... operands) {
            Assert.isTrue(operands.length == 1, "exactly one operand expected for unary operator!");
            Expression expression = operands[0].asExpression(root, query, builder);
            return getExpression(root, query, builder, expression);
        }
    }

    public static class Binary extends Operators.Base {
        private final TriFunction<CriteriaBuilder, Expression, Expression, Expression> expressionFunction;

        public Binary(String name, int precedence, TriFunction<CriteriaBuilder, Expression, Expression, Expression> expressionFunction) {
            super(name, NAry.BINARY, precedence);
            this.expressionFunction = expressionFunction;
        }

        protected Expression getExpression(Root root, CriteriaQuery query, CriteriaBuilder builder, Expression left, Expression right) {
            Assert.notNull(this.expressionFunction, "function must be defined!");
            return expressionFunction.apply(builder, left, right);
        }

        @Override
        public Expression getExpression(Root root, CriteriaQuery query, CriteriaBuilder builder, Operand... operands) {
            Assert.isTrue(operands.length == 2, "exactly one operand expected for unary operator!");
            Expression left = operands[0].asExpression(root, query, builder);
            Expression right = operands[1].asExpression(root, query, builder);

            if (left instanceof Join || right instanceof Join) {
                query.distinct(true);
            }
            return getExpression(root, query, builder, left, right);
        }
    }

    public static abstract class Base implements Operator {
        private final String name;
        private final NAry nary;
        private final int precedence;

        public Base(String name, NAry nary, int precedence) {
            this.name = name;
            this.nary = nary;
            this.precedence = precedence;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public int getPrecedence() {
            return this.precedence;
        }

        @Override
        public Associativity getAssociativity() {
            return Associativity.LEFT;
        }

        @Override
        public NAry getNAry() {
            return this.nary;
        }
    }
}
