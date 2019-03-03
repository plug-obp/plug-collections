package datastructures.bdd.v0;

import java.util.Objects;

public class DecisionNode<O, V> extends Node<O, V> {
    Node<O, V> trueBranch;
    Node<O, V> falseBranch;
    V variable;

    DecisionNode(V variable, Node<O, V> trueBranch, Node<O, V> falseBranch) {
        this.variable = variable;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
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
                DecisionNode<O,V> other = (DecisionNode<O, V>) obj;
                return     trueBranch.equals(other.trueBranch)
                        && falseBranch.equals(other.falseBranch)
                        && variable.equals(other.variable);
            } catch (ClassCastException unused) {
                return false;
            }

        }
        return false;
    }
}
