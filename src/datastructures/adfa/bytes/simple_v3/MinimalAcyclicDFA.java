package datastructures.adfa.bytes.simple_v3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 {@link <a href="http://www.jandaciuk.pl/adfa.html"> jan daciuk </a>}
 */
@SuppressWarnings("Duplicates")
public class MinimalAcyclicDFA {

    public Map<State, State> stateMap = new WeakHashMap<>();
    State startState = new State();

    public boolean contains(byte[] string) {
        if (startState == null) return false;

        State current = startState;

        for ( int i = 0; i<string.length; i++) {
            State next = current.next(string[i]);
            if (next == null) {
                return false;
            }
            current = next;
        }
        return current.isFinal();
    }

    //TODO: 2) increase the granularity from byte to group of bytes
    //TODO: 3) encode state fanout as BDD
    //Simple implementation: add the string, cloning if any match, then merge
    //uses hashconsing to accelerate the hashtable lookup
    //DOING: 1) follow existing prefix if no risk of adding unwanted words
    //USES: TreeMap instead of HashMap for storing fanout
    public boolean add(byte[] string) {
        boolean alreadyIn = true;
        Stack<State> path = new Stack<>();
        int modifiedFrom = 0;

        State current = startState;

        for (byte symbol : string) {
            State next = current.next(symbol);

            if (next == null) {
                // add a new state
                next = new State();
                //if a new state is added then the string was not already in
                if (alreadyIn) alreadyIn = false;
                if (current != startState && current.id != -1) extern(current);
            }
            else if (next.inCount > 1) {
                // clone and redirect
                next = new State(next);
                if (current != startState && current.id != -1) extern(current);
            } else {
                //stay on the existing DFA as long as there is no risk of adding unwanted words (inCount <=1)
                modifiedFrom++;
            }
            current.setNext(symbol, next);
            path.push(next);
            current = next;
        }
        if (modifiedFrom == string.length && current.isFinal) {
            //we stayed on the existing DFA, and arrived on a final state, so no need to merge
            return true;
        }

        current.setFinal();
        merge(path, string, modifiedFrom);

        return  alreadyIn;
    }

    public void merge(Stack<State> path, byte[] string, int modifiedFrom) {
        int i = string.length - 1;
        while (!path.isEmpty()) {
            State current = path.pop();
            State equivalentState = intern(current);

            if (equivalentState != current) {
                //redirect to the equivalent state
                State precedent;
                if (path.isEmpty()) {
                    precedent = startState;
                } else {
                    precedent = path.peek();
                    if (i < modifiedFrom) {
                        //if i < modifiedFrom, then the states are already registered,
                        //unregister them so that we can regroup even more
                        extern(precedent);
                    }
                }
                precedent.setNext(string[i], equivalentState);
            } else {
                if (i < modifiedFrom) {
                    //all previous states are already registered
                    break;
                }
            }
            i--;
        }
    }

    public void extern(State state) {
        State removed = stateMap.remove(state);
        if (removed != null) {
            removed.id = -1;
        }
    }
    public State intern(State state) {
        State equivalent = stateMap.get(state);
        if (equivalent == null) {
            state.id = stateMap.size();
            stateMap.put(state, state);
            return state;
        }
        return equivalent;
    }

    public static MinimalAcyclicDFA from(List<byte[]> strings) {
        MinimalAcyclicDFA dfa = new MinimalAcyclicDFA();

        for (byte[] string : strings) {
            dfa.add(string);
        }

        return dfa;
    }

    public void words(Consumer<byte[]> forEachSolution) {
        startState.suffixes(forEachSolution);
    }

    public void toTGF(File outfile) {
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(outfile));
            bos.write(startState.toTGF(c -> {
                State s = stateMap.get(c);
                return s != null && s == c ? "[inHash]" : "";
            }));
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toTGF(State start, File outfile) {
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(outfile));
            bos.write(start.toTGF(c -> {
                State s = stateMap.get(c);
                return s != null && s == c ? "[inHash]" : "";
            }));
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int countStates(){
        return startState.countStates();
    }
}
