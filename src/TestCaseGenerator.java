/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
 */

package com.example.cpb_test;

import java.util.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
public abstract class TestCaseGenerator<V> {
    protected final SUT<V> sut;
    public TestCaseGenerator(SUT<V> sut) { this.sut = sut; }
    public abstract List<List<V>> generate();
    protected boolean containsConstraint(List<V> path, Constraint<V> c) {
    	boolean from = false, to = false;
        for (int i = 0; i < path.size(); i++) {
            if(path.get(i).equals(c.getFrom())) from = true;
            else if (path.get(i).equals(c.getTo()) && from) to = true;
        }
        return from && to;
    }
    protected boolean containsConstraintRepeatedly(List<V> path, Constraint<V> c) {
    	int from = 0, to = 0;
        for (int i = 0; i < path.size(); i++) {
            if(path.get(i).equals(c.getFrom())) from ++;
            else if (path.get(i).equals(c.getTo()) && from > to) to++;
        }
        return from > 1 && to > 1;
    }
    // 驗證 path 是否不違反任何 constraint，並考慮已 covered 的正向約束
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
    protected void markConstraints(List<V> path,
                                 List<Constraint<V>> C,
                                 Set<Constraint<V>> coveredConstraints) {
        for (Constraint<V> c : C) {
            if (containsConstraint(path, c)) {
                coveredConstraints.add(c);
            }
        }
    }
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
     * Algorithm 8 (Path_From_Source) 的優化實作：  
     * 往前遍歷所有 incoming 邊，直到到達 startVertex  
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
     * Algorithm 9 (Path_To_End) 的優化實作：  
     * 往後遍歷所有 outgoing 邊，直到到達某一 endVertex  
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
    /** 計算 edge e 在 path 中的出現次數 */
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