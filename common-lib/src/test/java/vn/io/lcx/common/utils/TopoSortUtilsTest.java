package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TopoSortUtilsTest {

    @Test
    void topologicalSort_validDAG_returnsOrder() {
        // A -> B -> C (A depends on nothing, B depends on A, C depends on B)
        Map<String, List<String>> deps = new HashMap<>();
        deps.put("A", new ArrayList<>());
        deps.put("B", List.of("A"));
        deps.put("C", List.of("B"));

        List<String> result = TopoSortUtils.topologicalSort(deps);

        assertNotNull(result);
        assertEquals(3, result.size());

        // A must come before B, B must come before C
        assertTrue(result.indexOf("A") < result.indexOf("B"),
                "A should appear before B in topological order");
        assertTrue(result.indexOf("B") < result.indexOf("C"),
                "B should appear before C in topological order");
    }

    @Test
    void topologicalSort_complexDAG_returnsValidOrder() {
        // D depends on B and C, B depends on A, C depends on A
        Map<String, List<String>> deps = new HashMap<>();
        deps.put("A", new ArrayList<>());
        deps.put("B", List.of("A"));
        deps.put("C", List.of("A"));
        deps.put("D", List.of("B", "C"));

        List<String> result = TopoSortUtils.topologicalSort(deps);

        assertEquals(4, result.size());
        assertTrue(result.indexOf("A") < result.indexOf("B"));
        assertTrue(result.indexOf("A") < result.indexOf("C"));
        assertTrue(result.indexOf("B") < result.indexOf("D"));
        assertTrue(result.indexOf("C") < result.indexOf("D"));
    }

    @Test
    void topologicalSort_singleNode() {
        Map<String, List<String>> deps = new HashMap<>();
        deps.put("OnlyNode", new ArrayList<>());

        List<String> result = TopoSortUtils.topologicalSort(deps);

        assertEquals(1, result.size());
        assertEquals("OnlyNode", result.get(0));
    }

    @Test
    void topologicalSort_emptyMap_returnsEmpty() {
        List<String> result = TopoSortUtils.topologicalSort(new HashMap<>());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void topologicalSort_nullMap_returnsEmpty() {
        List<String> result = TopoSortUtils.topologicalSort(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findCircularDependency_noCycle_returnsNull() {
        Map<String, List<String>> deps = new HashMap<>();
        deps.put("A", new ArrayList<>());
        deps.put("B", List.of("A"));
        deps.put("C", List.of("B"));

        List<String> cycle = TopoSortUtils.findCircularDependency(deps);
        assertNull(cycle, "No circular dependency should be detected");
    }

    @Test
    void findCircularDependency_withCycle_detectsCycle() {
        // A -> B -> C -> A (circular)
        Map<String, List<String>> deps = new HashMap<>();
        deps.put("A", List.of("C"));
        deps.put("B", List.of("A"));
        deps.put("C", List.of("B"));

        List<String> cycle = TopoSortUtils.findCircularDependency(deps);
        assertNotNull(cycle, "Circular dependency should be detected");
        assertFalse(cycle.isEmpty(), "Cycle path should not be empty");
    }

    @Test
    void topologicalSort_withCycle_throwsError() {
        Map<String, List<String>> deps = new HashMap<>();
        deps.put("A", List.of("B"));
        deps.put("B", List.of("A"));

        assertThrows(ExceptionInInitializerError.class,
                () -> TopoSortUtils.topologicalSort(deps));
    }
}
