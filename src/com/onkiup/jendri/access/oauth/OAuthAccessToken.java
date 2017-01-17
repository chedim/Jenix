package com.onkiup.jendri.access.oauth;

import com.onkiup.jendri.access.User;
import com.onkiup.jendri.db.Query;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;

public class OAuthAccessToken extends OAuthToken {
    public OAuthAccessToken() {
        super();
    }

    public OAuthAccessToken(User user) throws Exception {
        super(user);
    }

    public static OAuthAccessToken from(OAuthAccessResourceRequest req) throws Exception {
        String value = req.getAccessToken();
        return Query.from(OAuthAccessToken.class).where("value = ?", value).fetchOne();
    }
}
