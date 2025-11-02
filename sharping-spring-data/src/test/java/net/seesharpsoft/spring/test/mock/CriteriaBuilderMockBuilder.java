package net.seesharpsoft.spring.test.mock;

import org.mockito.invocation.InvocationOnMock;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import static org.mockito.Mockito.mock;

public class CriteriaBuilderMockBuilder {

    public static CriteriaBuilder newCriteriaBuilder() {
        CriteriaBuilder builderMock = mock(CriteriaBuilder.class, new CriteriaBuilderMockDefaultAnswer());
        return builderMock;
    }

    private static class CriteriaBuilderMockDefaultAnswer extends MockAnswer {

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Object result = super.answer(invocation);
            if (result != UNDEFINED) {
                return result;
            }

            Class<?> returnType = invocation.getMethod().getReturnType();

            if (returnType.isAssignableFrom(Expression.class)) {
                return ExpressionMockBuilder.newExpression(invocation.getMethod().getName(), invocation.getArguments());
            }
            if (returnType.isAssignableFrom(Predicate.class)) {
                return ExpressionMockBuilder.newPredicate(invocation.getMethod().getName(), invocation.getArguments());
            }

            throw new UnsupportedOperationException();
        }
    }

}
