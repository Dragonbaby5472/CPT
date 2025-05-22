/*
 * Copyright 2025 Neo Tsai
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.cpb_test;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class offering a suite of static methods to compute metrics
 * over a set of test paths generated for a System Under Test (SUT).
 *
 * <p>Metrics include test validity against constraints, path counts,
 * total and unique edge coverage, path length statistics, and constraint
 * coverage ratios.</p>
 */
public class Metrics {
	
	/**
     * Evaluates whether all constraints of the SUT are satisfied by the test set.
     *
     * @param sut   the System Under Test model containing graph and constraints
     * @param tests the list of test paths, each path represented as a list of vertices
     * @return  1 if every constraint is met; otherwise returns the negative count
     *          of unsatisfied constraints
     */
    public static <V> int valid(SUT<V> sut, List<List<V>> tests) {
        List<Constraint<V>> C = sut.getConstraints();
        Map<Constraint<V>, Integer> occ = new HashMap<>();
        for (Constraint<V> c : C) occ.put(c, 0);
        for (List<V> path : tests) {
            for(Constraint<V> c : C) {
            	occ.merge(c, containsConstraintRepeatedly(path, c), Integer::sum);
            }
        }
        int unsat = 0;
        for (Constraint<V> c : C) {
            int n = occ.get(c);
            switch (c.getType()) {
                case POSITIVE:
                    if (n < 1) unsat++;
                    break;
                case ONCE:
                    if (n != 1) unsat++;
                    break;
                case NEGATIVE:
                    if (n > 0) unsat++;
                    break;
                case MAX_ONCE:
                    if (n > 1) unsat++;
                    break;
            }
        }
        return unsat == 0 ? 1 : -unsat;
    }

    /**
     * Returns the number of test paths in the given test set.
     *
     * @param tests the list of test paths
     * @return the total count of paths
     */
    public static <V> int size(List<List<V>> tests) {
        return tests.size();
    }

    /**
     * Computes the total number of edge traversals across all test paths.
     *
     * @param tests the list of test paths
     * @return the sum of (path length − 1) for each test path
     */
    public static <V> int totalEdges(List<List<V>> tests) {
        return tests.stream()
                    .mapToInt(path -> path.size() - 1)
                    .sum();
    }

    /**
     * Counts the number of unique edges covered by the test set.
     *
     * @param sut   the System Under Test model containing the graph
     * @param tests the list of test paths
     * @return the number of distinct edges traversed at least once
     */
    public static <V> int uniqueEdges(SUT<V> sut, List<List<V>> tests) {
        Graph<V, DefaultEdge> g = sut.getGraph();
        Set<DefaultEdge> covered = new HashSet<>();
        for (List<V> path : tests) {
            for (int i = 0; i + 1 < path.size(); i++) {
                DefaultEdge e = g.getEdge(path.get(i), path.get(i+1));
                //System.out.println(e);
                if (e != null) covered.add(e);

            }
        }
        return covered.size();
    }

    /**
     * Calculates the average path length (in edges) of the test set.
     *
     * @param tests the list of test paths
     * @return  the average of (path length − 1) across all paths,
     *          or 0.0 if the list is empty
     */
    public static <V> double averageLength(List<List<V>> tests) {
        if (tests.isEmpty()) return 0.0;
        return (double) totalEdges(tests) / tests.size();
    }

    /**
     * Computes the standard deviation of path lengths (in edges).
     *
     * @param tests the list of test paths
     * @return  the sample standard deviation of (path length − 1),
     *          or -1 if fewer than two paths are provided
     */
    public static <V> double lengthStdDev(List<List<V>> tests) {
        int n = tests.size();
        if (n < 2) return -1;
        double avg = averageLength(tests);
        double sum2 = tests.stream()
            .mapToDouble(path -> Math.pow((path.size()-1) - avg, 2))
            .sum();
        return Math.sqrt(sum2 / (n - 1));
    }

    /**
     * Computes edge efficiency as the ratio of unique edges to total edges.
     *
     * @param sut   the System Under Test model containing the graph
     * @param tests the list of test paths
     * @return  uniqueEdges(sut, tests) divided by totalEdges(tests),
     *          or 0.0 if no edges are traversed
     */
    public static <V> double edgeEfficiency(SUT<V> sut, List<List<V>> tests) {
        int total = totalEdges(tests);
        return total == 0 ? 0.0 : (double) uniqueEdges(sut, tests) / total;
    }

