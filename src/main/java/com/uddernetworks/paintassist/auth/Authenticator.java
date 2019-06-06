package com.uddernetworks.paintassist.auth;

import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;

import java.util.Optional;
import java.util.function.Consumer;

public interface Authenticator {

    boolean isAuthenticated();

    void unAuthenticate();

    Optional<Tokeninfo> authenticate();

    Optional<Tokeninfo> getTokenInfo();

    Oauth2 getOAuth2();
}
