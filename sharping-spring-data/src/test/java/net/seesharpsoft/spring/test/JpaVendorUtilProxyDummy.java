package net.seesharpsoft.spring.test;

import net.seesharpsoft.spring.data.jpa.JpaVendorUtilProxy;
import org.hibernate.query.criteria.internal.expression.function.FunctionExpression;

import javax.persistence.criteria.Expression;

public class JpaVendorUtilProxyDummy implements JpaVendorUtilProxy {
    @Override
    public boolean isAggregateFunction(Expression expression) {
        return expression instanceof FunctionExpression && ((FunctionExpression) expression).isAggregation();
    }
}
