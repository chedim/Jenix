package com.onkiup.jendri.client;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class CompilerServiceImpl implements CompilerService.Implementation {

    private static final Reflections REFLECTIONS = new Reflections("", new SubTypesScanner());
    private static final HashMap<String, Compiler> compilers = new HashMap<>();

    @Override
    public JApplicationCompiledFile compile(JApplicationFile file) throws Exception {
        File source = new File(file.getLocation());
        if (compilers.containsKey(file.getType())) {
            return compilers.get(file.getType()).compile(file);
        } else {
            throw new UnsupportedOperationException("No compiler for " + file.getType());
        }
    }

    @Override
    public BuilderService.ApplicationSection getSection(String type) {
        return compilers.get(type).getApplicationSection();
    }

    @Override
    public void start() throws Exception {
        Set<Class<? extends Compiler>> compilerClasses = REFLECTIONS.getSubTypesOf(Compiler.class);
        for (Class<? extends Compiler> compilerClass : compilerClasses) {
            if (!Modifier.isAbstract(compilerClass.getModifiers())) {
                Compiler compiler = compilerClass.newInstance();
                Set<String> types = compiler.getSupportedTypes();
                for (String type : types) {
                    compilers.put(type, compiler);
                }
            }
        }
    }

    @Override
    public void stop() throws Exception {
        compilers.clear();
    }

    @Override
    public boolean isCompilable(String type) {
        return compilers.containsKey(type);
    }

}
