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

    //TODO: implement it from:
    /**
     * Fast Generation of Prime-Irredundant Covers from Binary Decision Diagrams. Shin-ichi MINATO.
     * {@link <a href="https://eprints.lib.hokudai.ac.jp/dspace/bitstream/2115/47468/3/59_IEICE76_967.pdf">pdf</a>}
     * */
    public Node<Boolean, V> isop(Node<Boolean, V> bdd) {
        if (isContradiction(bdd)) {
            return bot();
        }
        if (isTautology(bdd)) {
            return top();
        }
        var node = (DecisionNode<Boolean, V>) bdd;
        var v = node.variable;
        var f0 = node.low();
        var f1 = node.high();

        var f0p = bot(); // compute f₀'
        var f1p = bot(); // comput f₁'

        var isop0 = isop(f0p); // recursively generate cubes including ¬v
        var isop1 = isop(f1p); // recursively generate cubes including =v
        return bot();
    }

    public boolean isTautology(Node<Boolean, V> bdd) {
        if (bdd == top()) {
            return true;
        }
        if (bdd == bot()) {
            return false;
        }
        var node = (DecisionNode<Boolean, V>) bdd;
        return isTautology(node.high()) && isTautology(node.low());
    }
    public boolean isContradiction(Node<Boolean, V> bdd) {
        if (bdd == top()) {
            return false;
        }
        if (bdd == bot()) {
            return true;
        }
        var node = (DecisionNode<Boolean, V>) bdd;
        return isContradiction(node.high()) && isContradiction(node.low());
    }


}
