package datastructures.adfa.bytes.simple_v1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
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

    //TODO: 1) follow existing prefix if no risk of adding unwanted words
    //TODO: 2) increase the granularity from byte to group of bytes
    //TODO: 3) encode state fanout as BDD
    //Simple implementation: add the string, cloning if any match, then merge
    //uses hashconsing to accelerate the hashtable lookup
    public boolean add(byte[] string) {
        boolean alreadyIn = true;
        Stack<State> path = new Stack<>();

        //clone the initial state
        State current = startState;

        for (byte symbol : string) {
            State next = current.next(symbol);

            if (next == null) {
                // add a new state
                next = new State();
                //if a new state is added then the string was not already in
                if (alreadyIn) alreadyIn = false;
            } else {
                // clone and redirect
                next = new State(next);
            }
            current.setNext(symbol, next);
            path.push(next);
            current = next;
        }
        current.setFinal();

        merge(path, string);
        return  alreadyIn;
    }

    public void merge(Stack<State> path, byte[] string) {
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
                }
                precedent.setNext(string[i], equivalentState);
            }
            i--;
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
