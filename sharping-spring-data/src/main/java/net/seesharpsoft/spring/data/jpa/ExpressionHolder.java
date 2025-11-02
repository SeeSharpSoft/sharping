package net.seesharpsoft.spring.data.jpa;

import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.Expression;

public interface ExpressionHolder<T> extends TupleElement<T> {
    Expression<T> getExpression();
}
