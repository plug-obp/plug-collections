package adfa.test;

import datastructures.byteset.ByteArrayMap;
import datastructures.byteset.ByteArraySet;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TestByteArraySet {

    @Test
    public void test2() {
        byte[][] elements = new byte[][]{
                {-109}, {24}, {3}, {3}, {127}
        };

        int duplicates = 0;
        ByteArraySet bas = new ByteArraySet();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < elements.length; i++) {
            if (bas.add(elements[i]) != null) {
                duplicates++;
            }
        }
        long endTime = System.currentTimeMillis();
        System.err.println("ByteArraySet: " + (endTime - startTime) + " milliseconds " + duplicates + " duplicated, size: " + bas.size());

        duplicates = 0;
        Map<X, X> map = new HashMap<>();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < elements.length; i++) {
            X x = new X(elements[i]);
            if (map.put(x, x) != null) {
                duplicates++;
            }
        }
        endTime = System.currentTimeMillis();
        System.err.println("HashMap: " + (endTime - startTime) + " milliseconds " + duplicates + " duplicated, size: " + map.size());
    }

    @Test
    public void test1() {
        int nbElements = 100000;
        int elementSize = 1000;

        byte[][] elements = new byte[nbElements][];
        for (int i = 0; i < nbElements; i++) {
            elements[i] = newMot(elementSize);
        }

        int duplicates = 0;
        ByteArraySet bas = new ByteArraySet();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < nbElements; i++) {
            if (bas.add(elements[i]) != null) {
                duplicates++;
            }
        }
        long endTime = System.currentTimeMillis();
        System.err.println("ByteArraySet: " + (endTime - startTime) + " milliseconds " + duplicates + " duplicated, size: " + bas.size());
        int bass = bas.size();
        bas = null;

        duplicates = 0;
        ByteArrayMap<X> bam = new ByteArrayMap<>();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < nbElements; i++) {
            if (bam.put(elements[i], new X(elements[i])) != null) {
                duplicates++;
            }
        }
        endTime = System.currentTimeMillis();
        System.err.println("ByteArrayMap: " + (endTime - startTime) + " milliseconds " + duplicates + " duplicated, size: " + bam.size());
        int bams = bam.size();
        bam = null;

        duplicates = 0;
        Map<X, X> map = new HashMap<>();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < nbElements; i++) {
            X x = new X(elements[i]);
            if (map.put(x, x) != null) {
                duplicates++;
            }
        }
        endTime = System.currentTimeMillis();
        System.err.println("HashMap: " + (endTime - startTime) + " milliseconds " + duplicates + " duplicated, size: " + map.size());
        int maps = map.size();
        map = null;

        if (maps != bams) {
            for (int i = 0; i < nbElements; i++) {
                System.err.println(Arrays.toString(elements[i]));
            }
        }
    }

    Random rnd = new Random();
    byte[] newMot(int n) {
        byte[] res = new byte[n];
        rnd.nextBytes(res);
        return res;
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
}
