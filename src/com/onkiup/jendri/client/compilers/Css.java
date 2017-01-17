package com.onkiup.jendri.client.compilers;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.onkiup.jendri.client.BuilderService;
import com.onkiup.jendri.client.Compiler;

public class Css implements Compiler {
    @Override
    public Set<String> getSupportedTypes() {
        return new HashSet<>(Arrays.asList("css"));
    }

    @Override
    public String getResultType() {
        return "css.compiled";
    }

    @Override
    public Reader compile(File code) throws Exception {
        return new FileReader(code);
    }

    @Override
    public BuilderService.ApplicationSection getApplicationSection() {
        return BuilderService.ApplicationSection.STYLE;
    }
}
