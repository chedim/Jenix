package com.onkiup.jendri.models.user;

import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.access.AuthCookie;
import com.onkiup.jendri.access.AuthService;
import com.onkiup.jendri.access.User;
import com.onkiup.jendri.api.DynamicObject;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.session.Cookies;
import com.onkiup.jendri.util.StringUtils;
import org.apache.commons.io.IOUtils;

public class CurrentUser implements DynamicObject {

    private Boolean loggedIn = false;
    private Profile profile;

    public CurrentUser() throws Exception {
        HttpServletRequest request = Cookies.getRequest();
        User user = AuthService.getUser(Cookies.getRequest(), Cookies.getResponse());
        if (user == null) {
            if (!request.getMethod().equals("PUT")) {
                Cookies.getResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                StringWriter w = new StringWriter();
                IOUtils.copy(request.getInputStream(), w);
                String userIdentifier = w.toString();
                if (!StringUtils.isEmpty(userIdentifier)){
                    user = new User(userIdentifier, new ArrayList<>());
                    profile = new Profile();
                    profile.setOwner(user);
                    profile.saveImmediately();
                    AuthService.setUser(user, Cookies.getRequest(), Cookies.getResponse());
                } else {
                    Cookies.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        } else {
            if (request.getMethod().equals("DELETE")) {
                AuthService.dropUser(request, Cookies.getResponse());
            } else {
                profile = Query.from(Profile.class).where("owner = ?", user).fetchOne();
                loggedIn = true;
            }
        }
    }
}
