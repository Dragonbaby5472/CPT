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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;

/**
 * Utility class for loading a System Under Test (SUT) model from a plain-text specification.
 *
 * <p>This loader reads the file line by line and populates a {@link SUT} instance
 * by interpreting the following patterns:
 * <ul>
 *   <li><code>END:[&lt;empty&gt;]</code> or <code>START:[v1,v2,...]</code> – designates the start vertex and, if successors list is empty, also marks it as an end vertex; otherwise edges to listed vertices are added.</li>
 *   <li><code>&lt;vertex&gt;:[v1,v2,...]</code> – adds directed edges from <code>vertex</code> to each successor.</li>
 *   <li><code>Constraint[from - to - TYPE]</code> – registers a constraint between two vertices of the specified {@link ConstraintType}.</li>
 * </ul>
 * Blank or unrecognized lines are skipped.</p>
 */
public class SUTLoader {

	/**
     * Parses the given text file and returns a {@code SUT<String>} populated
     * with vertices, edges, start/end markers, and vertex-pair constraints.
     *
     * <p>Supported line formats:
     * <ul>
     *   <li><code>START:[…]</code> – set start vertex, optionally mark ends or add edges.</li>
     *   <li><code>Vertex:[…]</code> – define outgoing edges.</li>
     *   <li><code>Constraint[…]</code> – add a constraint of type POSITIVE, ONCE, NEGATIVE, or MAX_ONCE.</li>
     * </ul>
     * </p>
     *
     * @param filePath path to the text file describing the SUT model
     * @return a {@code SUT<String>} instance constructed from the file contents
     * @throws IOException if reading from the file fails
	 * @throws FileLoadException 
     */
	public static SUT<String> loadFromTxt(String filePath) throws ParseFormatException , FileLoadException {
		Path path = Paths.get(filePath);
		if (!Files.exists(path)) {
            throw new FileLoadException("File " + path.toString() + " is not exist.");
        }
		SUT<String> sut = new SUT<>();
	    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	        String line;
	        int lineNum = 0;
	        while ((line = reader.readLine()) != null) {
	        		lineNum++;
	            line = line.trim();
	            if (line.isEmpty() || line.startsWith("#")) {
	                continue;
	            }
	            if (line.startsWith("Constraint")) {
	            	int lb = line.indexOf('['), rb = line.indexOf(']');
	            		if (lb < 0 || rb <= lb) {
	            	        throw new ParseFormatException(
	            	            String.format("Error occurred at file %s%s"
	            	            		+ "Line %d: Invalid Constraint format, missing brackets [] in: %s", filePath, System.lineSeparator(), lineNum, line)
	            	        );
	            	    }
	            		
		            String content = line.substring(lb + 1, rb);
		            String[] tk = content.split("-");
		            if (tk.length != 3) {
		                throw new ParseFormatException(
		                    String.format("Error occurred at file %s%s"
		                        + "Line %d: Invalid Constraint format, expected from-to-type but get: %s"
		                    		, filePath, System.lineSeparator(), lineNum, content
		                    )
		                );
		            }
		            String cFrom = tk[0].trim();
		            String cTo   = tk[1].trim();
		            try {
		            		ConstraintType type = ConstraintType.valueOf(tk[2].trim());
		            		sut.addConstraint(new Constraint<>(cFrom, cTo, type));
		            }catch(IllegalArgumentException e) {
		            	 	throw new ParseFormatException(
		            	            String.format("Error occurred at file %s%s"
		            	            		+ "Line %d: Invalid ConstraintType, unsupported value '%s'",
		            	            		filePath, System.lineSeparator() , lineNum, tk[2].trim())
		            	        );
		            }
		            continue;
	            }
	            if (!line.contains(":")) {
	                throw new ParseFormatException(
	                    String.format("Error occurred at file %s%s"
        	            		+ "Line %d: Missing ':' separator. Invalid format: %s",
        	            		filePath, System.lineSeparator(), lineNum, line)
	                );
	            }
	            String[] parts = line.split(":", 2);
	            
	            if (parts.length != 2) {
	                throw new ParseFormatException(
	                    String.format("Error occurred at file %s%s"
        	            		+ "Line %d: Unable to split into two parts by ':'. Got %d parts: %s",
        	            		filePath, System.lineSeparator(), lineNum, parts.length, line)
	                );
	            }
	            
	            String from = parts[0].trim();

	            if (from.equals("START") || from.equals("Start")) {
	                sut.setStartVertex(from);
	            }
	            if (parts[1].trim().equals("[]")) {
	                sut.addEndVertex(from);
	            }

	            sut.addVertex(from);
	            
	            String succList = parts[1].trim();
	            
	            if (!succList.startsWith("[") || !succList.endsWith("]")) {
	                throw new ParseFormatException(
	                    String.format("Error occurred at file %s%s"
        	            		+ "Line %d: Successor list must be enclosed in '[' and ']': %s"
	                    		, filePath, System.lineSeparator(), lineNum, succList)
	                );
	            }
	            String inner = succList.substring(1, succList.length() - 1).trim();
	            if (!inner.isEmpty()) {
	                for (String to : inner.split(",")) {
	                    to = to.trim();
	                    if (to.isEmpty()) {
	                        throw new ParseFormatException(
	                            String.format("Error occurred at file %s%s"
	            	            		+ "Line %d: Empty target node in list: %s",
	            	            		filePath, System.lineSeparator(), lineNum, inner)
	                        );
	                    }
	                    
	                    if (to.startsWith("END") || to.startsWith("end")) {
	                        sut.addEndVertex(to);
	                    }
	                    sut.addVertex(to);
	                    sut.addEdge(from, to);
	                    }
	            }
	            
	        }
	        if(sut.getStartVertex() == null) {
	        		throw new ParseFormatException(
                        String.format("Error occurred at file %s%s"
        	            		+ "No start vertice named START or Start", filePath, System.lineSeparator())
                    );
	        }
	        if(sut.getEndVertices().isEmpty()) {
	        		throw new ParseFormatException(
                        String.format("Error occurred at file %s%s"
        	            		+ "No end vertice in this SUT.", filePath, System.lineSeparator())
                    );
	        }
	    }catch(IOException e) {
	    		throw new FileLoadException("Unexpected error：" + e.getMessage(), e);
	    }
	    return sut;
	}
	
}