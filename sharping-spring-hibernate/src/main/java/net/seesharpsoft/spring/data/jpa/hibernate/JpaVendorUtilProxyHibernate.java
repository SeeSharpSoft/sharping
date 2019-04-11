package net.seesharpsoft.spring.data.jpa.hibernate;

import net.seesharpsoft.spring.data.jpa.JpaVendorUtilProxy;
import org.hibernate.query.criteria.internal.expression.function.FunctionExpression;

import javax.persistence.criteria.Expression;

public class JpaVendorUtilProxyHibernate implements JpaVendorUtilProxy {
    @Override
    public boolean isAggregateFunction(Expression expression) {
        return expression instanceof FunctionExpression && ((FunctionExpression) expression).isAggregation();
    }
}
