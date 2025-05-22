/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
 */

package com.example.cpb_test;

import java.util.*;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public class CPCGenerator<V> extends TestCaseGenerator<V> {
    public CPCGenerator(SUT<V> sut) { super(sut); }

    @Override
    public List<List<V>> generate() {
    	Graph<V, DefaultEdge> g = sut.getGraph();
        List<Constraint<V>> C = sut.getConstraints();

        List<List<V>> admissiblePaths = new ArrayList<>();
        List<List<V>> coveragePaths   = new ArrayList<>();
        Set<Constraint<V>> coveredConstraints = new HashSet<>();
        Set<DefaultEdge> coveredEdges = new HashSet<>();

        // 1. 處理 POSITIVE / ONCE：找到 admissible 路徑並加入兩組列表
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
 
        // 2. 補齊邊覆蓋：對每條尚未覆蓋的邊，找最短路徑並確認其是否符合約束
        for (DefaultEdge e : g.edgeSet()) {
            if (!coveredEdges.contains(e)) {
                //V u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
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


    private static final int VISITSLIMIT = 2;

    private List<V> findAdmissiblePath(Graph<V, DefaultEdge> g,
                                       Constraint<V> target,
                                       Set<Constraint<V>> covered,
                                       List<Constraint<V>> C) {
        V start = sut.getStartVertex();
        Set<V> ends = sut.getEndVertices();
        
        // 依次嘗試每種邊重用上限 i = 1..VISITSLIMIT
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

    /** 計算 edge e 在 path 中的出現次數 */
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
