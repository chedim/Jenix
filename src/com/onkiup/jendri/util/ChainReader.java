package com.onkiup.jendri.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.activation.UnsupportedDataTypeException;

public class ChainReader extends Reader {

    private Object[] sources;
    private int currentSource = 0;

    public ChainReader(Object... sources) {
        this.sources = sources;
    }

    private Reader getSource() throws UnsupportedDataTypeException {
        Reader result = null;
        if (currentSource < sources.length) {
            Object source = sources[currentSource];
            if (source instanceof Reader) {
                result = (Reader) source;
            } else if (source instanceof InputStream) {
                sources[currentSource] = result = new InputStreamReader((InputStream) source);
            } else if (source instanceof String) {
                sources[currentSource] = result = new StringReader((String) source);
            } else {
                throw new UnsupportedDataTypeException(source.getClass().getName());
            }
        }

        return result;
    }

    private Reader advanceSource() throws UnsupportedDataTypeException {
        if (currentSource + 1 < sources.length) {
            ++currentSource;
            return getSource();
        }
        return null;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        Reader delegate = getSource();
        for (int i = 0; i < off; ) {
            i += delegate.skip(off);
            if (i < off) {
                // not all bytes were skipped. Assuming that source has ended
                delegate = advanceSource();
            }
        }

        for (int i = 0; i < len; ) {
            char[] iterationBuffer = new char[len - i];
            int read = delegate.read(iterationBuffer);
            // copying read chars int cbuf
            for (int c = i; c < i + read; c++) {
                cbuf[c] = iterationBuffer[c - i];
            }
            if (i + read < len) {
                delegate = advanceSource();
            }

            i += read;

            if (delegate == null) {
                return i;
            }
        }

        return len;
    }

    @Override
    public void close() throws IOException {
        Reader delegate = getSource();
        while(null != delegate) {
            delegate.close();
            delegate = advanceSource();
        }
    }
}
