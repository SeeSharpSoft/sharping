package net.seesharpsoft.commons.util;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.text.ParseException;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TokenizerUT {

    @Test
    public void tokens_should_be_compared_by_their_field_values() {
        Tokenizer.TokenInfo tokenInfo21 = new Tokenizer.TokenInfo(0, "abc");
        Tokenizer.TokenInfo tokenInfo22 = new Tokenizer.TokenInfo(0, "abc");
        Tokenizer.TokenInfo tokenInfo23 = new Tokenizer.TokenInfo(0, "abcd");

        assertThat(tokenInfo21, equalTo(tokenInfo22));
        assertThat(tokenInfo21.hashCode(), equalTo(tokenInfo22.hashCode()));
        assertThat(tokenInfo21, not(equalTo(tokenInfo23)));
        assertThat(tokenInfo22, not(equalTo(tokenInfo23)));
    }

    @Test
    public void getTokenInfo_should_return_null_if_no_tokeninfo_exists() {
        Tokenizer tokenizer = new Tokenizer();

        tokenizer.add(0, "abc");

        assertThat(tokenizer.getToken(1), nullValue());
    }

    @Test
    public void add_should_add_a_tokeninfo() {
        Tokenizer tokenizer = new Tokenizer();

        tokenizer.add(0, "abc");

        assertThat(tokenizer.getToken(0), notNullValue());
    }

    @Test
    public void tokenize_should_return_empty_list_for_null_input() throws ParseException {
        Tokenizer tokenizer = new Tokenizer();

        tokenizer.add(0, "abc");

        assertThat(tokenizer.tokenize(null).isEmpty(), is(true));
    }

    @Test
    public void tokenize_should_return_empty_list_for_empty_input() throws ParseException {
        Tokenizer tokenizer = new Tokenizer();

        tokenizer.add(0, "abc");

        assertThat(tokenizer.tokenize(" ").isEmpty(), is(true));
    }

    @Test
    public void tokenize_should_return_correct_token() throws ParseException {
        Tokenizer<Integer> tokenizer = new Tokenizer();
        tokenizer.setTrimPattern(" ");

        tokenizer.add(0, "abc");

        assertThat(tokenizer.tokenize("abc "), contains(new Tokenizer.TokenInfo(0, new Tokenizer.CharRange("abc ", 0, 3))));
    }

    @Test
    public void tokenize_should_return_correct_tokens() throws ParseException {
        Tokenizer<Integer> tokenizer = new Tokenizer();
        tokenizer.setTrimPattern(" ");

        tokenizer.add(0, "abc");

        assertThat(tokenizer.tokenize("abc      abc "), Matchers.contains(
                new Tokenizer.TokenInfo(0, new Tokenizer.CharRange("abc      abc ", 0, 3)),
                new Tokenizer.TokenInfo(0, new Tokenizer.CharRange("abc      abc ", 9, 12))));
    }

    @Test
    public void tokenize_should_return_correct_multiple_tokens() throws ParseException {
        Tokenizer<Integer> tokenizer = new Tokenizer();
        tokenizer.setTrimPattern(" ");

        tokenizer.add(0, "abc");
        tokenizer.add(1, "def");

        assertThat(tokenizer.tokenize("def      abc "), Matchers.contains(
                new Tokenizer.TokenInfo(1, new Tokenizer.CharRange("def      abc ", 0, 3)),
                new Tokenizer.TokenInfo(0, new Tokenizer.CharRange("def      abc ", 9, 12))));
    }

}
