package net.seesharpsoft.util;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.text.ParseException;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TokenizerUT {

    private static class ValidatorDummy implements Tokenizer.Validator<Integer> {
        private boolean defaultResult;

        private ValidatorDummy(boolean defaultResult) {
            this.defaultResult = defaultResult;
        }

        @Override
        public boolean validate(Integer token, String sequence) {
            return defaultResult;
        }
    }

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
    public void isValid_should_return_true_if_no_validators_defined() {
        Tokenizer tokenizer = new Tokenizer();

        assertThat(tokenizer.isValid(0, "abc"), is(true));
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

        tokenizer.add(0, "abc");

        assertThat(tokenizer.tokenize("abc "), contains(new Tokenizer.TokenInfo(0, "abc")));
    }

    @Test
    public void tokenize_should_return_correct_tokens() throws ParseException {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        tokenizer.add(0, "abc");

        assertThat(tokenizer.tokenize("abc      abc "), Matchers.contains(
                new Tokenizer.TokenInfo(0, "abc"),
                new Tokenizer.TokenInfo(0, "abc")));
    }

    @Test
    public void tokenize_should_return_correct_multiple_tokens() throws ParseException {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        tokenizer.add(0, "abc");
        tokenizer.add(1, "def");

        assertThat(tokenizer.tokenize("def      abc "), Matchers.contains(
                new Tokenizer.TokenInfo(1, "def"),
                new Tokenizer.TokenInfo(0, "abc")));
    }

    @Test
    public void removeValidator_should_work_with_null_argument_and_return_tokenizer() {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        assertThat(tokenizer.removeValidator(null), is(tokenizer));
    }

    @Test
    public void removeValidator_should_work_for_non_existing_validator_and_return_tokenizer() {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        assertThat(tokenizer.removeValidator(new ValidatorDummy(true)), is(tokenizer));
    }

    @Test
    public void addValidator_should_add_a_validator_and_return_tokenizer() {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        Tokenizer.Validator validator = new ValidatorDummy(true);
        assertThat(tokenizer.addValidator(validator), is(tokenizer));
        assertThat(tokenizer.removeValidator(validator), is(tokenizer));
    }

    @Test
    public void addValidator_should_throw_NullPointerException_with_null_argument() {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        boolean nullPointerException = false;
        try {
            tokenizer.addValidator(null);
        } catch (NullPointerException exc) {
            nullPointerException = true;
        }
        assertThat(nullPointerException, is(true));
    }

    @Test
    public void hasValidator_should_work_with_null_argument_and_return_false() {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        assertThat(tokenizer.hasValidator(null), is(false));
    }

    @Test
    public void hasValidator_should_return_false_for_non_existing_validator() {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        Tokenizer.Validator validator = new ValidatorDummy(true);

        assertThat(tokenizer.hasValidator(validator), is(false));
    }

    @Test
    public void hasValidator_should_return_true_for_existing_validator() {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        Tokenizer.Validator validator = new ValidatorDummy(true);
        tokenizer.addValidator(validator);

        assertThat(tokenizer.hasValidator(validator), is(true));
    }

    @Test
    public void getValidatosr_should_return_an_unmodifiable_list_of_added_validators() {
        Tokenizer<Integer> tokenizer = new Tokenizer();

        Tokenizer.Validator validator = new ValidatorDummy(true);
        tokenizer.addValidator(validator);
        assertThat(tokenizer.getValidators(), contains(validator));
        // TODO check for unmodifiable
    }
}
