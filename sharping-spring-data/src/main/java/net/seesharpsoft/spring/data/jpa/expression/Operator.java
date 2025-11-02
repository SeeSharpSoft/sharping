package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.util.Assert;

import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import java.util.Arrays;
import java.util.List;

public interface Operator {

    enum Associativity {
        LEFT,
        RIGHT;
    }

    enum NAry {
        UNARY,
        BINARY,
        TERTIARY
    }

    int getPrecedence();

    Associativity getAssociativity();

    NAry getNAry();

    Object evaluate(Object... operands);

    default Class getJavaType(From root, List<TupleElement> contexts, Object... operands) {
        return Arrays.stream(operands)
                .filter(operand -> operand instanceof Operand)
                .map(operand -> ((Operand) operand).getJavaType(root, contexts))
                .filter(type -> type != null)
                .findFirst().orElse(null);
    }

    Expression createExpression(From root,
                                AbstractQuery query,
                                CriteriaBuilder builder,
                                Object... operands);

    default boolean hasHigherPrecedenceThan(Object other) {
        Assert.notNull(other, "otherOperator must not be null!");
        Assert.isInstanceOf(Operator.class, other);

        Operator otherOperator = (Operator) other;
        int precedenceDiff = getPrecedence() - otherOperator.getPrecedence();
        return precedenceDiff < 0 ? false :
                (precedenceDiff > 0 ? true :
                        (otherOperator.getAssociativity() == Associativity.LEFT ? true :
                                (getAssociativity() == Associativity.RIGHT ? false : true)));
    }
}
