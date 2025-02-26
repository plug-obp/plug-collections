package datastructures.bdd.v0;

import java.util.Objects;

public class DecisionNode<T, V> extends Node<T, V> {
    Node<T, V> trueBranch;
    Node<T, V> falseBranch;
    V variable;

    DecisionNode(V variable, Node<T, V> trueBranch, Node<T, V> falseBranch) {
        this.variable = variable;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public Node<T, V> high() {
        return trueBranch;
    }
    public Node<T, V> low() {
        return falseBranch;
    }

    @Override
    public boolean isDecision() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trueBranch, falseBranch, variable);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof DecisionNode) {
            try {
                @SuppressWarnings(value = "unchecked")
                DecisionNode<T,V> other = (DecisionNode<T, V>) obj;
                return  variable.equals(other.variable)
                        && trueBranch.equals(other.trueBranch)
                        && falseBranch.equals(other.falseBranch);
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
