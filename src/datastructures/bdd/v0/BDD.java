package datastructures.bdd.v0;

public class BDD<V> extends MTBDD<Boolean, V>  {
    public Node<Boolean, V> bot() {
        return constant(false);
    }
    public Node<Boolean, V> top() {
        return constant(true);
    }

    public Node<Boolean, V> var(V variable) {
        return node(variable, top(), bot());
    }

    //variable negation
    public Node<Boolean, V> rav(V variable) {
        return node(variable, bot(), top());
    }

    public Node<Boolean, V> negation(Node<Boolean, V> bdd) {
        return apply(b -> !b, bdd);
    }

    public Node<Boolean, V> iff(Node<Boolean, V> left, Node<Boolean, V> right) {
        return apply((a, b)-> a==b, left, right);
    }

    public Node<Boolean, V> xor(Node<Boolean, V> left, Node<Boolean, V> right) {
        return apply((a, b)-> a != b, left, right);
    }

    public Node<Boolean, V> and(Node<Boolean, V> left, Node<Boolean, V> right) {
        return apply((a, b)-> a && b, left, right);
    }

    public Node<Boolean, V> or(Node<Boolean, V> left, Node<Boolean, V> right) {
        return apply((a, b)-> a || b, left, right);
    }
}
