package net.seesharpsoft.commons.collection;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PairTest {
    @Test
    public void should_support_null_in_toString() {
        Pair<?, ?> pair = Pair.of(null, null);

        assertThat(pair.toString(), is("{null, null}"));
    }
}
