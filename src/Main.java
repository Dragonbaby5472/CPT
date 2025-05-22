/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
 */

package com.example.cpb_test;

import java.io.*;
import java.util.*;

public class Main {
	
	private static boolean showPath = false;
	private static boolean saveCSV = false;
	private static String csvPath = "./result.csv";
	private static PrintWriter pw;
	
	@SuppressWarnings("resource")
	private static void run(String[] args)throws InterruptedException, IOException {
		boolean isFile = true;
		boolean createLog = false;
		boolean createDot = false;
		boolean createPng = false;
		String filePath = "./msa_example.txt";
		String logFile = "./result.log";
		String dotFile = "./temp.dot";
		String pngFile = "./sut.png";
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("-file")) {
				isFile = true;
				filePath = args[++i];
			}
			else if(args[i].equals("-dir")) {
				isFile = false;
				filePath = args[++i];
			}
			else if(args[i].equals("-log")) {
				createLog = true;
				logFile = args[++i];
			}
			else if(args[i].equals("-showpath")) {
				showPath = true;
			}
			else if(args[i].equals("-topng")) {
				createDot = true;
				createPng = true;
				pngFile = args[++i];
			}
			else if(args[i].equals("-todot")) {
				createDot = true;
				dotFile = args[++i];
			}
			else if(args[i].equals("-csv")) {
				saveCSV = true;
				csvPath = args[++i];
			}
		}
		FileOutputStream fos = new FileOutputStream(logFile);
		MultiOutputStream mos = new MultiOutputStream(System.out, fos);
		PrintStream ps = new PrintStream(mos, true);
		if(createLog) {
			System.setOut(ps);
			System.setErr(ps);
		}
		if(saveCSV) {
			pw = new PrintWriter(new FileWriter(csvPath));
		}

		if(isFile){
			SUT<String> sut = SUTLoader.loadFromTxt(filePath);
			System.out.println("===== SUT Info =====");
            System.out.println(sut);
            System.out.println();

            TestCaseGenerator<String> cpcGen = new CPCGenerator<>(sut);
            TestCaseGenerator<String> filterGen = new FilterGenerator<>(sut);
            TestCaseGenerator<String> edgeGen = new EdgeGenerator<>(sut);
            System.out.println("===== CPC Result =====");
            singleTest(sut, cpcGen);
            System.out.println("===== Filter Result =====");
            singleTest(sut, filterGen);
            System.out.println("===== Edge Result =====");
            singleTest(sut, edgeGen);
            if(createDot || createPng) {
            	SUTVisualizer<String> viz = new SUTVisualizer<>();

                viz.exportDot(sut, dotFile);
                if(createPng) viz.renderToPng(dotFile, pngFile);
            }
            
		}
		else {
			File dir = new File(filePath);
			String[] files = dir.list(new FilenameFilter() {
	            @Override
	            public boolean accept(File d, String name) {
	                return name.endsWith(".txt");
	            }
	        });
	        if (files == null || files.length == 0) {
	            System.err.println("Cannot find any .txt file in the directory");
	            return;
	        }
	        List<String> sutName = new ArrayList<>();
	        List<SUT<String>> sutList = new ArrayList<>();
            List<TestCaseGenerator<String>> cpcGen = new ArrayList<>();
            List<TestCaseGenerator<String>> filterGen = new ArrayList<>();
            List<TestCaseGenerator<String>> edgeGen = new ArrayList<>();

	        for (String fname : files) {
	            String path = filePath + File.separator + fname;
	            sutName.add(fname);
	            SUT<String> sut = SUTLoader.loadFromTxt(path);
	            sutList.add(sut);
	            cpcGen.add(new CPCGenerator<>(sut));
	            filterGen.add(new FilterGenerator<>(sut));
	            edgeGen.add(new EdgeGenerator<>(sut));
	        }
	        System.out.println("Number of cases: " + sutList.size());
            System.out.println("===== CPC Result =====");
            if(saveCSV) {
           	 	String[] headers = {
           	            "CPC", "valid(T)", "size", "lT", 
           	            "u_edges(T)", "avg(|t|)", "s(T)", 
           	            "eff_edges(T)","cov_cp_positive(T)", 
           	            "cov_cp_once(T)", "cov_cp_negative(T)",
           	            "cov_cp_only-once(T)", "cov_edges(T)", "time[ms]"
           	        };
           	 	pw.println(String.join(",", headers));
            }
            multiTest(sutList, cpcGen, sutName);
            if(saveCSV) pw.println();
            
            System.out.println("===== Filter Result =====");
            if(saveCSV) {
           	 	String[] headers = {
           	            "Filter", "valid(T)", "size", "lT", 
           	            "u_edges(T)", "avg(|t|)", "s(T)", 
           	            "eff_edges(T)","cov_cp_positive(T)", 
           	            "cov_cp_once(T)", "cov_cp_negative(T)",
           	            "cov_cp_only-once(T)", "cov_edges(T)", "time[ms]"
           	        };
           	 	pw.println(String.join(",", headers));
            }
            multiTest(sutList, filterGen, sutName);
            if(saveCSV) pw.println();
            
            System.out.println("===== Edge Result =====");
            if(saveCSV) {
           	 	String[] headers = {
           	            "Edge", "valid(T)", "size", "lT", 
           	            "u_edges(T)", "avg(|t|)", "s(T)", 
           	            "eff_edges(T)","cov_cp_positive(T)", 
           	            "cov_cp_once(T)", "cov_cp_negative(T)",
           	            "cov_cp_only-once(T)", "cov_edges(T)", "time[ms]"
           	        };
           	 	pw.println(String.join(",", headers));
            }
            multiTest(sutList, edgeGen, sutName);
            if(saveCSV) pw.println();
		}
	}
	private static void singleTest(SUT<String> sut, TestCaseGenerator<String> testcase) {
		double t0 = System.nanoTime();
        List<List<String>> tests = testcase.generate();
        double timeMs = (System.nanoTime() - t0) * 0.000001;
        if(showPath) {
        	System.out.println("Path:");
        	for(List<String> test : tests) {
        		System.out.println("  " + test);
        	}
        	System.out.println();
        }
        int valid    = Metrics.valid(sut, tests);
        int size     = Metrics.size(tests);
        int lT       = Metrics.totalEdges(tests);
        int uEdges   = Metrics.uniqueEdges(sut, tests);
        double avg   = Metrics.averageLength(tests);
        double std   = Metrics.lengthStdDev(tests);
        double eff   = Metrics.edgeEfficiency(sut, tests);
        double pos   = Metrics.covPositive(sut, tests);
        double once  = Metrics.covOnce(sut, tests);
        double neg   = Metrics.covNegative(sut, tests);
        double max1  = Metrics.covMaxOnce(sut, tests);
        double cov   = Metrics.edgeCoverage(sut, tests);
        System.out.println("valid(T) = " + valid);
        System.out.println("|T| = " + size);
        System.out.println("l(T) = " + lT);
        System.out.println("u_edges(T) = " + uEdges);
        System.out.println("avg(|t|) = " + avg);
        System.out.println("s(T) = " + std);
        System.out.println("eff_edges(T) = " + eff);
        System.out.println("cov_cp_positive(T) = " + pos);
        System.out.println("cov_cp_once(T) = " + once);
        System.out.println("cov_cp_negative(T) = " + neg);
        System.out.println("cov_cp_only-once(T) = " + max1);
        System.out.println("edge_cov(T) = " + cov);
        System.out.println("t[ms] = " + timeMs);
        System.out.println();
	}
	
	private static void multiTest(List<SUT<String>> suts,
								List<TestCaseGenerator<String>> testcases,
								List<String> sutName) throws IOException {
		int allValid = 0;
        int avgSize = 0;
        int avglT = 0;
        double avgstd = 0;
        double avgeff = 0;
        double avgcov = 0;
        double avgTime = 0;
        
        Iterator<SUT<String>> a = suts.iterator();
        Iterator<TestCaseGenerator<String>> b = testcases.iterator();
        Iterator<String> c = sutName.iterator();
        
        while (a.hasNext() && b.hasNext() && c.hasNext()) {
        	SUT<String> sut = (SUT<String>) a.next();
        	TestCaseGenerator<String> gen = (TestCaseGenerator<String>) b.next();
        	String name = (String) c.next();
        	
        	double t0 = System.nanoTime();
        	List<List<String>> tests = gen.generate();
        	double timeMs = (System.nanoTime() - t0) * 0.000001;
        	
        	int valid    = Metrics.valid(sut, tests);
            int size     = Metrics.size(tests);
            int lT       = Metrics.totalEdges(tests);
            int uEdges   = Metrics.uniqueEdges(sut, tests);
            double avg   = Metrics.averageLength(tests);
            double std   = Metrics.lengthStdDev(tests);
            double eff   = Metrics.edgeEfficiency(sut, tests);
            double pos   = Metrics.covPositive(sut, tests);
            double once  = Metrics.covOnce(sut, tests);
            double neg   = Metrics.covNegative(sut, tests);
            double max1  = Metrics.covMaxOnce(sut, tests);
            double cov   = Metrics.edgeCoverage(sut, tests);
            allValid    += valid > 0 ? 1 : 0;
	        avgSize     += size;
	        avglT       += lT;
	        avgstd      += std;
	        avgeff      += eff;
	        avgcov      += cov;
	        avgTime     += timeMs;
	        if(showPath) {
	        	System.out.println("===== " + name + " =====");
	        	for(List<String> test : tests) {
	        		System.out.println("  " + test);
	        	}
	        	System.out.println();
	        }
	        if(saveCSV) {
	        	String[] row = {
	                    String.valueOf(name),
	                    String.valueOf(valid),
	                    String.valueOf(size),
	                    String.valueOf(lT),
	                    String.valueOf(uEdges),
	                    String.valueOf(avg),
	                    String.valueOf(std),
	                    String.valueOf(eff),
	                    String.valueOf(pos),
	                    String.valueOf(once),
	                    String.valueOf(neg),
	                    String.valueOf(max1),
	                    String.valueOf(cov),
	                    String.valueOf(timeMs)
	                };
	                pw.println(String.join(",", row));
	        }
        }
        if(showPath) System.out.println("===== Total Result =====");
        System.out.println("Valid rate = " + (double)allValid / testcases.size());
        System.out.println("Avg |T| = " + (double)avgSize / testcases.size());
        System.out.println("Avg l(T) = " + (double)avglT / testcases.size());
        System.out.println("Avg s(T) = " + avgstd / testcases.size());
        System.out.println("Avg eff_edges(T) = " + avgeff / testcases.size());
        System.out.println("Avg edge_cov(T) = " + avgcov / testcases.size());
        System.out.println("Avg t[ms] = " + avgTime / testcases.size());
        System.out.println();
	}
	
	//main
	public static void main(String[] args) throws InterruptedException, IOException {
		run(args);
	}
}
		
		