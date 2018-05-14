package net.seesharpsoft.spring.test.mock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;

public class MockAnswer implements Answer {

    public static final Object UNDEFINED = new Object();

    protected Object[] properties;

    public MockAnswer() {
    }

    public MockAnswer(Object[] arguments) {
        this.properties = arguments;
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        return handleMethod(invocation.getMethod().getName(), invocation);
    }

    protected Object handleMethod(String methodName, InvocationOnMock invocation) {
        switch (methodName) {
            case "toString":
                return createToString();
            default:
                return UNDEFINED;
        }
    }

    protected String createToString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (properties != null) {
            Arrays.stream(properties)
                    .map(property -> property.toString())
                    .forEach(property -> stringBuilder.append(property));
        }
        return stringBuilder.toString().trim();
    }
}