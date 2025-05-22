# CPT generator
## Introduction
This program reproduces the algorithm described in the paper “Novel Algorithm to Solve the Constrained Path-Based Testing Problem,” with some algorithmic improvements.
## develop environment
- Java JDK: 21.0.7.6
- JRE System Lib: JavaSE-1.8
- Maven dependency:
    - junit jupiter engine
    - junit jupiter params
    - jgrapht core
    - jgrapht io
- IDE: Eclipse IDE 2025-6

## Execution Instructions

- `-file <file_name>.txt`
Load a SUT model from the specified text file.
- `-dir <dir_name>`
Process all `.txt` files in the given directory.
- `-log <log_file>`
Write console output to the specified log file.
- `-showpath`
Display the solution paths in set T.
- `-topng <png_name>`
Generate a PNG image of the SUT (requires Graphviz).
- `-todot <dot_name>`
Export the SUT graph to a DOT file.
- `-csv <csv_name>`
Save metrics to a CSV file (only available with `-dir`).

You can run the example SUT with:

```bash
java -jar generator.jar -file msa_example.txt -log result.log -showpath
```
## Reference
> M. Klima et al., "Novel Algorithm to Solve the Constrained Path-Based Testing Problem," 2025 IEEE International Conference on Software Testing, Verification and Validation Workshops (ICSTW), Naples, Italy, 2025, pp. 41-49, doi: 10.1109/ICSTW64639.2025.10962488.Abstract: Constrained Path-based Testing (CPT) is a technique that extends traditional path-based testing by adding constraints on the order or presence of specific sequences of actions in the tests of System Under Test (SUT) processes. Through such an extension, CPT enhances the ability of the model to capture more real-life situations. In CPT, we define four types of constraints that either enforce or prohibit the use of a pair of actions in the resulting test set. We propose a novel Constrained Path-based Testing Composition (CPC) algorithm to solve the Constrained Path-based Testing Problem. We compare the results returned by the CPC algorithm with two alternatives, (1) the Filter algorithm, which solves the CPT problem in a greedy manner, and (2) the Edge algorithm, which generates a set of test cases that satisfy edge coverage. We evaluated the algorithms on 200 problem instances, with the CPC algorithm returning test sets (T) that have, on average, 350 edges, which is 2.4% and 11.1% shorter than the average number of edges in T returned by the Filter algorithm and the Edge algorithm, respectively. Regarding the compliance of the generated T with the constraints, the CPC algorithm produced T that satisfied the constraints in 95% of the cases, the Filter algorithm in 45% cases, and the Edge algorithm returned T that satisfied the constraints only for 6% SUT instances. Regarding the coverage of edges, the CPC algorithm returned test sets that contained, on average, 91.5% of edges in the graphs, while for T returned by the Filter algorithm, it was 90.8% edges. When comparing the average results of the edge coverage criterion and the fulfillment of the constraint criterion by individual algorithms, we consider the incomplete edge coverage achieved by the CPC algorithm and, at the same time, 95% fulfillment of the graph constraints to be a reasonable compromise. keywords: {Software testing;Scalability;Conferences;Software algorithms;Semantics;Metaheuristics;Employment;Directed graphs;Filtering algorithms;Testing;Model-based Testing;Path-based Testing;Constrained Path-based Testing;Directed Graph Algorithms;Automated Test Case Generation},URL: https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=10962488&isnumber=10962453

