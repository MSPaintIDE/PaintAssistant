package com.uddernetworks.paintassist.auth;

import com.google.api.services.oauth2.model.Tokeninfo;

import java.util.Optional;
import java.util.function.Consumer;

public interface Authenticator {

    boolean isAuthenticated();

    void unAuthenticate();

    void authenticate();

    void authenticate(Consumer<Tokeninfo> onSuccess, Consumer<Optional<Exception>> onError);

    Tokeninfo getTokenInfo();
}
