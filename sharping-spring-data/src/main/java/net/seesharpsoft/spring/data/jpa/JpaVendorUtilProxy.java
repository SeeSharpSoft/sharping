package net.seesharpsoft.spring.data.jpa;

import javax.persistence.criteria.Expression;

public interface JpaVendorUtilProxy {
    boolean isAggregateFunction(Expression expression);
}
