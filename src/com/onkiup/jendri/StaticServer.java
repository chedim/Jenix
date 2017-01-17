package com.onkiup.jendri;

import com.onkiup.jendri.api.AbstractStaticServer;
import com.onkiup.jendri.injection.Inject;

public class StaticServer extends AbstractStaticServer {
    @Inject("./web")
    private static String root;
    @Inject("/index.html")
    private static String indexFile;
    @Inject("/*")
    private static String baseUrl;

    @Override
    protected String getRoot() {
        return root;
    }

    @Override
    protected String getIndexFile() {
        return indexFile;
    }

    @Override
    public String getUrl() {
        return baseUrl;
    }
}
