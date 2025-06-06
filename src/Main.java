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

import java.io.*;
import java.util.*;

/**
 * Entry point for the Constrained Path-Based Testing application.
 *
 * <p>This class handles command-line argument parsing, logging and CSV setup,
 * loading of System Under Test (SUT) models from a single file or directory,
 * dispatching to CPC, Filter, and Edge test case generators, and optional
 * graph visualization. It also collects and prints test metrics.</p>
 */
public class Main {
	
	private static boolean showPath = false;
	private static boolean saveCSV = false;
	private static String csvPath = "./result.csv";
	private static PrintWriter pw;
	
	/**
     * Parses command-line arguments, configures logging, CSV output, and visualization,
     * then drives test generation for one or more SUT models.
     *
     * @param args array of command-line options:
     *             -file &lt;path&gt; or -dir &lt;path&gt; to specify input,
     *             -log &lt;file&gt; to enable logging,
     *             -csv &lt;file&gt; to enable CSV output,
     *             -todot &lt;file&gt; / -topng &lt;file&gt; for graph export.
     * @throws InterruptedException if graph rendering sleep is interrupted
     * @throws IOException if reading or writing any file fails
	 * @throws FileLoadException 
     */
	@SuppressWarnings("resource")
	private static void run(String[] args)throws InterruptedException, IOException, FileLoadException {
		boolean isFile = true;
		boolean createLog = false;
		boolean createDot = false;
		boolean createPng = false;
		String filePath = null;
		String logFile = "./result.log";
		String dotFile = "./temp.dot";
		String pngFile = "./sut.png";
		
		//args instructions
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
		if(filePath == null) {
			System.out.println("No file specifed, using default SUT.");
			System.out.println();
		}
		
		//write system.out to both console and logfile if necessary
		FileOutputStream fos = new FileOutputStream(logFile);
		MultiOutputStream mos = new MultiOutputStream(System.out, fos);
		PrintStream ps = new PrintStream(mos, true);
		if(createLog) {
			System.setOut(ps);
			System.setErr(ps);
		}
		if(saveCSV && !isFile) {
			pw = new PrintWriter(new FileWriter(csvPath));
		}

		if(isFile){
			SUT<String> sut = null;
			if(filePath == null) {
				sut = new SUT<>();
				sut.setStartVertex("START");
		        sut.addEndVertex("report service");
				String[] vertices = {
			            "START",
			            "order service",
			            "user service",
			            "notification service",
			            "product service",
			            "payment service",
			            "inventory service",
			            "analytics service",
			            "auth service",
			            "shipping service",
			            "report service"
			        };
		        for (String v : vertices) {
		            sut.addVertex(v);
		        }
		        sut.addEdge("START",               "order service");
		        sut.addEdge("order service",       "user service");
		        sut.addEdge("order service",       "notification service");
		        sut.addEdge("order service",       "product service");
		        sut.addEdge("order service",       "payment service");
		        sut.addEdge("user service",        "auth service");
		        sut.addEdge("user service",        "analytics service");
		        sut.addEdge("notification service","user service");
		        sut.addEdge("product service",     "inventory service");
		        sut.addEdge("product service",     "report service");
		        sut.addEdge("payment service",     "notification service");
		        sut.addEdge("inventory service",   "order service");
		        sut.addEdge("inventory service",   "shipping service");
		        sut.addEdge("analytics service",   "report service");
		        sut.addEdge("auth service",        "user service");
		        sut.addEdge("shipping service",    "notification service");
		        sut.addEdge("shipping service",    "report service");
		        sut.addConstraint(new Constraint<>(
		                "order service", "product service", ConstraintType.MAX_ONCE));
		        sut.addConstraint(new Constraint<>(
		                "order service", "auth service", ConstraintType.ONCE));
		        sut.addConstraint(new Constraint<>(
		                "notification service", "report service", ConstraintType.POSITIVE));
		        sut.addConstraint(new Constraint<>(
		                "shipping service", "auth service", ConstraintType.NEGATIVE));
			}
			else {
				try{
					sut = SUTLoader.loadFromTxt(filePath);
					constraintValid(filePath, sut);
				}catch(FileLoadException f) {
					System.err.println(f.getMessage());
					System.err.println("The program has been suspended due to the above error.");
					System.exit(1);
				}catch(ParseFormatException p) {
					System.err.println(p.getMessage());
					System.err.println("The program has been suspended due to the above error.");
					System.exit(2);
				}
			}
			
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
			try {
				if (!dir.exists()) {
					throw new FileLoadException(String.format("Directory %s does not exist.", filePath));
				}
				if (!dir.isDirectory()) {
					throw new FileLoadException(String.format("Provided path %s is not a directory.", filePath));
				}
			}catch(FileLoadException e) {
				System.err.println(e.getMessage());
				System.err.println("The program has been suspended due to the above error.");
				System.exit(2);
			}
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
	            SUT<String> sut = null;
	            try{
					sut = SUTLoader.loadFromTxt(path);
					constraintValid(fname, sut);
				}catch(FileLoadException f) {
					System.err.println(f.getMessage());
					System.err.println("Skip this file due to above error(s).");
					continue;
				}catch(ParseFormatException p) {
					System.err.println(p.getMessage());
					System.err.println("Skip this file due to above error(s).");
					continue;
				}
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
	
	/**
     * Generates and evaluates a SUT using the specified TestCaseGenerator. 
     * Measures execution time, optionally
     * prints each path, and outputs coverage metrics.
     *
     * @param sut      the System Under Test model to be tested
     * @param testcase the TestCaseGenerator instance (e.g., CPCGenerator, FilterGenerator, EdgeGenerator)
     */
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
	
    /**
     * Executes test case generation for multiple SUT instances in batch mode.
     * Iterates through the provided SUT models and corresponding generators,
     * aggregates metrics across all runs, optionally prints details and writes CSV.
     *
     * @param suts      list of SUT models to process
     * @param testcases list of TestCaseGenerator instances matching each SUT
     * @param sutNames  list of identifiers or filenames for each SUT
     * @throws IOException if writing to the CSV output fails
     */
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
	
	/**
	 * Validates that each constraint in the given SUT refers only to existing vertices.
	 *
	 * <p>Iterates through all {@link Constraint} objects in the SUT and ensures that
	 * both the source ("from") and target ("to") vertices are present in the SUT's graph.
	 * If any referenced vertex is missing, a {@link ParseFormatException} is thrown
	 * with a detailed message including the input file path and the invalid constraint.</p>
	 *
	 * @param <V>  the vertex type used in the SUT model
	 * @param file the path of the input file (used for error reporting)
	 * @param sut  the System Under Test model whose constraints are to be validated
	 * @throws ParseFormatException if the graph does not contain a vertex referenced by any constraint
	 */
	private static <V> void constraintValid(String file, SUT<V> sut) throws ParseFormatException {
		for(Constraint<V> c : sut.getConstraints()) {
			if(!sut.getGraph().containsVertex(c.getFrom())) {
				throw new ParseFormatException(
					String.format("Error occurred at file %s%s"
						+ "Invalid constraint %s: SUT doesn't contain vertex %s", file, System.lineSeparator(), c, c.getFrom())
				);
			}
			if(!sut.getGraph().containsVertex(c.getTo())) {
				throw new ParseFormatException(
					String.format("Error occurred at file %s%s"
						+ "Invalid constraint %s: SUT doesn't contain vertex %s", file, System.lineSeparator(), c, c.getTo())
					);
			}
		}
	}
	
    /**
     * Main application entry point.
     *
     * @param args command-line arguments passed to the program
     * @throws InterruptedException if run() is interrupted during execution
     * @throws IOException if an I/O error occurs in run()
     * @throws FileLoadException 
     */
	public static void main(String[] args) throws InterruptedException, IOException, FileLoadException {
		run(args);
	}
}
		
		