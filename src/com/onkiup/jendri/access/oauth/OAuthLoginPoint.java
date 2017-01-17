package com.onkiup.jendri.access.oauth;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.AbstractServlet;
import com.onkiup.jendri.access.AuthService;
import com.onkiup.jendri.access.User;
import com.onkiup.jendri.access.oauth.scope.OAuthTempToken;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.injection.Inject;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;

public class OAuthLoginPoint extends AbstractServlet {

    @Inject("/api/oauth/access")
    private static String url;

    @Inject("/oauth/login")
    private static String loginUrl;

    @Inject("86400")
    private static Long tokenLifeTime;

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            OAuthClient client = null;
            String redirectUrl = "/api/oauth/error";
            try {
                OAuthAuthzRequest oAuthRequest = new OAuthAuthzRequest(req);
                client = getClient(oAuthRequest);
                if (client != null) {
                    OAuthTempToken tempToken = new OAuthTempToken(client);
                    OAuthResponse oAuthResponse = OAuthASResponse.authorizationResponse(req, HttpServletResponse.SC_FOUND)
                            .setCode(tempToken.getValue())
                            .location(loginUrl)
                            .buildQueryMessage();

                    resp.sendRedirect(oAuthResponse.getLocationUri());
                }
            } catch (OAuthProblemException e) {
                final OAuthResponse oAuthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                        .error(e).location(redirectUrl).buildQueryMessage();

                resp.sendRedirect(oAuthResponse.getLocationUri());
            }
        } catch (Exception e) {
            throw new ServletException("Global error", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            OAuthTokenRequest request = new OAuthTokenRequest(req);
            OAuthClient client = getClient(request);
            User user = AuthService.getUser(req, resp);
            OAuthResponse r;
            if (user != null) {
                OAuthRefreshToken refreshToken = new OAuthRefreshToken(user);
                OAuthAccessToken accessToken = refreshToken.getPair();
                refreshToken.saveImmediately();

                r = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
                        .setAccessToken(accessToken.getValue())
                        .setRefreshToken(refreshToken.getValue())
                        .buildJSONMessage();

            } else {
                r = OAuthASResponse.tokenResponse(HttpServletResponse.SC_UNAUTHORIZED).buildJSONMessage();
            }
            resp.setStatus(r.getResponseStatus());
            Writer w = resp.getWriter();
            w.write(r.getBody());
            w.flush();
            w.close();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected static OAuthClient getClient(OAuthRequest request) throws Exception {
        String clientId = request.getClientId();
        String clientSecret = request.getClientSecret();
        OAuthClient client = Query.from(OAuthClient.class).where("clientId = ?", clientId).and("value = ?", clientSecret).fetchOne();
        if (client == null) {
            throw new RuntimeException("Client not found");
        }
        return null;
    }
}
