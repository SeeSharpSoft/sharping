package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

public interface Operator {

    enum Associativity {
        LEFT,
        RIGHT;
    }

    enum NAry {
        UNARY,
        BINARY
    }

    int getPrecedence();

    Associativity getAssociativity();

    NAry getNAry();

    Object evaluate(Object... operands);

    Expression createExpression(Root root,
                                CriteriaQuery query,
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
