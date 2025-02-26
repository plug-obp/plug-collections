package datastructures.bdd.v0;

import java.util.function.BiFunction;

public interface IVisitor<I, O, T, V> extends BiFunction<Node<T,V>, I, O> {
    O visit(TerminalNode<T, V> node, I input);
    O visit(DecisionNode<T, V> node, I input);
    O visit(Node<T, V> node, I input);
}