    /**
     * Computes edge coverage as the fraction of graph edges covered.
     *
     * @param sut   the System Under Test model containing the graph
     * @param tests the list of test paths
     * @return  uniqueEdges(sut, tests) divided by the graph's edge count,
     *          or 0.0 if the graph has no edges
     */
    public static <V> double edgeCoverage(SUT<V> sut, List<List<V>> tests) {
        int u = uniqueEdges(sut, tests);
        int all = sut.getGraph().edgeSet().size();
        return all == 0 ? 0.0 : (double) u / all;
    }

    /**
     * Computes the proportion of POSITIVE constraints satisfied.
     *
     * @param sut   the System Under Test model containing constraints
     * @param tests the list of test paths
     * @return  the ratio of POSITIVE constraints with at least one occurrence,
     *          or -1.0 if no POSITIVE constraints are defined
     */
    public static <V> double covPositive(SUT<V> sut, List<List<V>> tests) {
        return covConstraintType(sut, tests, ConstraintType.POSITIVE,
            c -> totalOcc(tests, c) >= 1);
    }

    /**
     * Computes the proportion of ONCE constraints satisfied exactly once.
     *
     * @param sut   the System Under Test model containing constraints
     * @param tests the list of test paths
     * @return  the ratio of ONCE constraints with exactly one occurrence,
     *          or -1.0 if no ONCE constraints are defined
     */
    public static <V> double covOnce(SUT<V> sut, List<List<V>> tests) {
        return covConstraintType(sut, tests, ConstraintType.ONCE,
            c -> totalOcc(tests, c) == 1);
    }

    /**
     * Computes the proportion of NEGATIVE constraints that hold (zero occurrences).
     *
     * @param sut   the System Under Test model containing constraints
     * @param tests the list of test paths
     * @return  the ratio of NEGATIVE constraints with no occurrences,
     *          or -1.0 if no NEGATIVE constraints are defined
     */
    public static <V> double covNegative(SUT<V> sut, List<List<V>> tests) {
    	return covConstraintType(sut, tests, ConstraintType.NEGATIVE,
                c -> totalOcc(tests, c) >= 1);
    }
    
    /**
     * Computes the proportion of MAX_ONCE constraints not exceeded.
     *
     * @param sut   the System Under Test model containing constraints
     * @param tests the list of test paths
     * @return  the ratio of MAX_ONCE constraints with at most one occurrence,
     *          or -1.0 if no MAX_ONCE constraints are defined
     */
    public static <V> double covMaxOnce(SUT<V> sut, List<List<V>> tests) {
        return covConstraintType(sut, tests, ConstraintType.MAX_ONCE,
            c -> totalOcc(tests, c) <= 1);
    }

    /**
     * Checks if the given path contains the specified constraint sequence.
     *
     * @param path       a single test path as a sequence of vertices
     * @param constraint the constraint pairing two vertices
     * @return the number of times the 'from'→'to' sequence occurs in order
     */
	private static <V> boolean containsConstraint(List<V> path, Constraint<V> c) {
    	boolean from = false, to = false;
        for (int i = 0; i < path.size(); i++) {
            if(path.get(i).equals(c.getFrom())) from = true;
            else if (path.get(i).equals(c.getTo()) && from) to = true;
        }
        return from && to;
    }
    private static <V> int containsConstraintRepeatedly(List<V> path, Constraint<V> c) {
    	int from = 0, to = 0;
        for (int i = 0; i < path.size(); i++) {
            if(path.get(i).equals(c.getFrom())) from ++;
            else if (path.get(i).equals(c.getTo()) && from > to) to++;
        }
        return to;
    }
    
    /**
     * Counts total occurrences of a constraint across all test paths.
     *
     * @param tests      the list of test paths
     * @param constraint the constraint pairing two vertices
     * @return the sum of occurrences in each path
     */
    private static <V> int totalOcc(List<List<V>> tests, Constraint<V> c) {
        int cnt = 0;
        for (List<V> path : tests) {
           if(containsConstraint(path, c)) cnt++;
        }
        return cnt;
    }

    /**
     * Computes the coverage ratio for constraints of a specific type.
     *
     * @param sut     the System Under Test model containing constraints
     * @param tests   the list of test paths
     * @param type    the constraint type to evaluate
     * @param checker a predicate that returns true if a given constraint is satisfied
     * @return the fraction of constraints of the given type that satisfy the predicate,
     *         or -1.0 if no constraints of that type exist
     */
    private static <V> double covConstraintType(SUT<V> sut,
        List<List<V>> tests,
        ConstraintType type,
        java.util.function.Predicate<Constraint<V>> sat) {

        List<Constraint<V>> list = sut.getConstraints().stream()
            .filter(c -> c.getType() == type)
            .collect(Collectors.toList());
        if (list.isEmpty()) return -1;
        long satCount = list.stream().filter(sat).count();
        return (double) satCount / list.size();
    }
}
