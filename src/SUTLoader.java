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


public class SUTLoader {

	public static SUT<String> loadFromTxt(String filePath) throws IOException {
	    SUT<String> sut = new SUT<>();
	    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	        String line;

	        while ((line = reader.readLine()) != null) {
	            line = line.trim();
	            if (line.isEmpty()) {
	                continue;
	            }
	            if (line.startsWith("Constraint")) {
	            	int lb = line.indexOf('['), rb = line.indexOf(']');
		            if (lb >= 0 && rb > lb) {
		                String content = line.substring(lb + 1, rb);
		                String[] tk = content.split("-");
		                if (tk.length == 3) {
		                    String cFrom = tk[0].trim();
		                    String cTo   = tk[1].trim();
		                    ConstraintType type = ConstraintType.valueOf(tk[2].trim());
		                    sut.addConstraint(new Constraint<>(cFrom, cTo, type));
		                }
		            }
		            continue;
	            }
	            String[] parts = line.split(":", 2);
	            String from = parts[0].trim();

	            if (from.equals("START") || from.equals("Start")) {
	                sut.setStartVertex(from);
	            }
	            if (parts[1].trim().equals("[]")) {
	                sut.addEndVertex(from);
	            }

	            sut.addVertex(from);

	            String succList = parts[1].trim();
	            if (succList.startsWith("[") && succList.endsWith("]")) {
	                String inner = succList.substring(1, succList.length() - 1).trim();
	                if (!inner.isEmpty()) {
	                    for (String to : inner.split(",")) {
	                        to = to.trim();
	                        if (to.startsWith("END") || to.startsWith("end")) {
	                            sut.addEndVertex(to);
	                        }
	                        sut.addVertex(to);
	                        sut.addEdge(from, to);
	                    }
	                }
	            }
	        }
	    }
	    return sut;
	}
	
}