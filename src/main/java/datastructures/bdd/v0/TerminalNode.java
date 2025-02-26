package datastructures.bdd.v0;

import java.util.Objects;

public class TerminalNode<T, V> extends Node<T, V> {
    T value;

    TerminalNode(T value) {
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
                TerminalNode<T, V> other = (TerminalNode<T, V>) obj;
                return this.value.equals(other.value);
            } catch (ClassCastException unused) {
                return false;
            }
        }
        return false;
    }

    @Override
    public <I, O> O accept(IVisitor<I, O, T, V> visitor, I input) {
        return visitor.visit(this, input);
    }
}
