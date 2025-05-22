/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
 */

package com.example.cpb_test;
import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
public class EdgeGenerator <V> extends TestCaseGenerator<V>{
	
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
