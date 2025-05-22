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
 * Implements the Filter‐based approach to constrained path‐based testing.
 *
 * <p>This generator first obtains a set of paths that achieve edge coverage
 * via {@link #egGenerate()}, then filters out any path that violates
 * the SUT’s constraints (NEGATIVE, ONCE, MAX_ONCE). Remaining paths are
 * guaranteed to cover all edges while respecting the defined constraints.</p>
 *
 * @param <V> the vertex type used in the underlying graph model
 */
public class FilterGenerator <V> extends TestCaseGenerator<V> {
	
    /**
     * Constructs a FilterGenerator for the specified System Under Test.
     *
     * @param sut the SUT model containing the directed graph and constraints
     */
    public FilterGenerator(SUT<V> sut) { 
    	super(sut); 
    }
    
	@Override
	public List<List<V>> generate() {
		List<List<V>> admissiblePaths = new ArrayList<>();
		List<Constraint<V>> C = sut.getConstraints();
		Set<Constraint<V>> coveredConstraints = new HashSet<>();
		List<List<V>> testPaths = egGenerate();
		for(List<V> path : testPaths) {
			if(isAdmissible(path, C, coveredConstraints)) {
				markConstraints(path, C, coveredConstraints);
				admissiblePaths.add(path);
			}
		}
		return admissiblePaths;
	}

	/**
     * Builds a path for each uncovered edge in the SUT graph to achieve edge coverage.
     *
     * <p>Iterates over all edges in {@code sut.getGraph().edgeSet()}, constructs
     * a path covering each uncovered edge using
     * {@link TestCaseGenerator#buildPathCoveringEdge(Graph, DefaultEdge)},
     * marks edges as covered via {@link #markEdges(List, Graph, Set)}, and
     * returns the full collection of paths.</p>
     *
     * @return a list of vertex sequences, each covering one or more formerly uncovered edges
     */
    public List<List<V>> egGenerate() {
		Set<DefaultEdge> coveredEdges = new HashSet<>();
		Graph<V, DefaultEdge> g = sut.getGraph();
		Set<DefaultEdge> edgeSet = g.edgeSet();
		List<List<V>> admissiblePaths = new ArrayList<>();
		for(DefaultEdge e: edgeSet) {
			if(coveredEdges.contains(e)) continue;
	        List<V> path = new ArrayList<>();
	        path = buildPathCoveringEdge(g, e);
	        admissiblePaths.add(path);
	        markEdges(path, g, coveredEdges);
		}
		return admissiblePaths;
	}

}
