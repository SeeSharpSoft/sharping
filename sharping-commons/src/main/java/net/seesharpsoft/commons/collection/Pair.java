package net.seesharpsoft.commons.collection;

import java.util.Objects;

public class Pair<F, S> {

    public static <F, S> Pair<F,S> of(F first, S second) {
        return new Pair(first, second);
    }

    private final F first;
    private final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "{" + (first == null ? "null" : first.toString()) + ", " + (second == null ? "null" : second.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair other = (Pair)obj;
        return Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.first, this.second);
    }
}