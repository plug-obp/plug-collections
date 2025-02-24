package datastructures.bdd.v0;

public abstract class Node<O, V> {
    public boolean isTerminal() {
        return false;
    }

    public boolean isDecision() {
        return false;
    }
}
