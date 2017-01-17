package com.onkiup.jendri.client;

import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.service.ServiceStub;

public final class CompilerService {

    @Inject
    private static Implementation implementation;

    public static boolean isCompilable(String type) {
        return implementation.isCompilable(type);
    }

    public static JApplicationCompiledFile compile(JApplicationFile file) throws Exception {
        return implementation.compile(file);
    }

    public static BuilderService.ApplicationSection getSection(String type) {
        return implementation.getSection(type);
    }

    public interface Implementation extends ServiceStub {
        boolean isCompilable(String type);

        JApplicationCompiledFile compile(JApplicationFile file) throws Exception;

        BuilderService.ApplicationSection getSection(String type);
    }
}
