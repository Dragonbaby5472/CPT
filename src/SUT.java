/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
 */

package com.example.cpb_test;

import java.util.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public class SUT<V> {
    private final Graph<V, DefaultEdge> graph;
    private final List<Constraint<V>> constraints;
    private V startVertex;
    private final Set<V> endVertices;

    public SUT() {
        this.graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        this.constraints = new ArrayList<>();
        this.endVertices = new HashSet<>();
    }

    public void addVertex(V v) {
        graph.addVertex(v);
    }

    public void setStartVertex(V v) {
        addVertex(v);
        this.startVertex = v;
    }
    public V getStartVertex() {
        return startVertex;
    }

    public void addEndVertex(V v) {
        addVertex(v);
        this.endVertices.add(v);
    }
    public Set<V> getEndVertices() {
        return Collections.unmodifiableSet(endVertices);
    }

    public void addEdge(V from, V to) {
        graph.addEdge(from, to);
    }

    public void addConstraint(Constraint<V> c) {
        constraints.add(c);
    }

    public Graph<V, DefaultEdge> getGraph() {
        return graph;
    }

    public List<Constraint<V>> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

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
