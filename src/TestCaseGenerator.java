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
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Provides a generic framework for generating test cases (test paths) over a directed graph model 
 * of a System Under Test (SUT).
 *
 * <p>This abstract base class holds a reference to the {@code SUT} and defines the contract for
 * {@link #generate()}, which must produce a collection of test paths that collectively achieve
 * edge coverage while respecting any defined constraints. Subclasses implement {@code generate()}
 * and may leverage the supplied utility methods to:
 * <ul>
 *   <li>Check whether a path contains or repeats a given constraint.</li>
 *   <li>Determine if a candidate path is admissible under the current set of covered constraints.</li>
 *   <li>Mark edges and constraints covered by a path.</li>
 *   <li>Build or discover paths covering a specific edge, both forwards and backwards from that edge.</li>
 * </ul>
 *
 * @param <V> the vertex type used in the underlying graph model
 */
public abstract class TestCaseGenerator<V> {
    protected final SUT<V> sut;
    
    /**
     * Constructs a TestCaseGenerator for the given System Under Test.
     *
     * @param sut the SUT model against which test paths will be generated
     */
    public TestCaseGenerator(SUT<V> sut) { this.sut = sut; }
    
    /**
     * Generates a collection of test paths satisfying the defined constraints.
     *
     * @return a list of test paths, each represented as an ordered list of vertices
     */
    public abstract List<List<V>> generate();
    
    /**
     * Determines whether the specified constraint appears in the given path.
     * A constraint is satisfied if its 'from' vertex precedes its 'to' vertex at least once.
     *
     * @param path the sequence of vertices forming a candidate test path
     * @param c    the constraint pairing two vertices (from → to)
     * @return true if the path contains the constraint, false otherwise
     */
    protected boolean containsConstraint(List<V> path, Constraint<V> c) {
    	boolean from = false, to = false;
        for (int i = 0; i < path.size(); i++) {
            if(path.get(i).equals(c.getFrom())) from = true;
            else if (path.get(i).equals(c.getTo()) && from) to = true;
        }
        return from && to;
    }
    
    /**
     * Checks whether the given constraint appears more than once in the path.
     * Both the 'from' and 'to' vertices must each occur at least twice, in order.
     *
     * @param path the sequence of vertices forming a candidate test path
     * @param c    the constraint pairing two vertices (from → to)
     * @return true if the constraint appears repeatedly, false otherwise
     */
    protected boolean containsConstraintRepeatedly(List<V> path, Constraint<V> c) {
    	int from = 0, to = 0;
        for (int i = 0; i < path.size(); i++) {
            if(path.get(i).equals(c.getFrom())) from ++;
            else if (path.get(i).equals(c.getTo()) && from > to) to++;
        }
        return from > 1 && to > 1;
    }
    
    /**
     * Verifies that the candidate path does not violate any constraint in C,
     *
     * @param path    the sequence of vertices forming a candidate test path
     * @param C       the full list of constraints for the SUT
     * @param covered the set of constraints already covered by previous paths
     * @return true if the path is admissible, false if it violates any rule
     */
    protected boolean isAdmissible(List<V> path,
                                 List<Constraint<V>> C,
                                 Set<Constraint<V>> covered) {
		for(Constraint<V> c : C) {
			if(c.getType() == ConstraintType.NEGATIVE && containsConstraint(path, c)) {
				return false;
			}
			if(c.getType() == ConstraintType.ONCE || c.getType() == ConstraintType.MAX_ONCE) {
				if(containsConstraintRepeatedly(path, c)) {
					return false;
					}
				if(containsConstraint(path, c) && covered.contains(c)) {
					return false;
				}
			}
		}
    	return true;
        
    }
    
    /**
     * Marks all constraints from C that are covered by the given path,
     * adding them to the coveredConstraints set.
     *
     * @param path               the sequence of vertices forming a test path
     * @param C                  the full list of constraints for the SUT
     * @param coveredConstraints the set to which newly covered constraints will be added
     */
    protected void markConstraints(List<V> path,
                                 List<Constraint<V>> C,
                                 Set<Constraint<V>> coveredConstraints) {
        for (Constraint<V> c : C) {
            if (containsConstraint(path, c)) {
                coveredConstraints.add(c);
            }
        }
    }
    
    /**
     * Adds all edges traversed in the given path to the coveredEdges set.
     *
     * @param path         the sequence of vertices forming a test path
     * @param g            the graph model of the SUT
     * @param coveredEdges the set to which covered edges will be added
     */
    protected void markEdges(List<V> path,
                           Graph<V, DefaultEdge> g,
                           Set<DefaultEdge> coveredEdges) {
        for (int i = 0; i + 1 < path.size(); i++) {
            V u = path.get(i);
            V v = path.get(i + 1);
            DefaultEdge e = g.getEdge(u, v);
            if (e != null) {
                coveredEdges.add(e);
            }
        }
    }
    
    /**
     * Builds a complete path that covers the specified edge by concatenating
     * a path leading to its source and a path from its target.
     *
     * @param g   the graph model of the SUT
     * @param eIn the edge to be covered by the resulting path
     * @return a list of vertices forming the combined path, or null if invalid
     */
    protected List<V> buildPathCoveringEdge(Graph<V, DefaultEdge> g,
                                          DefaultEdge eIn) {
    	
        List<V> Ps = findPathsToEdge(eIn, g);
        List<V> Pe = findPathsFromEdge(eIn, g);
        List<V> path = new ArrayList<>();
        
        if (Ps != null) {
        	path.addAll(Ps);
        }
        if (Pe != null) {
        	path.addAll(Pe);
        }
        if(path.isEmpty()) return path;
        
        
        if(path.getFirst() != sut.getStartVertex() || !sut.getEndVertices().contains(path.getLast())) {
        	return null;
        }
        return path;
    }

    /**
     * Finds a shortest path from the SUT start vertex to the source of the given edge
     * using a breadth‐first search over incoming edges.
     *
     * @param e the edge whose source vertex must be reached
     * @param g the graph model of the SUT
     * @return a list of vertices from start to the edge source, or null if no path exists
     */
    protected List<V> findPathsToEdge(DefaultEdge e,
                                          Graph<V, DefaultEdge> g) {
        V start = sut.getStartVertex();
        V end = g.getEdgeSource(e);
        if(start == end) {
        	List<V> path = new ArrayList<>();
            path.addFirst(end);
            return path;
        }
        Queue<List<V>> queue = new ArrayDeque<>();
            
        for (DefaultEdge eIn : g.incomingEdgesOf(end)) {
            V next = g.getEdgeSource(eIn);
            List<V> path = new ArrayList<>();
            path.addFirst(end);
            path.addFirst(next);
            queue.add(path);
            
        }
        while (!queue.isEmpty()) {
        	List<V> path = new ArrayList<>();
            path = queue.poll();
            V first = path.getFirst();
            if (first == start) {
                return path;
            }
            for (DefaultEdge eIn : g.incomingEdgesOf(first)) {
                V nxt = g.getEdgeSource(eIn);
                if (nxt == start) {
                	path.addFirst(nxt);
                    return path;
                }
                if (!containEdge(path, eIn, g)) {
                    List<V> newPath = new ArrayList<>();
                    newPath.addAll(path);
                    newPath.addFirst(nxt);
                    queue.add(newPath);
                }
            }
        }
        return null;
    }

    /**
     * Finds a shortest path from the target of the given edge to any end vertex
     * using a breadth‐first search over outgoing edges.
     *
     * @param e the edge whose target vertex is the path start
     * @param g the graph model of the SUT
     * @return a list of vertices from the edge target to an end vertex, or null if none exists
     */
    protected List<V> findPathsFromEdge(DefaultEdge e,
                                          Graph<V, DefaultEdge> g) {
        Set<V> ends = sut.getEndVertices();
        V start = g.getEdgeTarget(e);
        if(ends.contains(start)) {
        	List<V> path = new ArrayList<>();
            path.addFirst(start);
            return path;
        }
        Queue<List<V>> queue = new ArrayDeque<>();
            
        for (DefaultEdge eOut : g.outgoingEdgesOf(start)) {
            V next = g.getEdgeTarget(eOut);
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
                return path;
            }
            for (DefaultEdge eOut : g.outgoingEdgesOf(last)) {
                V nxt = g.getEdgeTarget(eOut);
                if (ends.contains(nxt)) {
                	path.add(nxt);
                    return path;
                }
                if (!containEdge(path, eOut, g)) {
                    List<V> newPath = new ArrayList<>();
                    newPath.addAll(path);
                    newPath.add(nxt);
                    queue.add(newPath);
                }
            }
        }
        return null;
    }
    
    /**
     * Checks whether the specified edge appears in the given path.
     *
     * @param path the sequence of vertices forming a test path
     * @param e    the graph edge to look for
     * @param g    the graph model of the SUT
     * @return true if the edge is present in the path, false otherwise
     */
    protected boolean containEdge(List<V> path,
                                     DefaultEdge e,
                                     Graph<V, DefaultEdge> g) {
        V u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
        for (int i = 0; i + 1 < path.size(); i++) {
            if (path.get(i).equals(u) && path.get(i + 1).equals(v)) {
                return true;
            }
        }
        return false;
    }
}