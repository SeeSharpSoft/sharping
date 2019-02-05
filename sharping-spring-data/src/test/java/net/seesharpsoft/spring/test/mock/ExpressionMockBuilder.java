package net.seesharpsoft.spring.test.mock;

import org.mockito.invocation.InvocationOnMock;

import javax.persistence.criteria.*;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class ExpressionMockBuilder {

    public static Expression newExpression(String expressionName, Object[] arguments) {
        Expression mock = mock(Expression.class, new ExpressionAnswer(expressionName, arguments));
        return mock;
    }

    public static Predicate newPredicate(String expressionName, Object[] arguments) {
        Predicate mock = mock(Predicate.class, new ExpressionAnswer(expressionName, arguments));
        return mock;
    }

    public static From newFrom(Object[] arguments) {
        From mock = mock(From.class, new FromAnswer(arguments));
        return mock;
    }

    public static Join newJoin(Object[] arguments) {
        Join mock = mock(Join.class, new JoinAnswer(arguments));
        return mock;
    }

    public static Root newRoot(Object[] arguments) {
        Root mock = mock(Root.class, new RootAnswer(arguments));
        return mock;
    }

    public static class ExpressionAnswer extends MockAnswer {
        private String expressionName;

        public ExpressionAnswer(Object[] expressionArguments) {
            super(expressionArguments);
        }

        public ExpressionAnswer(String expressionName, Object[] expressionArguments) {
            this(expressionArguments);
            this.expressionName = expressionName;
        }

        @Override
        protected Object handleMethod(String methodName, InvocationOnMock invocation) {
            switch (methodName) {
                case "getJavaType":
                    return void.class;
                default:
                    return super.handleMethod(methodName, invocation);
            }
        }

        @Override
        protected String createToString() {
            if (expressionName == null) {
                return super.createToString();
            }

            StringBuilder stringBuilder = new StringBuilder();
            switch (expressionName) {
                case "nullLiteral":
                    stringBuilder.append("NULL");
                    break;
                case "literal":
                    stringBuilder.append("'");
                    Arrays.stream(properties).forEach(argument -> stringBuilder.append(argument));
                    stringBuilder.append("'");
                    break;
                default:
                    stringBuilder.append(expressionName);
                    stringBuilder.append("(");
                    for (int i = 0; i < properties.length; ++i) {
                        stringBuilder.append(properties[i]);
                        if (i < properties.length - 1) {
                            stringBuilder.append(", ");
                        }
                    }
                    stringBuilder.append(")");
                    break;
            }
            return stringBuilder.toString().trim();
        }
    }

    public static class FromAnswer extends ExpressionAnswer {
        public FromAnswer(Object[] arguments) {
            super(arguments);
        }

        @Override
        protected Object handleMethod(String methodName, InvocationOnMock invocation) {
            switch (methodName) {
                case "getParentPath":
                    return null;
                default:
                    return super.handleMethod(methodName, invocation);
            }
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Object result = super.answer(invocation);
            if (result != UNDEFINED) {
                return result;
            }

            Class<?> returnType = invocation.getMethod().getReturnType();

            if (returnType.isAssignableFrom(Join.class)) {
                return ExpressionMockBuilder.newJoin(invocation.getArguments());
            }

            throw new UnsupportedOperationException();
        }
    }

    public static class RootAnswer extends FromAnswer {

        public RootAnswer(Object[] arguments) {
            super(arguments);
        }
    }

    public static class JoinAnswer extends FromAnswer {

        public JoinAnswer(Object[] arguments) {
            super(arguments);
        }

        @Override
        protected Object handleMethod(String methodName, InvocationOnMock invocation) {
            switch (methodName) {
                case "getOn":
                    return null;
                default:
                    return super.handleMethod(methodName, invocation);
            }
        }
    }
}
