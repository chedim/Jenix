package com.onkiup.jendri.access.oauth.scope;

public interface UserScopes {
    class Credentials implements OAuthScope {
        @Override
        public String getExplanation() {
            return "Access to your login";
        }
    }

    class Profile implements OAuthScope {

        @Override
        public String getExplanation() {
            return "Access to your profile";
        }
    }
}
