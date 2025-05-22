/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
 */

package com.example.cpb_test;

import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public class FilterGenerator <V> extends TestCaseGenerator<V> {
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
