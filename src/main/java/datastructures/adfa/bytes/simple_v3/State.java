package datastructures.adfa.bytes.simple_v3;


import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("Duplicates")
public class State {
    int id = -1;
    int hashCode = -1;
    boolean isFinal; //accepting state
    private Map<Byte, State> delta = new TreeMap<>();
    int inCount = 0;

    // Create new state with no transitions
    public State() {
        this.isFinal = false;
    }

    //clone a state
    public State(State s) {
        this.isFinal = s.isFinal();

        for (Map.Entry<Byte, State> transition : s.getDelta().entrySet()) {
            setNext(transition.getKey(), transition.getValue());
        }
    }

    public int hit() {
        return ++inCount;
    }
    public int unhit() {
        if (--inCount == 0) {
            //if I am not reacheable, neither are my targets from me
            for (State target : delta.values()) {
                target.unhit();
            }
        }
        return inCount;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal() {
        isFinal = true;
    }

    public Map<Byte, State> getDelta() {
        return delta;
    }

    public State next(byte label) {
        if (delta == null) return null;
        return delta.get(label);
    }

    //add a new outgoing transition
    // the target should be different from this, if it is not then cycle... which is not allowed
    public void setNext(byte label, State target) {
        if (this == target) return;

        State oldTarget = delta.put(label, target);
        if (oldTarget != null) {
            oldTarget.unhit();
        }
        target.hit();
    }

    @Override
    public int hashCode() {
        return id == -1 ? hashCode = Objects.hash(isFinal, delta) : hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof State)) {
            return false;
        }
        State other = (State) obj;

        //if both interned check only the ID
        if (id != -1 && other.id != -1) {
            return id == other.id;
        }

        if (isFinal != other.isFinal) return false;

        Map<Byte, State> otherDelta = other.getDelta();

        if (delta.size() != otherDelta.size()) return false;

        for (Map.Entry<Byte, State> t1 : delta.entrySet()) {
            //for each entry in this.delta, the other.delta should have the same destination
            State value = otherDelta.get(t1.getKey());
            if (value == null) {
                if (t1.getValue() == null)
                    continue;
                else
                    return false;
            }

            if (!value.equals(t1.getValue())) {
                return false;
            }
        }
        return true;
    }
    class StackEntry {
        Map.Entry<Byte, State> t;
        Iterator<Map.Entry<Byte, State>> iterator;
        public StackEntry(Map.Entry<Byte, State> t, Iterator<Map.Entry<Byte, State>> iterator) {
            this.t = t;
            this.iterator = iterator;
        }
    }

    public void suffixes (Consumer<byte[]> forEachSolution) {
        if (delta == null || delta.size() == 0) return;

        for (Map.Entry<Byte, State> t : delta.entrySet()) {

            Stack<StackEntry> stack = new Stack<>();
            stack.push(new StackEntry(t, null));

            while (!stack.isEmpty()) {
                StackEntry current = stack.peek();

                if (current.iterator == null) {
                    Map<Byte, State> nextDelta = current.t.getValue().getDelta();
                    current.iterator = nextDelta == null ? null : nextDelta.entrySet().iterator();
                }

                if (current.iterator != null && current.iterator.hasNext()) {
                    Map.Entry<Byte, State> next = current.iterator.next();
                    stack.push(new StackEntry(next, null));
                } else {
                    if (current.t.getValue().isFinal()) {
                        if (forEachSolution != null) {
                            byte[] word = new byte[stack.size()];
                            for (int i = 0; i < stack.size(); i++) {
                                word[i] = stack.get(i).t.getKey();
                            }
                            forEachSolution.accept(word);
                        }
                    }
                    stack.pop();
                }
            }
        }
    }

    public int countStates() {
        Set<State> closed = Collections.newSetFromMap(new IdentityHashMap<>());
        Queue<State> open = new LinkedList<>();
        closed.add(this);
        open.add(this);
        while (!open.isEmpty()) {
            State current = open.poll();

            if (current.getDelta() == null) continue;
            for (Map.Entry<Byte, State> t : current.getDelta().entrySet()) {
                State next = t.getValue();

                if (!closed.contains(next)) {
                    closed.add(next);
                    open.add(next);
                }
            }
        }
        return closed.size();
    }

    public String toTGF(Function<State, String> customization) {
        Map<State, Integer> state2id = new IdentityHashMap<>();
        Queue<State> queue = new LinkedList<>();
        int idx = 0;
        state2id.put(this, idx);
        queue.add(this);
        while (!queue.isEmpty()) {
            State current = queue.poll();

            if (current.getDelta() == null) continue;
            for (Map.Entry<Byte, State> t : current.getDelta().entrySet()) {
                State next = t.getValue();
                Integer id = state2id.get(next);
                if (id == null) {
                    state2id.put(next, ++idx);
                    queue.add(next);
                }
            }
        }

        String tgf = "";
        for (State s : state2id.keySet()) {
            tgf += state2id.get(s) + " " + (s.isFinal() ? "f_" : "") + state2id.get(s) + "[i:"+inCount+"]"+ customization.apply(s)+"\n";
        }
        tgf += "#\n";
        for (State s : state2id.keySet()) {
            if (s.getDelta() == null) continue;
            int sourceID = state2id.get(s);
            for (Map.Entry<Byte, State> t : s.getDelta().entrySet()) {
                int targetID = state2id.get(t.getValue());
                tgf += sourceID + " " + targetID + " " + t.getKey() + "\n";
            }
        }
        return tgf;
    }
}
