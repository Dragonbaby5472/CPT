/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
 */

package com.example.cpb_test;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class Metrics {
	
    /**
     * 計算 valid(T)：若所有約束都被滿足回傳 1，否則回傳未滿足的約束數（加負號）
     */
    public static <V> int valid(SUT<V> sut, List<List<V>> tests) {
        List<Constraint<V>> C = sut.getConstraints();
        // 統計每條 constraint 在 tests 中出現次數
        Map<Constraint<V>, Integer> occ = new HashMap<>();
        for (Constraint<V> c : C) occ.put(c, 0);
        for (List<V> path : tests) {
            for(Constraint<V> c : C) {
            	occ.merge(c, containsConstraintRepeatedly(path, c), Integer::sum);
            }
        }
        // 檢查哪些約束未被滿足
        int unsat = 0;
        for (Constraint<V> c : C) {
            int n = occ.get(c);
            switch (c.getType()) {
                case POSITIVE:
                    if (n < 1) unsat++;
                    break;
                case ONCE:
                    if (n != 1) unsat++;
                    break;
                case NEGATIVE:
                    if (n > 0) unsat++;
                    break;
                case MAX_ONCE:
                    if (n > 1) unsat++;
                    break;
            }
        }
        return unsat == 0 ? 1 : -unsat;
    }

    /** |T|：測試集路徑數 */
    public static <V> int size(List<List<V>> tests) {
        return tests.size();
    }

    /** l(T)：所有路徑邊數總和 */
    public static <V> int totalEdges(List<List<V>> tests) {
        return tests.stream()
                    .mapToInt(path -> path.size() - 1)
                    .sum();
    }

    /** u_edges(T)：去重後覆蓋的邊數 */
    public static <V> int uniqueEdges(SUT<V> sut, List<List<V>> tests) {
        Graph<V, DefaultEdge> g = sut.getGraph();
        Set<DefaultEdge> covered = new HashSet<>();
        for (List<V> path : tests) {
            for (int i = 0; i + 1 < path.size(); i++) {
                DefaultEdge e = g.getEdge(path.get(i), path.get(i+1));
                //System.out.println(e);
                if (e != null) covered.add(e);

            }
        }
        return covered.size();
    }

    /** avg(|t|)：平均路徑長度 = l(T) / |T| */
    public static <V> double averageLength(List<List<V>> tests) {
        if (tests.isEmpty()) return 0.0;
        return (double) totalEdges(tests) / tests.size();
    }

    /** s(T)：路徑長度標準差 */
    public static <V> double lengthStdDev(List<List<V>> tests) {
        int n = tests.size();
        if (n < 2) return -1;
        double avg = averageLength(tests);
        double sum2 = tests.stream()
            .mapToDouble(path -> Math.pow((path.size()-1) - avg, 2))
            .sum();
        return Math.sqrt(sum2 / (n - 1));
    }

    /** eff_edges(T)：邊效率 = u_edges(T) / l(T) */
    public static <V> double edgeEfficiency(SUT<V> sut, List<List<V>> tests) {
        int total = totalEdges(tests);
        return total == 0 ? 0.0 : (double) uniqueEdges(sut, tests) / total;
    }

    /** edge_cov(T)：邊覆蓋率 = u_edges(T) / |E| */
    public static <V> double edgeCoverage(SUT<V> sut, List<List<V>> tests) {
        int u = uniqueEdges(sut, tests);
        int all = sut.getGraph().edgeSet().size();
        return all == 0 ? 0.0 : (double) u / all;
    }

    /** cov_cp_positive(T)：POSITIVE 約束覆蓋率，若無此類約束回傳 -1 */
    public static <V> double covPositive(SUT<V> sut, List<List<V>> tests) {
        return covConstraintType(sut, tests, ConstraintType.POSITIVE,
            c -> totalOcc(tests, c) >= 1);
    }

    /** cov_cp_once(T)：ONCE 約束覆蓋率，若無此類約束回傳 -1 */
    public static <V> double covOnce(SUT<V> sut, List<List<V>> tests) {
        return covConstraintType(sut, tests, ConstraintType.ONCE,
            c -> totalOcc(tests, c) == 1);
    }

    /** cov_cp_negative(T)：NEGATIVE 約束滿足率，若無此類約束回傳 -1 */
    public static <V> double covNegative(SUT<V> sut, List<List<V>> tests) {
    	return covConstraintType(sut, tests, ConstraintType.NEGATIVE,
                c -> totalOcc(tests, c) >= 1);
    }
    /** cov_cp_only-once(T)：MAXONCE 約束覆蓋率，若無此類約束回傳 -1 */
    public static <V> double covMaxOnce(SUT<V> sut, List<List<V>> tests) {
        return covConstraintType(sut, tests, ConstraintType.MAX_ONCE,
            c -> totalOcc(tests, c) <= 1);
    }

    //constraint 檢查函式
	private static <V> boolean containsConstraint(List<V> path, Constraint<V> c) {
    	boolean from = false, to = false;
        for (int i = 0; i < path.size(); i++) {
            if(path.get(i).equals(c.getFrom())) from = true;
            else if (path.get(i).equals(c.getTo()) && from) to = true;
        }
        return from && to;
    }
    private static <V> int containsConstraintRepeatedly(List<V> path, Constraint<V> c) {
    	int from = 0, to = 0;
        for (int i = 0; i < path.size(); i++) {
            if(path.get(i).equals(c.getFrom())) from ++;
            else if (path.get(i).equals(c.getTo()) && from > to) to++;
        }
        return to;
    }
    /** 計算單一 constraint 在所有路徑中出現次數 */
    private static <V> int totalOcc(List<List<V>> tests, Constraint<V> c) {
        int cnt = 0;
        for (List<V> path : tests) {
           if(containsConstraint(path, c)) cnt++;
        }
        return cnt;
    }

    /**
     * 計算某類型 constraint 在 tests 中被 predicate 視為「滿足」的比例；
     * 若無該類型約束則回傳 -1
     */
    private static <V> double covConstraintType(SUT<V> sut,
        List<List<V>> tests,
        ConstraintType type,
        java.util.function.Predicate<Constraint<V>> sat) {

        List<Constraint<V>> list = sut.getConstraints().stream()
            .filter(c -> c.getType() == type)
            .collect(Collectors.toList());
        if (list.isEmpty()) return -1;
        long satCount = list.stream().filter(sat).count();
        return (double) satCount / list.size();
    }
}
