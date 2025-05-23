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

/**
 * Indicates that an input string or configuration could not be parsed
 * due to an unexpected or invalid format.
 *
 * <p>This exception is thrown by parsing routines when the data being
 * processed does not conform to the expected syntax or structure.</p>
 */
public class ParseFormatException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
     * Constructs a new ParseFormatException with the specified detail message.
     *
     * @param msg the detail message explaining why the parsing failed
     */
	public ParseFormatException(String msg) {
        super(msg);
    }
	
	/**
     * Constructs a new ParseFormatException with the specified detail message
     * and underlying cause.
     *
     * @param msg   the detail message explaining why the parsing failed
     * @param cause the underlying exception that triggered this parse error
     */
    public ParseFormatException(String msg, Throwable cause) {
        super(msg, cause);
    }
}