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
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Represents the System Under Test (SUT) as a directed graph model with
 * vertices, edges, a designated start vertex, and end vertices. Also Contained
 * constraint set for generating test paths for this SUT.
 *
 * <p>This generic class encapsulates a JGraphT {@code Graph<V, DefaultEdge>},
 * maintains a list of {@link Constraint<V>} objects, and tracks one start vertex
 * plus a set of end vertices. It provides methods to add and query vertices,
 * edges, constraints, and to retrieve immutable views of its internal structures.</p>
 *
 * @param <V> the vertex type used in the graph model
 */
public class SUT<V> {
    private final Graph<V, DefaultEdge> graph;
    private final List<Constraint<V>> constraints;
    private V startVertex;
    private final Set<V> endVertices;

    /**
     * Initializes a new SUT instance backed by a {@link SimpleDirectedGraph},
     * with empty constraint list and end-vertex set.
     */
    public SUT() {
        this.graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        this.constraints = new ArrayList<>();
        this.endVertices = new HashSet<>();
    }

    /**
     * Adds the given vertex to the graph if not already present. 
     *
     * @param v the vertex to add
     */
    public void addVertex(V v) {
        graph.addVertex(v);
    }

    /**
     * Sets the designated start vertex for test path generation, adding it
     * to the graph if necessary. 
     *
     * @param v the vertex to designate as start
     */
    public void setStartVertex(V v) {
        addVertex(v);
        this.startVertex = v;
    }
    
    /**
     * Returns the current start vertex.
     *
     * @return the vertex marked as the start of all test paths
     */
    public V getStartVertex() {
        return startVertex;
    }

    /**
     * Adds the given vertex to the set of end vertices, also ensuring
     * it exists in the graph.
     *
     * @param v the vertex to mark as an end point
     */
    public void addEndVertex(V v) {
        addVertex(v);
        this.endVertices.add(v);
    }
    
    /**
     * Returns an unmodifiable view of the end-vertex set.
     *
     * @return the set of vertices designated as valid end points
     */
    public Set<V> getEndVertices() {
        return Collections.unmodifiableSet(endVertices);
    }

    /**
     * Adds a directed edge from {@code from} to {@code to} in the graph.
     * Both vertices must already exist or will be added implicitly.
     *
     * @param from the source vertex
     * @param to   the target vertex
     */
    public void addEdge(V from, V to) {
        graph.addEdge(from, to);
    }

    /**
     * Registers a new vertex-pair {@link Constraint} to be enforced during
     * test path generation.
     *
     * @param c the constraint object pairing two vertices
     */
    public void addConstraint(Constraint<V> c) {
        constraints.add(c);
    }

    /**
     * Returns the underlying directed graph model.
     *
     * @return the JGraphT {@code Graph<V, DefaultEdge>} instance
     */
    public Graph<V, DefaultEdge> getGraph() {
        return graph;
    }

    /**
     * Returns an unmodifiable list of all registered constraints.
     *
     * @return the list of {@link Constraint<V>} objects
     */
    public List<Constraint<V>> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    /**
     * Returns a string summary of the SUT, including counts of vertices,
     * edges, constraints, and the current start and end vertices.
     *
     * @return a formatted string describing the SUT state
     */
    @Override
    public String toString() {
        return String.format(
            "SUT{vertices=%d, edges=%d, constraints=%s, start=%s, ends=%s}",
            graph.vertexSet().size(),
            graph.edgeSet().size(),
            constraints,
            startVertex,
            endVertices
        );
    }
}
