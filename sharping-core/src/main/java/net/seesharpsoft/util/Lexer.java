package net.seesharpsoft.util;

import net.seesharpsoft.UnhandledSwitchCaseException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;

public class Lexer<T> {

    /**
     * Defines a lexer state by its name and a set of target states that can be reached by certain tokens.
     * @param <T> the token type
     */
    public static class State<T> {
        private final String name;
        private Map<State, Set<T>> nextStates;

        private State(String name) {
            this.name = name;
            nextStates = new HashMap<>();
        }

        protected boolean hasDuplicateTokens(List<T> tokens) {
            return nextStates.values().stream()
                    .anyMatch(existingTokens -> tokens.stream().anyMatch(token -> existingTokens.contains(token)));
        }

        public void addNextState(State state, List<T> tokens) {
            if (hasDuplicateTokens(tokens)) {
                throw new IllegalArgumentException(String.format("duplicate tokenInfos: %s - %s", tokens, nextStates.values()));
            }
            if (state == null) {
                throw new IllegalArgumentException("state must not be null!");
            }
            if (nextStates.containsKey(state)) {
                nextStates.get(state).addAll(tokens);
            } else {
                nextStates.put(state, new HashSet<>(tokens));
            }
        }

        public void addNextState(State state, T... tokens) {
            addNextState(state, Arrays.asList(tokens));
        }

        protected State getNextState(T token) {
            for (Map.Entry<State, Set<T>> entry : nextStates.entrySet()) {
                if (entry.getValue().contains(token)) {
                    return entry.getKey();
                }
            }
            return null;
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

    private class TokenValidator implements Tokenizer.Validator<T> {
        @Override
        public boolean validate(T token, String sequence) {
            State nextState = currentState.getNextState(token);
            if (nextState != null) {
                currentState = nextState;
            }
            return nextState != null;
        }
    }

    private final Tokenizer<T> tokenizer;
    private Set<State> states;
    private State initialState;
    private State currentState;

    public Lexer(Tokenizer<T> tokenizer) {
        this.states = new HashSet<>();
        this.tokenizer = tokenizer;
        this.tokenizer.addValidator(new TokenValidator());
    }

    public State addState(String name) {
        State state = new State(name);
        states.add(state);
        if (getInitialState() == null) {
            setInitialState(state);
        }
        return state;
    }

    public State getState(String name) {
        return states.stream().filter(state -> state.name.equals(name)).findFirst().orElse(null);
    }

    public void setInitialState(State state) {
        if (!states.contains(state)) {
            throw new IllegalArgumentException(String.format("state '%s' is not defined in this Lexer instance", state.toString()));
        }
        initialState = state;
    }

    public State getInitialState() {
        return initialState;
    }

    public synchronized List<Tokenizer.TokenInfo<T>> tokenize(String input) throws ParseException {
        currentState = getInitialState();

        return tokenizer.tokenize(input);
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

    /**
     * Experimental.
     * @param resourceName
     */
    public void initStates(String resourceName, Function<String, T> tokenResolver) {
        String lexerStates = null;
        try (InputStream is = ClassLoader.getSystemResourceAsStream(resourceName)) {
            lexerStates = SharpIO.readAsString(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Tokenizer<Integer> stateTokenizer = new Tokenizer();
        stateTokenizer.add(0, "[a-z\\_]+\\:", true);
        stateTokenizer.add(1, "[A-Z\\_]+", true);
        stateTokenizer.add(2, "\\|", true);
        stateTokenizer.add(3, "\\:[a-z\\_]+", true);
        stateTokenizer.add(4, "\\n", true);

        List<Tokenizer.TokenInfo<Integer>> tokenInfos = null;
        try {
            tokenInfos = stateTokenizer.tokenize(lexerStates);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Lexer.State lastState = null;
        String name = null;
        List<T> currentTokens = new ArrayList<>();
        for (Tokenizer.TokenInfo<Integer> tokenInfo : tokenInfos) {
            switch (tokenInfo.token) {
                case 0:
                    name = tokenInfo.sequence.substring(0, tokenInfo.sequence.length() - 1);
                    lastState = getState(name);
                    if (lastState == null) {
                        lastState = addState(name);
                    }
                    currentTokens.clear();
                    break;
                case 1:
                    currentTokens.add(tokenResolver.apply(tokenInfo.sequence));
                    break;
                case 3:
                    name = tokenInfo.sequence.substring(1);
                    Lexer.State nextState = getState(name);
                    if (nextState == null) {
                        nextState = addState(name);
                    }
                    lastState.addNextState(nextState, currentTokens);
                    break;
                case 2:
                case 4:
                    break;
                default:
                    throw new UnhandledSwitchCaseException(tokenInfo.token);
            }

        }
    }
}
