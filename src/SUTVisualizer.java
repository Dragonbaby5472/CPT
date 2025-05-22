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


 //將 SUT 物件匯出為 DOT 檔，並呼叫 GraphViz 產生圖像
public class SUTVisualizer<V> {

    public void exportDot(SUT<V> sut, String dotFile) throws IOException {
        Graph<V, DefaultEdge> g = sut.getGraph();
        DOTExporter<V, DefaultEdge> exporter = new DOTExporter<>(
            v -> v.toString().replaceAll("\\W+", "_")); // 頂點 ID
        // 設定頂點標籤
        exporter.setVertexAttributeProvider(v -> {
            Map<String, Attribute> m = new LinkedHashMap<>();
            m.put("label", DefaultAttribute.createAttribute(v.toString()));
            return m;
        });
        // 匯出點的 provider
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
