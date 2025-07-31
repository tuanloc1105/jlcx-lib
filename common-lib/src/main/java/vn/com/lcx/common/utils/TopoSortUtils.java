package vn.com.lcx.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class TopoSortUtils {

    private TopoSortUtils() {
    }

    /**
     * Performs topological sort on a map of class names to their dependencies.
     *
     * @param classDependencies Map where key is class name and value is list of dependencies
     * @return List of class names in the order they should be instantiated
     * @throws IllegalArgumentException if there is a circular dependency
     */
    public static List<String> topologicalSort(Map<String, List<String>> classDependencies) {
        if (classDependencies == null || classDependencies.isEmpty()) {
            return new ArrayList<>();
        }

        // Check for circular dependencies first
        List<String> circularPath = findCircularDependency(classDependencies);
        if (circularPath != null) {
            StringBuilder errorMsg = new StringBuilder("Circular dependency detected: ");
            for (int i = 0; i < circularPath.size(); i++) {
                if (i > 0) {
                    errorMsg.append(" -> ");
                }
                errorMsg.append(circularPath.get(i));
            }
            errorMsg.append(" -> ").append(circularPath.get(0));
            throw new ExceptionInInitializerError(errorMsg.toString());
        }

        // Create adjacency list and in-degree count
        Map<String, List<String>> adjacencyList = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        // Initialize all classes with 0 in-degree
        for (String className : classDependencies.keySet()) {
            adjacencyList.put(className, new ArrayList<>());
            inDegree.put(className, 0);
        }

        // Build adjacency list and calculate in-degrees
        for (Map.Entry<String, List<String>> entry : classDependencies.entrySet()) {
            String className = entry.getKey();
            List<String> dependencies = entry.getValue();

            if (dependencies != null) {
                for (String dependency : dependencies) {
                    // Only consider dependencies that are in our input map
                    if (classDependencies.containsKey(dependency)) {
                        // Add edge from dependency to current class
                        adjacencyList.computeIfAbsent(dependency, k -> new ArrayList<>()).add(className);
                        // Increment in-degree of current class
                        inDegree.put(className, inDegree.getOrDefault(className, 0) + 1);
                    }
                }
            }
        }

        // Find all classes with 0 in-degree (no dependencies)
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<String> result = new ArrayList<>();
        // int processedCount = 0;

        // Process classes in topological order
        while (!queue.isEmpty()) {
            String currentClass = queue.poll();
            result.add(currentClass);
            // processedCount++;

            // Reduce in-degree of all dependent classes
            List<String> dependents = adjacencyList.get(currentClass);
            if (dependents != null) {
                for (String dependent : dependents) {
                    int newInDegree = inDegree.get(dependent) - 1;
                    inDegree.put(dependent, newInDegree);

                    // If in-degree becomes 0, add to queue
                    if (newInDegree == 0) {
                        queue.offer(dependent);
                    }
                }
            }
        }

        // Add any remaining classes that weren't processed (disconnected components)
        Set<String> processedClasses = new HashSet<>(result);
        for (String className : classDependencies.keySet()) {
            if (!processedClasses.contains(className)) {
                result.add(className);
            }
        }

        return result;
    }

    /**
     * Finds a circular dependency in the class dependencies map.
     *
     * @param classDependencies Map where key is class name and value is list of dependencies
     * @return List representing the circular path, or null if no circular dependency exists
     */
    public static List<String> findCircularDependency(Map<String, List<String>> classDependencies) {
        if (classDependencies == null || classDependencies.isEmpty()) {
            return null;
        }

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        Map<String, String> parent = new HashMap<>();

        for (String className : classDependencies.keySet()) {
            if (!visited.contains(className)) {
                List<String> cycle = dfsForCycle(className, classDependencies, visited, recursionStack, parent);
                if (cycle != null) {
                    return cycle;
                }
            }
        }

        return null;
    }

    /**
     * DFS helper method to detect cycles in the dependency graph.
     */
    private static List<String> dfsForCycle(String current, Map<String, List<String>> classDependencies, Set<String> visited, Set<String> recursionStack, Map<String, String> parent) {
        visited.add(current);
        recursionStack.add(current);

        List<String> dependencies = classDependencies.get(current);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                // Only consider dependencies that are in our input map
                if (classDependencies.containsKey(dependency)) {
                    if (!visited.contains(dependency)) {
                        parent.put(dependency, current);
                        List<String> cycle = dfsForCycle(dependency, classDependencies, visited, recursionStack, parent);
                        if (cycle != null) {
                            return cycle;
                        }
                    } else if (recursionStack.contains(dependency)) {
                        // Found a cycle, reconstruct the path
                        List<String> cycle = new ArrayList<>();
                        String node = current;
                        while (node != null && !node.equals(dependency)) {
                            cycle.add(0, node);
                            node = parent.get(node);
                        }
                        if (node != null) {
                            cycle.add(0, node);
                        }
                        return cycle;
                    }
                }
            }
        }

        recursionStack.remove(current);
        return null;
    }

    /**
     * Prints detailed information about circular dependencies.
     *
     * @param classDependencies Map where key is class name and value is list of dependencies
     */
    public static void printCircularDependencies(Map<String, List<String>> classDependencies) {
        List<String> circularPath = findCircularDependency(classDependencies);
        if (circularPath != null) {
            LogUtils.writeLog(LogUtils.Level.DEBUG, "Circular dependency detected:");
            LogUtils.writeLog(LogUtils.Level.DEBUG, "Path: " + String.join(" -> ", circularPath) + " -> " + circularPath.get(0));

            LogUtils.writeLog(LogUtils.Level.DEBUG, "Detailed dependency information:");
            for (int i = 0; i < circularPath.size(); i++) {
                String currentClass = circularPath.get(i);
                String nextClass = circularPath.get((i + 1) % circularPath.size());
                List<String> dependencies = classDependencies.get(currentClass);

                LogUtils.writeLog(LogUtils.Level.DEBUG, "  " + currentClass + " depends on: " + (dependencies != null ? dependencies.toString() : "[]"));
                LogUtils.writeLog(LogUtils.Level.DEBUG, "    -> Specifically depends on: " + nextClass);
            }
        } else {
            LogUtils.writeLog(LogUtils.Level.DEBUG, "No circular dependencies found.");
        }
    }

    /**
     * Performs topological sort with additional validation and error handling.
     *
     * @param classDependencies Map where key is class name and value is list of dependencies
     * @return List of class names in the order they should be instantiated
     * @throws IllegalArgumentException if there is a circular dependency or invalid input
     */
    public static List<String> topologicalSortWithValidation(Map<String, List<String>> classDependencies) {
        if (classDependencies == null) {
            throw new IllegalArgumentException("Class dependencies map cannot be null");
        }

        // Validate input
        for (Map.Entry<String, List<String>> entry : classDependencies.entrySet()) {
            String className = entry.getKey();
            List<String> dependencies = entry.getValue();

            if (className == null || className.trim().isEmpty()) {
                throw new IllegalArgumentException("Class name cannot be null or empty");
            }

            if (dependencies != null) {
                for (String dependency : dependencies) {
                    if (dependency == null || dependency.trim().isEmpty()) {
                        throw new IllegalArgumentException("Dependency name cannot be null or empty");
                    }
                }
            }
        }

        return topologicalSort(classDependencies);
    }

    /**
     * Performs topological sort and provides detailed information about the process.
     *
     * @param classDependencies Map where key is class name and value is list of dependencies
     * @return List of class names in the order they should be instantiated
     * @throws IllegalArgumentException if there is a circular dependency
     */
    public static List<String> topologicalSortWithDebug(Map<String, List<String>> classDependencies) {
        if (classDependencies == null || classDependencies.isEmpty()) {
            return new ArrayList<>();
        }

        LogUtils.writeLog(LogUtils.Level.DEBUG, "Input classes: " + classDependencies.keySet());
        LogUtils.writeLog(LogUtils.Level.DEBUG, "Total input classes: " + classDependencies.size());

        List<String> result = topologicalSort(classDependencies);

        LogUtils.writeLog(LogUtils.Level.DEBUG, "Output classes: " + result);
        LogUtils.writeLog(LogUtils.Level.DEBUG, "Total output classes: " + result.size());

        if (result.size() != classDependencies.size()) {
            Set<String> inputClasses = new HashSet<>(classDependencies.keySet());
            Set<String> outputClasses = new HashSet<>(result);
            Set<String> missingClasses = new HashSet<>(inputClasses);
            missingClasses.removeAll(outputClasses);
            LogUtils.writeLog(LogUtils.Level.DEBUG, "Missing classes: " + missingClasses);
        }

        return result;
    }

    /**
     * Analyzes dependencies and shows which external dependencies are being ignored.
     *
     * @param classDependencies Map where key is class name and value is list of dependencies
     */
    public static void analyzeDependencies(Map<String, List<String>> classDependencies) {
        if (classDependencies == null || classDependencies.isEmpty()) {
            return;
        }

        Set<String> allDependencies = new HashSet<>();
        Set<String> internalDependencies = new HashSet<>();
        Set<String> externalDependencies = new HashSet<>();

        for (Map.Entry<String, List<String>> entry : classDependencies.entrySet()) {
            List<String> dependencies = entry.getValue();
            if (dependencies != null) {
                for (String dependency : dependencies) {
                    allDependencies.add(dependency);
                    if (classDependencies.containsKey(dependency)) {
                        internalDependencies.add(dependency);
                    } else {
                        externalDependencies.add(dependency);
                    }
                }
            }
        }

        LogUtils.writeLog(LogUtils.Level.DEBUG, "Dependency Analysis:");
        LogUtils.writeLog(LogUtils.Level.DEBUG, "Total unique dependencies: " + allDependencies.size());
        LogUtils.writeLog(LogUtils.Level.DEBUG, "Internal dependencies (in input map): " + internalDependencies);
        LogUtils.writeLog(LogUtils.Level.DEBUG, "External dependencies (ignored): " + externalDependencies);
    }
}
