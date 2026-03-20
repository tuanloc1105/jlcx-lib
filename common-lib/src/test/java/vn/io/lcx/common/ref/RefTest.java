package vn.io.lcx.common.ref;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RefTest {

    @Test
    void init_withValue_storesValue() {
        Ref<String> ref = Ref.init("hello");
        assertEquals("hello", ref.getVal());
    }

    @Test
    void init_noArgs_storesNull() {
        Ref<String> ref = Ref.init();
        assertNull(ref.getVal());
    }

    @Test
    void getVal_returnsStoredValue() {
        Ref<Integer> ref = Ref.init(42);
        assertEquals(42, ref.getVal());
    }

    @Test
    void setVal_updatesValue() {
        Ref<String> ref = Ref.init("initial");
        ref.setVal("updated");
        assertEquals("updated", ref.getVal());
    }

    @Test
    void setVal_toNull() {
        Ref<String> ref = Ref.init("not null");
        ref.setVal(null);
        assertNull(ref.getVal());
    }

    @Test
    void ref_usedInLambda_mutatesValue() {
        Ref<Integer> counter = Ref.init(0);

        // Simulate using Ref to mutate a value inside a lambda
        // (where a regular variable would need to be effectively final)
        Runnable increment = () -> counter.setVal(counter.getVal() + 1);

        increment.run();
        increment.run();
        increment.run();

        assertEquals(3, counter.getVal());
    }

    @Test
    void ref_withComplexType() {
        Ref<java.util.List<String>> ref = Ref.init(new java.util.ArrayList<>());
        ref.getVal().add("item1");
        ref.getVal().add("item2");

        assertEquals(2, ref.getVal().size());
        assertEquals("item1", ref.getVal().get(0));
    }

    @Test
    void ref_replaceValue_typeConsistency() {
        Ref<Number> ref = Ref.init(42);
        assertEquals(42, ref.getVal());

        ref.setVal(3.14);
        assertEquals(3.14, ref.getVal());
    }
}
