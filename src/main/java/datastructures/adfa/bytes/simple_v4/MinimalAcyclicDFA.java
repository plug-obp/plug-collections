package datastructures.adfa.bytes.simple_v4;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 {@see <a href="http://www.jandaciuk.pl/adfa.html"> jan daciuk </a>}
 unsorted_construction line 365
 */
@SuppressWarnings("Duplicates")
public class MinimalAcyclicDFA {
    int symbolSize = 4;

    public Map<State, State> stateMap = new WeakHashMap<>();
    State startState = new State();

    public MinimalAcyclicDFA() { }

    public MinimalAcyclicDFA(int symbolSize) {
        this.symbolSize = symbolSize;
    }

    public List<byte[]> word(byte[] string) {
        int length = string.length / symbolSize;
        List<byte[]> word = new ArrayList<>(length);
        int i = 0;
        for (; i < length - 1; i++) {
            int offset = i * symbolSize;
            word.add(i, Arrays.copyOfRange(string, offset, offset+symbolSize));
        }
        int offset = i*symbolSize;
        int size = string.length % symbolSize == 0 ? symbolSize : string.length % symbolSize;
        word.add(i, Arrays.copyOfRange(string, offset, offset+size));
        return word;
    }

    public boolean contains(byte[] string) {
        return contains(word(string));
    }

    public boolean contains(List<byte[]> word) {
        if (startState == null) return false;

        State current = startState;

        for ( int i = 0; i<word.size(); i++) {
            State next = current.next(word.get(i));
            if (next == null) {
                return false;
            }
            current = next;
        }
        return current.isFinal();
    }

    public boolean add(byte[] string) {
        return add(word(string));
    }

    //TODO: 3) encode state fanout as BDD
    //Simple implementation: add the string, cloning if any match, then merge
    //uses hashconsing to accelerate the hashtable lookup
    //DOING: 1) follow existing prefix if no risk of adding unwanted words
    //USES: TreeMap instead of HashMap for storing fanout
    //DOING: 2) increase the granularity from byte to group of bytes
    public boolean add(List<byte[]> word) {
        boolean alreadyIn = true;
        Stack<State> path = new Stack<>();
        int modifiedFrom = 0;

        State current = startState;

        for (byte[] symbol : word) {
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
        if (modifiedFrom == word.size() && current.isFinal) {
            //we stayed on the existing DFA, and arrived on a final state, so no need to merge
            return true;
        }

        current.setFinal();
        merge(path, word, modifiedFrom);

        return  alreadyIn;
    }

    public void merge(Stack<State> path, List<byte[]> word, int modifiedFrom) {
        int i = word.size() - 1;
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
                precedent.setNext(word.get(i), equivalentState);
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
