package com.onkiup.jendri;

import com.onkiup.jendri.client.AbstractAppServer;
import com.onkiup.jendri.injection.Inject;

public class AppServer extends AbstractAppServer {
    @Inject("/app/*")
    private static String baseUrl;

    @Inject("interfaces/default")
    private static String defaultInterface;

    @Override
    public String getUrl() {
        return baseUrl;
    }

    public static String getDefaultInterface() {
        return defaultInterface;
    }
}
