package net.seesharpsoft.spring.data.jpa;

import javax.persistence.TupleElement;
import javax.persistence.criteria.Expression;

public interface ExpressionHolder<T> extends TupleElement<T> {
    Expression<T> getExpression();
}
