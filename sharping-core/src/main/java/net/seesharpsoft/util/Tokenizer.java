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
            Objects.requireNonNull(token, "token must be not null!");
            Objects.requireNonNull(regex, "regex must be not null!");
            this.regex = regex;
            this.token = token;
        }

        @Override
        public String toString() {
            return String.format("%s=%s (%s)", token, regex.pattern(), regex.flags());
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Tokenizer.Token)) {
                return false;
            }
            Token otherToken = (Token)other;
            return Objects.equals(this.token, otherToken.token) &&
                    Objects.equals(this.regex.pattern(), otherToken.regex.pattern()) &&
                    Objects.equals(this.regex.flags(), otherToken.regex.flags());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.token, this.regex.pattern(), this.regex.flags());
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

    private Map<T, Token<T>> tokenMap;
    private List<Validator> validators;
    private Pattern trimPatternStart;
    private Pattern trimPatternEnd;
    private boolean caseInsensitive = false;

    public Tokenizer()
    {
        tokenMap = new HashMap<>();
        validators = new ArrayList<>();
        setTrimPattern("\r| ");
    }

    public boolean getCaseInsensitive() {
        return this.caseInsensitive;
    }

    public Tokenizer<T> setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
        return this;
    }

    protected Tokenizer<T> add(Token<T> token) {
        tokenMap.put(token.token, token);
        return this;
    }

    protected Token<T> createToken(T token, String regex, boolean caseInsensitive) {
        return new Token(token, Pattern.compile("^(" + regex + ")", caseInsensitive ? Pattern.CASE_INSENSITIVE : 0));
    }

    public Tokenizer<T> add(T token, String regex, boolean caseInsensitive) {
        return add(createToken(token, regex, caseInsensitive));
    }

    public Tokenizer<T> add(T token, String regex) {
        return this.add(token, regex, getCaseInsensitive());
    }

    protected Token<T> getToken(T token) {
        return tokenMap.get(token);
    }

    public Tokenizer<T> addValidator(Validator validator) {
        Objects.requireNonNull(validator, "validator must be not null!");
        validators.add(validator);
        return this;
    }

    public Tokenizer<T> removeValidator(Validator validator) {
        validators.remove(validator);
        return this;
    }

    public boolean hasValidator(Validator validator) {
        return validators.contains(validator);
    }

    protected List<Validator> getValidators() {
        return Collections.unmodifiableList(validators);
    }

    protected <T> boolean isValid(T token, String sequence) {
        return validators.isEmpty() || validators.stream().anyMatch(validator -> validator.validate(token, sequence));
    }

    public Tokenizer<T> setTrimPattern(String regexTrimPattern) {
        if (regexTrimPattern == null || regexTrimPattern.isEmpty()) {
            this.trimPatternStart = null;
            this.trimPatternEnd = null;
        } else {
            this.trimPatternStart = Pattern.compile(String.format("^(%s)*", regexTrimPattern));
            this.trimPatternEnd = Pattern.compile(String.format("(%s)*$", regexTrimPattern));
        }
        return this;
    }

    protected String trim(String input) {
        if (input == null) {
            return "";
        }

        if (trimPatternStart != null) {
            input = trimPatternStart.matcher(input).replaceFirst("");
        }
        if (trimPatternEnd != null) {
            input = trimPatternEnd.matcher(input).replaceFirst("");
        }

        return input;
    }

    protected <T> List<TokenInfo<T>> tokenize(String str, Collection<Token<T>> tokenCollection) throws ParseException {
        List<TokenInfo<T>> tokenInfos = new LinkedList<>();
        String trimmedString = trim(str == null ? null : str.replaceAll("\r\n", "\n"));
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
        return tokenize(str, tokenMap.values());
    }
}

