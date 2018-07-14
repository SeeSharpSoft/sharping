package net.seesharpsoft.spring.data.jpa.expression;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class OperationsUT {

    @Test
    public void equals_operation_should_evaluate_correct() {
        assertThat(Operations.equals("a", "b").evaluate(), is(false));
        assertThat(Operations.equals("a", "a").evaluate(), is(true));
        assertThat(Operations.equals(1, 2).evaluate(), is(false));
        assertThat(Operations.equals(0, null).evaluate(), is(false));
        assertThat(Operations.equals(0, false).evaluate(), is(false));
        assertThat(Operations.equals(null, false).evaluate(), is(false));
        assertThat(Operations.equals(false, false).evaluate(), is(true));
        assertThat(Operations.equals(3.0d, 3).evaluate(), is(false));
        assertThat(Operations.equals(42, 42).evaluate(), is(true));
    }
}
