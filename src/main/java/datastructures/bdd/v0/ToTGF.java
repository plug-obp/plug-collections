package datastructures.bdd.v0;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.HashMap.*;


public class ToTGF<T, V> implements IVisitor<Map<Node<T, V>, Integer>, ToTGF.TGF, T, V> {
    public static record TGF(Map<Integer, String> nodes, String edges) {
        @Override
        public String toString() {
            return nodes.entrySet()
                    .stream()
                    .reduce("",
                            (a, e) -> a + "\n" + e.getKey() + " " +e.getValue(),
                            (a, b) -> a + b)
                    + "\n#\n" + edges;
        }
    }
    /**
     * @param nodeIntegerMap the function argument
     * @return
     */
    @Override
    public TGF apply(Node<T, V> node, Map<Node<T, V>, Integer> nodeIntegerMap) {
        return node.accept(this, nodeIntegerMap);
    }

    /**
     * @param node, the BDD node.
     * @param input, an identity map of BDD nodes to names.
     * @return a TGF string representation of this node.
     */
    @Override
    public TGF visit(Node<T, V> node, Map<Node<T, V>, Integer> input) {
        return new TGF(Collections.emptyMap(), "");
    }

    /**
     * @param node
     * @param input
     * @return a TGF string representation of this node.
     */
    @Override
    public TGF visit(TerminalNode<T, V> node, Map<Node<T, V>, Integer> input) {
        var id = input.computeIfAbsent(node, n -> input.size() );
        return new TGF(Collections.singletonMap(id, node.value.toString()), "");
    }

    /**
     * @param node
     * @param input
     * @return a TGF string representation of this node.
     */
    @Override
    public TGF visit(DecisionNode<T, V> node, Map<Node<T, V>, Integer> input) {
        var id = input.computeIfAbsent(node, n -> input.size() );

        var loTGF = node.high().accept(this, input);
        var hiTGF = node.low().accept(this, input);

        var nodes = new HashMap<>(Map.of(id, node.variable.toString()));
        nodes.putAll(hiTGF.nodes);
        nodes.putAll(loTGF.nodes);

        var edges =     id + " " + input.get(node.high()) + " 1\n"
                    +   id + " " + input.get(node.low())  + " 0"
                    + (loTGF.edges().isEmpty() ? "" : "\n" + loTGF.edges())
                    + (hiTGF.edges().isEmpty() ? "" : "\n" + hiTGF.edges());

        return new TGF(
          nodes,
          edges
        );
    }
}
