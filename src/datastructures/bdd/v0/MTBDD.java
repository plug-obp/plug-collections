package datastructures.bdd.v0;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MTBDD<O, V> {
    //TODO: hashconsing
    //TODO: memoization in apply
    //TODO: using a weak hash map will enable automatic garbage collecting on the nodeMap
    Map<Node<O,V>, Node<O, V>> nodeMap = new HashMap<>();
    Comparator<V> variableOrder;

    public Node<O, V> intern(Node<O, V> node) {
        return nodeMap.putIfAbsent(node, node);
    }

    public Node<O, V> constant(O value) {
        return intern(new TerminalNode<>(value));
    }

    public Node<O, V> node(V variable, Node<O, V> trueBranch, Node<O, V> falseBranch) {
        if (trueBranch == falseBranch) {
            return trueBranch;
        }
        return intern(new DecisionNode<>(variable, trueBranch, falseBranch));
    }

    public Node<O, V> apply(Function<O, O> operator, Node<O, V> operand) {
        if (operand.isTerminal()) {
            TerminalNode<O, V> operandT = (TerminalNode<O, V>) operand;
            return constant(operator.apply(operandT.value));
        }
        DecisionNode<O, V> operandD = (DecisionNode<O, V>)operand;
        return node(
                operandD.variable,
                apply(operator, operandD.trueBranch),
                apply(operator, operandD.falseBranch)
        );
    }

    public Node<O, V> apply(BiFunction<O, O, O> operator, Node<O, V> left, Node<O, V> right) {
        if (left.isTerminal() && right.isTerminal()) {
            TerminalNode<O, V> leftT = (TerminalNode<O, V>) left;
            TerminalNode<O, V> rightT = (TerminalNode<O, V>) right;
            return constant(operator.apply(leftT.value, rightT.value));
        }

        int leg = 0;
        if (left.isDecision() && right.isDecision()) {
            DecisionNode<O, V> leftD = (DecisionNode<O, V>) left;
            DecisionNode<O, V> rightD = (DecisionNode<O, V>) right;
            leg = variableOrder.compare(leftD.variable, rightD.variable);

            if (leg == 0) { //equal case -- both variable have the same depth
                return node(
                        leftD.variable,
                        apply(operator, leftD.trueBranch, rightD.trueBranch),
                        apply(operator, leftD.falseBranch, rightD.falseBranch)
                );
            }
        }

        if (left.isTerminal() || leg < 0) { // left is terminal node or the left variable depth is smaller than right
            DecisionNode<O, V> rightD = (DecisionNode<O, V>) right;
            return node(
                    rightD.variable,
                    apply(operator, left, rightD.trueBranch),
                    apply(operator, left, rightD.falseBranch)
            );
        }
        if (right.isTerminal() || leg > 0) { // right is terminal node or the right variable depth is smaller than left
            DecisionNode<O, V> leftD = (DecisionNode<O, V>) left;
            return node(
                    leftD.variable,
                    apply(operator, leftD.trueBranch, right),
                    apply(operator, leftD.falseBranch, right)
            );
        }
        //should never get here, the previous conditions cover all cases
        return null;
    }
}
