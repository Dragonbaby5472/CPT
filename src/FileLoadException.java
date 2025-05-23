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
 * Signals that an error occurred while loading or processing a file.
 *
 * <p>This exception wraps underlying I/O or parsing errors
 * to provide a uniform error type for file‐loading operations.</p>
 */
public class FileLoadException extends Exception{
	private static final long serialVersionUID = 1L;
	
    /**
     * Constructs a new FileLoadException with the specified detail message.
     *
     * @param msg the detail message explaining the reason for the failure
     */
	public FileLoadException(String msg) {
		super(msg);
	}
	
	 /**
     * Constructs a new FileLoadException with the specified detail message
     * and the underlying cause of the failure.
     *
     * @param msg   the detail message explaining the reason for the failure
     * @param cause the underlying exception that caused this error
     */
	public FileLoadException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
