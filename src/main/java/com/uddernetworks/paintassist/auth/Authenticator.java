package com.uddernetworks.paintassist.auth;

import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;

import java.util.Optional;

public interface Authenticator {

    /**
     * Gets if the client has been authenticated.
     *
     * @return If the client has been authenticated
     */
    boolean isAuthenticated();

    /**
     * Resets authentication and deletes local token cache files
     */
    void unAuthenticate();

    /**
     * Attempts to authenticate the use, returning the {@link Tokeninfo} if successful. If presented with a code 400
     * error for whatever reason, it will try 3 times total. If all times fail, it will accept its fate and return an
     * empty {@link Optional} and spit an error message in console.
     *
     * @return The {@link Tokeninfo} of the client if successful
     */
    Optional<Tokeninfo> authenticate();

    /**
     * Gets the {@link Tokeninfo} made by invoking {@link Authenticator#authenticate()} previously, if existent.
     *
     * @return The {@link Tokeninfo} previously generated
     */
    Optional<Tokeninfo> getTokenInfo();

    /**
     * Gets the {@link Oauth2} used internally.
     *
     * @return The {@link Oauth2} used
     */
    Oauth2 getOAuth2();
}
