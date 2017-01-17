package com.onkiup.jendri.client.compilers;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.onkiup.jendri.client.BuilderService;
import com.onkiup.jendri.client.Compiler;

public class JavaScript implements Compiler {
    @Override
    public Set<String> getSupportedTypes() {
        return new HashSet<>(Arrays.asList("js"));
    }

    @Override
    public String getResultType() {
        return "js.compiled";
    }

    @Override
    public Reader compile(String code) throws Exception {
        return new StringReader(code);
    }

    @Override
    public Reader compile(URL code) throws Exception {
        return new InputStreamReader(code.openStream());
    }

    @Override
    public Reader compile(File code) throws Exception {
        return new FileReader(code);
    }

    @Override
    public BuilderService.ApplicationSection getApplicationSection() {
        return BuilderService.ApplicationSection.CODE;
    }
}
