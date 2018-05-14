package net.seesharpsoft.commons.util;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

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

    @Test
    public void init_should_read_lexer_states_from_file() throws IOException {
        Lexer<Integer> lexer = new Lexer<>(tokenizer);

        lexer.init("/lexer/lexer_init_simple.lex", true);

        assertThat(lexer.getState("start"), notNullValue());
        assertThat(lexer.getState("end"), notNullValue());
        assertThat(lexer.getState("start").getNextState("0"), is(lexer.getState("start")));
        assertThat(lexer.getState("start").getNextState("1"), is(lexer.getState("start")));
        assertThat(lexer.getState("start").getNextState("2"), is(lexer.getState("end")));
        assertThat(lexer.getState("end").getNextState("0"), nullValue());
        assertThat(lexer.getState("end").getNextState("1"), nullValue());
        assertThat(lexer.getState("end").getNextState("2"), nullValue());
    }

    @Test
    public void init_should_read_lexer_states_from_file_with_custom_token_resolver() throws IOException {
        Lexer<Integer> lexer = new Lexer<>(tokenizer);

        lexer.init("/lexer/lexer_init_simple.lex", true, token -> Integer.parseInt(token));

        assertThat(lexer.getState("start"), notNullValue());
        assertThat(lexer.getState("end"), notNullValue());
        assertThat(lexer.getState("start").getNextState(0), is(lexer.getState("start")));
        assertThat(lexer.getState("start").getNextState(1), is(lexer.getState("start")));
        assertThat(lexer.getState("start").getNextState(2), is(lexer.getState("end")));
        assertThat(lexer.getState("end").getNextState(0), nullValue());
        assertThat(lexer.getState("end").getNextState(1), nullValue());
        assertThat(lexer.getState("end").getNextState(2), nullValue());
    }

    @Test
    public void init_should_read_lexer_states_and_tokens_from_file() throws IOException {
        Lexer lexer = new Lexer();

        Tokenizer customTokenizer = lexer.getTokenizer();

        lexer.init("/lexer/lexer_init_with_tokens.lex", true);

        assertThat(lexer.getState("start"), notNullValue());
        assertThat(lexer.getState("end"), notNullValue());
        assertThat(lexer.getState("start").getNextState("0"), is(lexer.getState("start")));
        assertThat(lexer.getState("start").getNextState("1"), is(lexer.getState("start")));
        assertThat(lexer.getState("start").getNextState("2"), is(lexer.getState("end")));
        assertThat(lexer.getState("end").getNextState("0"), nullValue());
        assertThat(lexer.getState("end").getNextState("1"), nullValue());
        assertThat(lexer.getState("end").getNextState("2"), nullValue());
        assertThat(lexer.getTokenizer().getToken("0"), equalTo(customTokenizer.createToken("0", "abc", customTokenizer.getCaseSensitive())));
        assertThat(lexer.getTokenizer().getToken("1"), equalTo(customTokenizer.createToken("1", "[^ ]+", customTokenizer.getCaseSensitive())));
        assertThat(lexer.getTokenizer().getToken("2"), equalTo(customTokenizer.createToken("2", "def|hij", customTokenizer.getCaseSensitive())));
    }

    @Test
    public void init_should_read_lexer_states_and_tokens_from_file_with_custom_token_resolver() throws IOException {
        Lexer<Integer> lexer = new Lexer<>();

        Tokenizer<Integer> customTokenizer = lexer.getTokenizer();

        lexer.init("/lexer/lexer_init_with_tokens.lex", true, token -> Integer.parseInt(token));

        assertThat(lexer.getState("start"), notNullValue());
        assertThat(lexer.getState("end"), notNullValue());
        assertThat(lexer.getState("start").getNextState(0), is(lexer.getState("start")));
        assertThat(lexer.getState("start").getNextState(1), is(lexer.getState("start")));
        assertThat(lexer.getState("start").getNextState(2), is(lexer.getState("end")));
        assertThat(lexer.getState("end").getNextState(0), nullValue());
        assertThat(lexer.getState("end").getNextState(1), nullValue());
        assertThat(lexer.getState("end").getNextState(2), nullValue());
        assertThat(lexer.getTokenizer().getToken(0), equalTo(customTokenizer.createToken(0, "abc", customTokenizer.getCaseSensitive())));
        assertThat(lexer.getTokenizer().getToken(1), equalTo(customTokenizer.createToken(1, "[^ ]+", customTokenizer.getCaseSensitive())));
        assertThat(lexer.getTokenizer().getToken(2), equalTo(customTokenizer.createToken(2, "def|hij", customTokenizer.getCaseSensitive())));
    }
}
