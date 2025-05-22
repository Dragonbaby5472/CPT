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

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that duplicates write, flush, and close operations
 * across multiple underlying OutputStream instances.
 *
 * <p>All data written to this stream will be forwarded to each of the
 * configured streams in the order they were provided.</p>
 */
public class MultiOutputStream extends OutputStream {
    private final OutputStream[] streams;
    
    /**
     * Constructs a MultiOutputStream to fan-out operations to the given streams.
     *
     * @param streams one or more OutputStream instances to receive all data,
     *                flush, and close calls
     */
    public MultiOutputStream(OutputStream... streams) {
        this.streams = streams;
    }
    
    /**
     * Writes the specified byte to all underlying streams.
     *
     * @param b the byte to write
     * @throws IOException if an I/O error occurs in any of the streams
     */
    @Override
    public void write(int b) throws IOException {
        for (OutputStream s : streams) s.write(b);
    }
    
    /**
     * Writes {@code len} bytes from the specified byte array starting
     * at offset {@code off} to each underlying stream.
     *
     * @param b   the byte array containing data to write
     * @param off the start offset in the byte array
     * @param len the number of bytes to write
     * @throws IOException if an I/O error occurs in any of the streams
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream s : streams) s.write(b, off, len);
    }
    
    /**
     * Flushes all underlying streams, ensuring that any buffered output
     * bytes are written out.
     *
     * @throws IOException if an I/O error occurs in any of the streams
     */
    @Override
    public void flush() throws IOException {
        for (OutputStream s : streams) s.flush();
    }
    
    /**
     * Closes all underlying streams and releases any associated
     * system resources.
     *
     * @throws IOException if an I/O error occurs when closing any stream
     */
    @Override
    public void close() throws IOException {
        for (OutputStream s : streams) s.close();
    }
}


