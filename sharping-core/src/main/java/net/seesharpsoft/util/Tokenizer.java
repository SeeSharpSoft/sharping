package net.seesharpsoft.util;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer<T>
{
    public interface Validator<T> {
        boolean validate(T token, String sequence);
    }

    protected static class Token<T>
    {
        public final Pattern regex;
        public final T token;

        public Token(T token, Pattern regex)
        {
            this.regex = regex;
            this.token = token;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Tokenizer.Token)) {
                return false;
            }
            Token otherToken = (Token)other;
            return Objects.equals(this.token, otherToken.token) && Objects.equals(this.regex, otherToken.regex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.token, this.regex);
        }
    }

    public static class TokenInfo<T>
    {
        public final T token;
        public final String sequence;

        public TokenInfo(T token, String sequence)
        {
            this.token = token;
            this.sequence = sequence;
        }

        @Override
        public String toString() {
            return String.format("%s:%s", this.token, this.sequence);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Tokenizer.TokenInfo)) {
                return false;
            }
            TokenInfo otherTokenInfo = (TokenInfo)other;
            return Objects.equals(this.token, otherTokenInfo.token) && Objects.equals(this.sequence, otherTokenInfo.sequence);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.token, this.sequence);
        }
    }

    private Map<T, Token<T>> tokenInfos;
    private List<Validator> validators;

    public Tokenizer()
    {
        tokenInfos = new HashMap<>();
        validators = new ArrayList<>();
    }

    protected void add(Token<T> token) {
        tokenInfos.put(token.token, token);
    }

    public void add(T token, String regex, boolean caseSensitive) {
        add(new Token(token, Pattern.compile((caseSensitive ? "" : "(?i)") + "^(" + regex + ")")));
    }

    public void add(T token, String regex) {
        this.add(token, regex, false);
    }

    protected Token<T> getTokenInfo(T token) {
        return tokenInfos.get(token);
    }

    public boolean addValidator(Validator validator) {
        if (validator == null) {
            return false;
        }
        return validators.add(validator);
    }

    public boolean removeValidator(Validator validator) {
        return validators.remove(validator);
    }

    protected List<Validator> getValidators() {
        return Collections.unmodifiableList(validators);
    }

    protected <T> boolean isValid(T token, String sequence) {
        return validators.isEmpty() || validators.stream().anyMatch(validator -> validator.validate(token, sequence));
    }

    protected String trim(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replaceAll("\\r\\n", "\n")
                .replaceFirst("^(\r| )*", "")
                .replaceFirst("(\r| )*$", "");
    }

    protected <T> List<TokenInfo<T>> tokenize(String str, Collection<Token<T>> tokenCollection) throws ParseException {
        List<TokenInfo<T>> tokenInfos = new LinkedList<>();
        String trimmedString = trim(str);
        while (!trimmedString.equals(""))
        {
            boolean match = false;
            for (Token<T> info : tokenCollection)
            {
                Matcher matcher = info.regex.matcher(trimmedString);
                if (matcher.find())
                {
                    String sequence = trim(matcher.group());
                    if (isValid(info.token, sequence)) {
                        match = true;
                        trimmedString = trim(matcher.replaceFirst(""));
                        tokenInfos.add(new TokenInfo(info.token, sequence));
                        break;
                    }
                }
            }
            if (!match) {
                throw new ParseException("Unexpected character in input: " + trimmedString, 0);
            }
        }
        return tokenInfos;
    }

    public List<TokenInfo<T>> tokenize(String str) throws ParseException {
        return tokenize(str, tokenInfos.values());
    }
}

