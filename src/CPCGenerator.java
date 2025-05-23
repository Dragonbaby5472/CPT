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

import java.util.*;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Implements the Constrained Path-based Testing Composition (CPC) algorithm
 * for generating test paths over the given System Under Test (SUT) graph.
 *
 * <p>This generator first covers all POSITIVE and ONCE constraints by finding
 * admissible paths, then completes edge coverage for the remaining edges,
 * always ensuring that no NEGATIVE or repeated constraints are violated.</p>
 *
 * @param <V> the vertex type used in the SUT graph model
 */
public class CPCGenerator<V> extends TestCaseGenerator<V> {
	
	/**
     * Constructs a new CPCGenerator for the specified SUT model.
     *
     * @param sut the System Under Test model containing the graph and constraints
     */
    public CPCGenerator(SUT<V> sut) { super(sut); }

    @Override
    public List<List<V>> generate() {
    	Graph<V, DefaultEdge> g = sut.getGraph();
        List<Constraint<V>> C = sut.getConstraints();

        List<List<V>> admissiblePaths = new ArrayList<>();
        List<List<V>> coveragePaths   = new ArrayList<>();
        Set<Constraint<V>> coveredConstraints = new HashSet<>();
        Set<DefaultEdge> coveredEdges = new HashSet<>();

        // Phase 1: cover POSITIVE and ONCE constraints
        for (Constraint<V> c : C) {
            if (c.getType() == ConstraintType.POSITIVE ||
                c.getType() == ConstraintType.ONCE) {
            	if(coveredConstraints.contains(c)) continue;
            	
                List<V> path = findAdmissiblePath(g, c, coveredConstraints, C);
                if (path != null && !admissiblePaths.contains(path)) {
                    admissiblePaths.add(path);
                    coveragePaths.add(path);
                    markEdges(path, g, coveredEdges);
                    markConstraints(path, C, coveredConstraints);
                }
            }
        }
 
        // Phase 2: complete edge coverage
        for (DefaultEdge e : g.edgeSet()) {
            if (!coveredEdges.contains(e)) {
                List<V> path = buildPathCoveringEdge(g, e);
                if (path != null && !coveragePaths.contains(path)) {
                    if (isAdmissible(path, C, coveredConstraints)) {
                        coveragePaths.add(path);
                        markEdges(path, g, coveredEdges);
                        admissiblePaths.add(path);
                        markConstraints(path, C, coveredConstraints);
                    }
                }
            }
        }
        return admissiblePaths;
    }

    /**
     * Maximum number of times a single edge may be reused when searching for
     * an admissible path.
     */
    private static final int VISITSLIMIT = 2;

    /**
     * Performs a breadth-first search to find a path from the SUT start vertex
     * that satisfies the given target constraint without violating any negative
     *  constraints. It gradually increases the allowed reuse limit for edges.
     *
     * @param g               the directed graph model of the SUT
     * @param target          the constraint to be satisfied by the returned path
     * @param covered         the set of constraints already covered by previous paths
     * @param allConstraints  the full list of constraints defined on the SUT
     * @return a list of vertices forming an admissible path that satisfies {@code target},
     *         or {@code null} if no such path exists within the visit limit
     */
    private List<V> findAdmissiblePath(Graph<V, DefaultEdge> g,
                                       Constraint<V> target,
                                       Set<Constraint<V>> covered,
                                       List<Constraint<V>> C) {
        V start = sut.getStartVertex();
        Set<V> ends = sut.getEndVertices();
        
        for (int limit = 1; limit <= VISITSLIMIT; limit++) {
            Queue<List<V>> queue = new ArrayDeque<>();
            
            for (DefaultEdge e : g.outgoingEdgesOf(start)) {
                V next = g.getEdgeTarget(e);
                List<V> path = new ArrayList<>();
                path.add(start);
                path.add(next);
                queue.add(path);
            }
            while (!queue.isEmpty()) {
            	List<V> path = new ArrayList<>();
                path = queue.poll();
                V last = path.getLast();

                if (ends.contains(last)) {
                    if (containsConstraint(path, target))return path;
                    else continue;
                }

                for (DefaultEdge e : g.outgoingEdgesOf(last)) {
                    V nxt = g.getEdgeTarget(e);
                    if (countEdgeOccurrences(path, e, g) < limit) {
                        List<V> newPath = new ArrayList<>();
                        newPath.addAll(path);
                        newPath.add(nxt);
                        if (isAdmissible(newPath, C, covered)) {
                            queue.add(newPath);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Counts how many times the specified edge appears consecutively in the given path.
     *
     * @param path the sequence of vertices representing a candidate test path
     * @param e    the graph edge whose occurrences are to be counted
     * @param g    the directed graph model to resolve edge endpoints
     * @return the number of occurrences of {@code e} in {@code path}
     */
    private int countEdgeOccurrences(List<V> path,
                                     DefaultEdge e,
                                     Graph<V, DefaultEdge> g) {
        int count = 0;
        V u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
        for (int i = 0; i + 1 < path.size(); i++) {
            if (path.get(i).equals(u) && path.get(i + 1).equals(v)) {
                count++;
            }
        }
        return count;
    }
}
