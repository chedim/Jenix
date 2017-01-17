package com.onkiup.jendri;

import com.onkiup.jendri.api.AbstractLogServer;
import com.onkiup.jendri.injection.Inject;

public class LogServer extends AbstractLogServer {

    @Inject("/_error")
    private static String url;

    @Inject("yyyy-MM-dd HH:mm:ss")
    private static String datePattern;

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    protected String getDatePattern() {
        return datePattern;
    }
}
