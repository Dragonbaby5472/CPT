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
 * A concrete TestCaseGenerator that produces test paths achieving
 * edge coverage on the SUT graph.
 *
 * <p>This generator visits each edge in the directed graph exactly once.
 * For every uncovered edge, it builds a path that traverses that edge
 * by calling {@link #buildPathCoveringEdge(Graph, DefaultEdge)} and
 * then marks the edge as covered.</p>
 *
 * @param <V> the vertex type used in the SUT graph
 */
public class EdgeGenerator <V> extends TestCaseGenerator<V>{
	
	/**
     * Constructs an EdgeGenerator for the given System Under Test.
     *
     * @param sut the SUT model containing the directed graph and constraints
     */
    public EdgeGenerator(SUT<V> sut) { super(sut); }
    
	@Override
	public List<List<V>> generate() {
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
