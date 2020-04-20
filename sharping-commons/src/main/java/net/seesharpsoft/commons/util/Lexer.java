package net.seesharpsoft.commons.util;

import net.seesharpsoft.UnhandledSwitchCaseException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Lexer<T> {

    /**
     * Defines a lexer state by its name and a set of target states that can be reached by certain tokens.
     * @param <T> the token type
     */
    public static class State<T> {
        private final String name;
        private Map<T, State> nextStates;

        private State(String name) {
            this.name = name;
            nextStates = new HashMap<>();
        }

        public void addNextState(State state, List<T> tokens) {
            Objects.requireNonNull(state, "state must not be null!");
            tokens.forEach(token -> {
                if (nextStates.containsKey(token)) {
                    throw new IllegalArgumentException(String.format("duplicate tokenInfos in state '%s' for next state '%s': '%s' in '%s'", this, state, token, nextStates.values()));
                }
                nextStates.put(token, state);
            });
        }

        public void addNextState(State state, T... tokens) {
            addNextState(state, Arrays.asList(tokens));
        }

        protected State getNextState(T token) {
            return nextStates.get(token);
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof State)) {
                return false;
            }
            return Objects.equals(this.name, ((State)object).name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.name);
        }
    }

    public static class StateInfo<T> {
        private final State<T> state;
        private List<Tokenizer.TokenInfo<T>> tokenInfos;

        private StateInfo(State state) {
            Objects.requireNonNull(state, "state must not be null!");
            this.state = state;
            tokenInfos = new ArrayList<>();
        }

        public State<T> getState() {
            return state;
        }

        public List<Tokenizer.TokenInfo<T>> getTokenInfos() {
            return Collections.unmodifiableList(tokenInfos);
        }

        public void addToken(Tokenizer.TokenInfo<T> tokenInfo) {
            this.tokenInfos.add(tokenInfo);
        }

        @Override
        public String toString() {
            return String.format("%s (%s)", this.state, this.tokenInfos);
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Lexer.StateInfo)) {
                return false;
            }
            return Objects.equals(this.state, ((StateInfo)object).state)
                    && Objects.equals(this.tokenInfos, ((StateInfo)object).tokenInfos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.state, this.tokenInfos);
        }
    }

    protected final Tokenizer<T> tokenizer;
    protected Set<State> states;
    protected State initialState;
    protected State currentState;

    public Lexer(Tokenizer<T> tokenizer) {
        this.states = new HashSet<>();
        this.tokenizer = tokenizer;
    }

    public Lexer() {
        this(new Tokenizer<>());
    }

    protected Tokenizer<T> getTokenizer() {
        return this.tokenizer;
    }

    public State addState(String name) {
        Objects.requireNonNull(name, "state name must not be null!");
        State state = new State(name);
        states.add(state);
        if (getInitialState() == null) {
            setInitialState(state);
        }
        return state;
    }

    public State getState(String name) {
        Objects.requireNonNull(name, "state name must not be null!");
        return states.stream().filter(state -> state.name.equals(name)).findFirst().orElse(null);
    }

    public void setInitialState(State state) {
        Objects.requireNonNull(state, "state must not be null!");
        if (!states.contains(state)) {
            throw new IllegalArgumentException(String.format("state '%s' is not defined in this Lexer instance", state.toString()));
        }
        initialState = state;
    }

    public State getInitialState() {
        return initialState;
    }

    protected boolean tokenMatcherCallback(T token, String text) {
        State nextState = currentState.getNextState(token);
        if (nextState != null) {
            currentState = nextState;
        }
        return nextState != null;
    }

    public synchronized List<Tokenizer.TokenInfo<T>> tokenize(String input) {
        currentState = getInitialState();

        final Map<State, Collection<Tokenizer.Token<T>>> stateToTokenMap = new HashMap();
        states.stream().forEach(state -> {
            stateToTokenMap.put(state, (Collection)state.nextStates.keySet().stream()
                    .map(tokenKey -> tokenizer.getToken((T)tokenKey))
                    .filter(token -> token != null)
                    .collect(Collectors.toList()));
        });
        List<Tokenizer.TokenInfo<T>> tokenInfos = new ArrayList<>(input.length() / 5);
        CharSequence fullText = input == null ? "" : input.replaceAll("\r\n", "\n");
        int start = tokenizer.trimStart(fullText);
        int end = tokenizer.trimEnd(fullText);
        while (start < end)
        {
            Tokenizer.TokenInfo<T> nextToken = tokenizer.findToken(fullText, start, end, stateToTokenMap.get(currentState), this::tokenMatcherCallback);
            if (nextToken == null) {
                break;
            }
            Tokenizer.CharRange textWithRange = nextToken.textRange();
            start = textWithRange.end() + tokenizer.trimStart(fullText.subSequence(nextToken.textRange().end(), end));
            tokenInfos.add(nextToken);
        }
        return tokenInfos;
    }

    public List<StateInfo<T>> parseStates(List<Tokenizer.TokenInfo<T>> tokenInfos) {
        StateInfo<T> stateInfo = new StateInfo<>(getInitialState());
        List<StateInfo<T>> stateInfos = new ArrayList<>();
        stateInfos.add(stateInfo);

        for (Tokenizer.TokenInfo<T> tokenInfo : tokenInfos) {
            State<T> state = stateInfo.state.getNextState(tokenInfo.token);
            if (!state.equals(stateInfo.state)) {
                stateInfo = new StateInfo<>(state);
                stateInfos.add(stateInfo);
            }

            stateInfo.addToken(tokenInfo);
        }

        return stateInfos;
    }

    public List<StateInfo<T>> parse(String input) throws ParseException {
        return parseStates(tokenize(input));
    }

    public void init(InputStream is, Function<String, T> tokenResolver) throws IOException {
        Objects.requireNonNull(tokenResolver, "tokenResolver must be not null!");

        String lexerStates = null;

        lexerStates = SharpIO.readAsString(is);

        Tokenizer<Integer> stateTokenizer = new Tokenizer();
        stateTokenizer.setTrimPattern("(\n| )");
        stateTokenizer.add(0, "[a-z\\_]+:");
        stateTokenizer.add(1, "[0-9A-Z\\_]+");
        stateTokenizer.add(2, "\\|");
        stateTokenizer.add(3, ":[0-9a-z\\_]+");
        stateTokenizer.add(4, "\\n");
        stateTokenizer.add(5, "=.+?(\\n|$)");

        List<Tokenizer.TokenInfo<Integer>> tokenInfos = null;
        try {
            tokenInfos = stateTokenizer.tokenize(lexerStates);
        } catch (ParseException e) {
            throw new IOException(e);
        }

        Lexer.State lastState = null;
        String name = null;
        List<T> currentTokens = null;
        T currentToken = null;
        for (Tokenizer.TokenInfo<Integer> tokenInfo : tokenInfos) {
            switch (tokenInfo.token) {
                case 0:
                    name = tokenInfo.text().substring(0, tokenInfo.text().length() - 1);
                    lastState = getState(name);
                    if (lastState == null) {
                        lastState = addState(name);
                    }
                    currentTokens = new ArrayList<>();
                    break;
                case 1:
                    currentToken = tokenResolver.apply(tokenInfo.text());
                    break;
                case 3:
                    currentTokens.add(currentToken);
                    name = tokenInfo.text().substring(1);
                    Lexer.State nextState = getState(name);
                    if (nextState == null) {
                        nextState = addState(name);
                    }
                    lastState.addNextState(nextState, currentTokens);
                    currentTokens = null;
                    break;
                case 2:
                    currentTokens.add(currentToken);
                    break;
                case 4:
                    break;
                case 5:
                    this.tokenizer.add(currentToken, tokenInfo.text().substring(1).replaceFirst("\n$", ""));
                    break;
                default:
                    throw new UnhandledSwitchCaseException(tokenInfo.token);
            }
        }
    }

    public void init(String fileName, Function<String, T> tokenResolver) throws IOException {
        try (InputStream is = SharpIO.createInputStream(fileName)) {
            this.init(is, tokenResolver);
        }
    }

    public void init(String fileName) throws IOException {
        this.init(fileName, sequence -> (T)sequence);
    }
}
