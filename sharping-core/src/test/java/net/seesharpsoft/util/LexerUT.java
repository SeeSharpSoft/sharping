package net.seesharpsoft.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class LexerUT {

    private Tokenizer<Integer> tokenizer;

    @Before
    public void beforeEach() {
        tokenizer = new Tokenizer();
        tokenizer.add(0, "abc");
        tokenizer.add(1, "[^ ]+");
        tokenizer.add(2, "def|hij");
    }

    @Test
    public void state_should_not_have_a_public_constructor() {
        assertThat(Lexer.State.class.getConstructors().length, is(0));
    }

    @Test
    public void lexer_should_register_a_validator_to_tokenizer() {
        new Lexer(tokenizer);

        assertThat(tokenizer.getValidators().isEmpty(), is(false));
    }

    @Test
    public void initial_state_should_be_null_at_first() {
        Lexer<Integer> lexer = new Lexer(tokenizer);

        assertThat(lexer.getInitialState(), nullValue());
    }

    @Test
    public void initial_state_should_be_set_to_first_added_state() {
        Lexer<Integer> lexer = new Lexer(tokenizer);

        Lexer.State firstState = lexer.addState("test");
        Lexer.State secondState = lexer.addState("test2");

        assertThat(lexer.getInitialState(), is(firstState));
        assertThat(lexer.getInitialState(), not(is(secondState)));
    }

    @Test
    public void setInitialState_should_change_initial_state() {
        Lexer<Integer> lexer = new Lexer(tokenizer);

        Lexer.State firstState = lexer.addState("test");
        Lexer.State secondState = lexer.addState("test2");
        lexer.setInitialState(secondState);

        assertThat(lexer.getInitialState(), is(secondState));
        assertThat(lexer.getInitialState(), not(is(firstState)));
    }
}
