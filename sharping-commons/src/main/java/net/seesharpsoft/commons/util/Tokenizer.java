package net.seesharpsoft.commons.util;

import java.text.ParseException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer<T> {
    public static class Token<T> {
        public final Pattern regex;
        public final T token;

        public Token(T token, Pattern regex) {
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
            Token otherToken = (Token) other;
            return Objects.equals(this.token, otherToken.token) &&
                    Objects.equals(this.regex.pattern(), otherToken.regex.pattern()) &&
                    Objects.equals(this.regex.flags(), otherToken.regex.flags());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.token, this.regex.pattern(), this.regex.flags());
        }
    }

    public static class CharRange {
        public static CharRange EMPTY = new CharRange();

        protected final CharSequence text;
        protected final int start;
        protected final int end;

        public CharRange(CharSequence text, int start, int end) {
            this.text = text == null ? "" : text;
            this.start = start;
            this.end = end;
        }

        public CharRange(CharSequence text, int start) {
            this(text, start, start + text.length());
        }

        public CharRange(CharSequence text) {
            this(text, 0);
        }

        public CharRange() {
            this("");
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Tokenizer.CharRange)) {
                return false;
            }
            CharRange otherTokenInfo = (CharRange) other;
            return Objects.equals(this.text, otherTokenInfo.text) && Objects.equals(this.start, otherTokenInfo.start) && Objects.equals(this.end, otherTokenInfo.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.text, this.start, this.end);
        }

        public CharSequence source() {
            return text;
        }

        public CharSequence text() {
            return text.subSequence(this.start, this.end);
        }

        public int length() {
            return text.length();
        }

        public int start() {
            return this.start;
        }

        public int end() {
            return this.end;
        }
    }

    public static class TokenInfo<T> {
        protected final T token;
        protected final CharRange text;

        public TokenInfo(T token, CharRange text) {
            this.token = token;
            this.text = text;
        }

        public TokenInfo(T token, String text) {
            this(token, new CharRange(text));
        }

        @Override
        public String toString() {
            return String.format("%s:%s", this.token, this.text);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Tokenizer.TokenInfo)) {
                return false;
            }
            TokenInfo otherTokenInfo = (TokenInfo) other;
            return Objects.equals(this.token, otherTokenInfo.token) && Objects.equals(this.text, otherTokenInfo.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.token, this.text);
        }

        public T token() {
            return token;
        }

        public CharRange textRange() {
            return text;
        }

        public String text() {
            return text.text().toString();
        }
    }

    private Map<T, Token<T>> tokenMap;
    private Pattern trimPatternStart;
    private Pattern trimPatternEnd;
    private boolean caseSensitive;

    public Tokenizer() {
        tokenMap = new HashMap<>();
        setCaseSensitive(true);
    }

    public boolean getCaseSensitive() {
        return this.caseSensitive;
    }

    public Tokenizer<T> setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    protected Tokenizer<T> add(Token<T> token) {
        tokenMap.put(token.token, token);
        return this;
    }

    protected Token<T> createToken(T token, String regex, boolean caseSensitive) {
        return new Token(token, Pattern.compile("^(" + regex + ")", caseSensitive ? 0 : Pattern.CASE_INSENSITIVE));
    }

    public Tokenizer<T> add(T token, String regex, boolean caseSensitive) {
        return add(createToken(token, regex, caseSensitive));
    }

    public Tokenizer<T> add(T token, String regex) {
        return this.add(token, regex, getCaseSensitive());
    }

    public Collection<Token<T>> getTokenCollection() {
        return tokenMap.values();
    }

    protected Token<T> getToken(T token) {
        return tokenMap.get(token);
    }

    public Tokenizer<T> setTrimPattern(String regexTrimPattern) {
        if (regexTrimPattern == null || regexTrimPattern.isEmpty()) {
            this.trimPatternStart = null;
            this.trimPatternEnd = null;
        } else {
            this.trimPatternStart = Pattern.compile(String.format("^(%s)*", regexTrimPattern), (getCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE));
            this.trimPatternEnd = Pattern.compile(String.format("(%s)*$", regexTrimPattern), (getCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE));
        }
        return this;
    }

    protected int trimStart(CharSequence input) {
        Matcher matcher;
        if (trimPatternStart == null || !(matcher = trimPatternStart.matcher(input)).find()) {
            return 0;
        }
        return matcher.end();
    }

    protected int trimEnd(CharSequence input) {
        Matcher matcher;
        if (trimPatternEnd == null || !(matcher = trimPatternEnd.matcher(input)).find()) {
            return input.length();
        }
        return matcher.start();
    }

    public <T> TokenInfo<T> findToken(
            CharSequence text,
            int start,
            int end,
            Collection<Token<T>> tokenCollection,
            BiFunction<T, String, Boolean> matcherCallback
    ) {
        CharSequence currentText = text.subSequence(start, end);
        for (Token<T> info : tokenCollection) {
            Matcher matcher = info.regex.matcher(currentText);
            if (matcher.find() &&
                    (matcherCallback == null || matcherCallback.apply(info.token, matcher.group()))
            ) {
                return new TokenInfo(info.token, new CharRange(text, start, start + matcher.end()));
            }
        }
        return null;
    }

    public <T> List<TokenInfo<T>> tokenize(String str, Collection<Token<T>> tokenCollection, BiFunction<T, String, Boolean> matcherCallback) throws ParseException {
        List<TokenInfo<T>> tokenInfos = new LinkedList<>();
        CharSequence fullText = str == null ? "" : str.replaceAll("\r\n", "\n");
        int start = trimStart(fullText);
        int end = trimEnd(fullText);
        while (start < end) {
            TokenInfo<T> nextToken = findToken(fullText, start, end, tokenCollection, matcherCallback);
            if (nextToken == null) {
                break;
            }
            CharRange textWithRange = nextToken.textRange();
            start = textWithRange.end() + trimStart(fullText.subSequence(nextToken.textRange().end(), end));
            tokenInfos.add(nextToken);
        }
        return tokenInfos;
    }

    public List<TokenInfo<T>> tokenize(String str, BiFunction<T, String, Boolean> matcherCallback) throws ParseException {
        return tokenize(str, tokenMap.values(), matcherCallback);
    }

    public List<TokenInfo<T>> tokenize(String str) throws ParseException {
        return tokenize(str, tokenMap.values(), null);
    }
}
