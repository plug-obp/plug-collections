package datastructures.bdd.v0;

public abstract class Node<T, V> {
    public boolean isTerminal() {
        return false;
    }

    public boolean isDecision() {
        return false;
    }

    public abstract boolean equals(Object obj);
    public abstract int hashCode();
    public <I, O> O accept(IVisitor<I, O, T, V> visitor, I input) {
        return visitor.visit(this, input);
    }
}
