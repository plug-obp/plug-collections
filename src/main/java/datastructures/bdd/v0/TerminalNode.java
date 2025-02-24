package datastructures.bdd.v0;

import java.util.Objects;

public class TerminalNode<O, V> extends Node<O, V> {
    O value;

    TerminalNode(O value) {
        this.value = value;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof TerminalNode) {
            try {
                @SuppressWarnings(value = "unchecked")
                TerminalNode<O, V> other = (TerminalNode<O, V>) obj;
                return this.value.equals(other.value);
            } catch (ClassCastException unused) {
                return false;
            }
        }
        return false;
    }
}
