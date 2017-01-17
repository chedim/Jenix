package com.onkiup.jendri.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.tools.javac.code.Printer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import sun.jvm.hotspot.utilities.ObjectReader;

public class MethodInputStream extends InputStream {

    private Function from;

    public MethodInputStream(Function from) throws IOException {
        this.from = from;
    }

    @Override
    public int read() throws IOException {
        return 0;
    }
}
