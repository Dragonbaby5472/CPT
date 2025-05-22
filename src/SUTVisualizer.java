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

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Provides utilities to export a System Under Test (SUT) graph to DOT format
 * and render it as a PNG image using GraphViz.
 *
 * <p>This class customizes vertex identifiers, labels, and styles based on the SUT’s
 * start vertex and defined constraints, then writes a DOT file and can invoke
 * the GraphViz ‘dot’ tool to produce a PNG visualization.</p>
 *
 * @param <V> the vertex type used in the SUT graph
 */
public class SUTVisualizer<V> {

	/**
     * Exports the given SUT graph to a DOT file compatible with GraphViz.
     *
     * <p>Vertex IDs are sanitized to valid DOT identifiers. The start vertex is
     * highlighted with a filled style and specific fill color. Vertices involved
     * in constraints receive color and line‐style attributes according to their types.</p>
     *
     * @param sut     the System Under Test model containing the graph and constraints
     * @param dotFile the path to the output DOT file
     * @throws IOException if an I/O error occurs during file writing
     */
    public void exportDot(SUT<V> sut, String dotFile) throws IOException {
        Graph<V, DefaultEdge> g = sut.getGraph();
        DOTExporter<V, DefaultEdge> exporter = new DOTExporter<>(
            v -> v.toString().replaceAll("\\W+", "_"));
        // Provide default vertex label
        exporter.setVertexAttributeProvider(v -> {
            Map<String, Attribute> m = new LinkedHashMap<>();
            m.put("label", DefaultAttribute.createAttribute(v.toString()));
            return m;
        });
        // Provide styled attributes for start vertex and constrained vertices
        exporter.setVertexAttributeProvider(v -> {
        	Map<String, Attribute> m = new LinkedHashMap<>();
        	m.put("label", DefaultAttribute.createAttribute(v.toString()));
            if (v.equals(sut.getStartVertex())) {
                // 填充樣式
                m.put("style", DefaultAttribute.createAttribute("filled"));
                m.put("fillcolor", DefaultAttribute.createAttribute("chartreuse4"));
            }
        	String color = null;
        	String style = null;
        	for (Constraint<V> c : sut.getConstraints()) {
        		if (c.getFrom().equals(v)) {
        			switch (c.getType()) {
        			case POSITIVE:
        				color = "green";  style = "dashed"; break;
        			case ONCE:
        				color = "blue";   style = "dashed"; break;
        			case NEGATIVE:
        				color = "red";    style = "dashed"; break;
        			case MAX_ONCE:
        				color = "orange"; style = "dashed"; break;
        			}
        		}
        		if (c.getTo().equals(v)) {
        			switch (c.getType()) {
        			case POSITIVE:
        				color = "green";  style = "solid"; break;
        			case ONCE:
        				color = "blue";   style = "solid"; break;
        			case NEGATIVE:
        				color = "red";    style = "solid"; break;
        			case MAX_ONCE:
        				color = "orange"; style = "solid"; break;
        			}
        		}
        	}

        	if (color != null) {
        		m.put("color", DefaultAttribute.createAttribute(color));
        	}
        	if (style != null) {
        		m.put("style", DefaultAttribute.createAttribute(style));
        	}
        	return m;
        });

        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(dotFile), StandardCharsets.UTF_8))) {
            exporter.exportGraph(g, writer);
        }
    }

    /**
     * Renders a DOT file to a PNG image by invoking the GraphViz ‘dot’ command.
     *
     * @param dotFile the input DOT file path
     * @param pngFile the output PNG file path
     * @throws IOException          if an I/O error occurs while executing the process
     * @throws InterruptedException if the rendering process is interrupted
     * @throws RuntimeException     if GraphViz returns a non-zero exit code
     */
    public void renderToPng(String dotFile, String pngFile)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
            "dot", "-Tpng", dotFile, "-o", pngFile);
        Process p = pb.start();
        if (p.waitFor() != 0) {
            throw new RuntimeException("GraphViz rendering failed");
        }
    }
}
