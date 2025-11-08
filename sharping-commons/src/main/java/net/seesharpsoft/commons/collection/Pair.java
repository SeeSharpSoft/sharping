package net.seesharpsoft.commons.collection;

import java.util.Objects;

public record Pair<F, S>(F first, S second) {

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", this.first, this.second);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair<?, ?> other)) {
            return false;
        }
        return Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second);
    }

}