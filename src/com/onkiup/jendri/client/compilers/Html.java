package com.onkiup.jendri.client.compilers;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.onkiup.jendri.client.BuilderService;
import com.onkiup.jendri.client.Compiler;

public class Html implements Compiler {
    @Override
    public Set<String> getSupportedTypes() {
        return new HashSet<>(Arrays.asList("html"));
    }

    @Override
    public String getResultType() {
        return "html.compiled";
    }

    @Override
    public Reader compile(File code) throws Exception {
        return new FileReader(code);
    }

    @Override
    public BuilderService.ApplicationSection getApplicationSection() {
        return BuilderService.ApplicationSection.TEMPLATE;
    }
}
