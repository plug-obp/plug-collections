package bdd;

import datastructures.bdd.v0.BDD;
import datastructures.bdd.v0.TerminalNode;
import datastructures.bdd.v0.ToTGF;
import org.junit.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;

import static org.junit.Assert.assertEquals;

public class TestBDD {
    BDD<Character> factory = new BDD<>();

    @Test
    public void testTerminals() {
        assertEquals(factory.constant(false), factory.bot());
        assertEquals(factory.constant(true), factory.top());
    }
    @Test
    public void testTGF() {
        var f = new BDD<Character>();
        f.variableOrder = Comparator.naturalOrder();
        var bdd = f.and(f.var('b'), f.var('a'));

        var to = new ToTGF<Boolean, Character>();
        var tgf = bdd.accept(to, new HashMap<>());
        System.out.println(tgf);
    }

    @Test
    public void testFo() {
        //(x1∧x2∧x2 )∨(x1∧¬x3)
        var f = new BDD<String>();
        f.variableOrder = Comparator.reverseOrder();
        var bdd = f.or(
                f.and(f.var("x1"), f.and(f.var("x2"), f.var("x2"))),
                f.and(f.var("x1"), f.rav("x3"))
        );

        var to = new ToTGF<Boolean, String>();
        var tgf = bdd.accept(to, new HashMap<>());
        System.out.println(tgf);
    }
}
