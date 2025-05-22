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
