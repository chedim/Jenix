package com.onkiup.jendri.access.oauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.AbstractServlet;
import com.onkiup.jendri.injection.Inject;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;

public class OauthAccessPoint extends AbstractServlet {
    @Inject("/oauth/access")
    protected static String url;

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


    }
}
