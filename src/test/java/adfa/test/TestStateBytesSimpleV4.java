package adfa.test;

import datastructures.adfa.bytes.simple_v4.MinimalAcyclicDFA;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class TestStateBytesSimpleV4 {

    @Test
    public void testL() {
        List<byte[]> lines = new ArrayList() {{
            add(new byte[] {1,2,3,4});

            add(new byte[] {2,2,3,4});
            add(new byte[] {2,2,3,5});
            add(new byte[] {1,2,3,5});
            add(new byte[] {1,2});

        }};

        MinimalAcyclicDFA dfa0 = new MinimalAcyclicDFA();
        for (byte[] line : lines) {
            dfa0.add(line);
            dfa0.words(c -> System.out.println(Arrays.toString(c)));
            dfa0.toTGF(new File("y.tgf"));
            System.out.println("hashsize: " + dfa0.stateMap.size());
            System.out.println("automata size: " + dfa0.countStates());
            System.out.println("--------------------");
        }
        dfa0.toTGF(new File("y.tgf"));

    }

    @Test
    public void testL1() {
        List<byte[]> lines = new ArrayList() {{
            add(new byte[] {1,2,3,4,1,2,3,4});

            add(new byte[] {2,2,3,4,1,2,3,4});
            add(new byte[] {2,2,3,5,1,2,3,4});
            add(new byte[] {1,2,3,5,1,2,3,4});
            add(new byte[] {1,2});

        }};

        MinimalAcyclicDFA dfa0 = new MinimalAcyclicDFA();
        for (byte[] line : lines) {
            dfa0.add(line);
            dfa0.words(c -> System.out.println(Arrays.toString(c)));
            dfa0.toTGF(new File("y.tgf"));
            System.out.println("hashsize: " + dfa0.stateMap.size());
            System.out.println("automata size: " + dfa0.countStates());
            System.out.println("--------------------");
        }
        dfa0.toTGF(new File("y.tgf"));

    }

    @Test
    public void testLong() {
        MinimalAcyclicDFA dfa0 = new MinimalAcyclicDFA(10);
        long startTime = System.currentTimeMillis();
        int alreadyIn = 0;
        for (int i = 0; i < 50000; i++) {
            byte[] arr = newMot(1000);
            dfa0.add(arr);
            if (dfa0.add(arr)) {
                alreadyIn++;
            }
            //dfa0.words(System.out::println);
            //System.out.println("--------------------");
        }
        long endTime = System.currentTimeMillis();
        System.err.println(endTime - startTime + " milliseconds");
        System.out.println("hashsize: " + dfa0.stateMap.size());
        System.out.println("automata size: " + dfa0.countStates());
        System.out.println("collisions: " + alreadyIn);
        //dfa0.toTGF(new File("y.tgf"));
        //dfa0.words(c -> System.out.println(Arrays.toString(c)));

        Map<X, X> map = new HashMap<>();
         startTime = System.currentTimeMillis();
        for (int i = 0; i< 50000; i++) {
            X x = new X(newMot(1000));
            map.put(x, x);
            //dfa0.words(System.out::println);
            //System.out.println("--------------------");
        }
         endTime = System.currentTimeMillis();
        System.err.println(endTime - startTime + " milliseconds");

    }
    class X {
        byte[] data;
        public X(byte[] data) {
            this.data =data;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }

        @Override
        public boolean equals(Object obj) {
            return Arrays.equals(data, ((X)obj).data);
        }
    }

    Random rnd = new Random();
    byte[] newMot(int n) {
        byte[] res = new byte[rnd.nextInt(n)+1];
        //System.out.println(res.length);
        rnd.nextBytes(res);
        return res;
    }
}
