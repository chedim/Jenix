package com.onkiup.jendri.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;

public class ByteReader extends Reader {

    private ByteBuffer source;

    public ByteReader(ByteBuffer source) {
        this.source = source;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        for (int i=0; i<off; i++) {
            if (!source.hasRemaining()) {
                return 0;
            }
            source.getChar();
        }

        for (int i=0; i<len; i++) {
            if (!source.hasRemaining()) {
                return i;
            }
            cbuf[i] = source.getChar();
        }

        return len;
    }

    @Override
    public void close() throws IOException {
        source.clear();
    }
}
