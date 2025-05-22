/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
 */

package com.example.cpb_test;

import java.io.IOException;
import java.io.OutputStream;

public class MultiOutputStream extends OutputStream {
    private final OutputStream[] streams;
    public MultiOutputStream(OutputStream... streams) {
        this.streams = streams;
    }
    @Override
    public void write(int b) throws IOException {
        for (OutputStream s : streams) s.write(b);
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream s : streams) s.write(b, off, len);
    }
    @Override
    public void flush() throws IOException {
        for (OutputStream s : streams) s.flush();
    }
    @Override
    public void close() throws IOException {
        for (OutputStream s : streams) s.close();
    }
}


