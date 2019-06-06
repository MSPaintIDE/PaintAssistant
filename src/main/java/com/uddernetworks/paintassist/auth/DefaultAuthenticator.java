package com.uddernetworks.paintassist.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.uddernetworks.paintassist.CustomLocalServerReceiver;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DefaultAuthenticator implements Authenticator {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticator.class);

    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "MS-Paint-IDE/1.0";

    /**
     * Directory to store user credentials.
     */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/oauth2_sample");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport httpTransport;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * OAuth 2.0 scopes.
     */
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");

    private static Oauth2 oauth2;
    private static GoogleClientSecrets clientSecrets;
    private Tokeninfo tokenInfo;

    @Override
    public boolean isAuthenticated() {
        return this.tokenInfo != null;
    }

    @Override
    public void unAuthenticate() {
        if (tokenInfo != null) this.tokenInfo.clear();
        this.tokenInfo = null;

        try {
            FileUtils.deleteDirectory(DATA_STORE_DIR);
        } catch (IOException e) {
            LOGGER.error("There was a problem removing the data store directory", e);
        }
    }

    @Override
    public Optional<Tokeninfo> authenticate() {
        return authenticate(0);
    }

    public Optional<Tokeninfo> authenticate(int attempt) {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            // authorization
            var credential = authorize();
            // set up global Oauth2 instance
            oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                    APPLICATION_NAME).build();
            // run commands
            String accessToken = credential.getAccessToken();

            try {
                var tokenOptional = verifyToken(accessToken);
                tokenOptional.ifPresent(tokenInfo -> this.tokenInfo = tokenInfo);
                return tokenOptional;
            } catch (GoogleJsonResponseException e) {
                if (e.getMessage().equals("400 Bad Request")) {
                    LOGGER.warn("Authentication failed with a 400, trying again...");
                    if (attempt == 3) {
                        LOGGER.error("There was an error during authorization", e);
                        return Optional.empty();
                    }
                    unAuthenticate();
                    return authenticate(++attempt);
                }
            }
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.error("There was an error during authorization", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Tokeninfo> getTokenInfo() {
        return Optional.ofNullable(this.tokenInfo);
    }

    @Override
    public Oauth2 getOAuth2() {
        return oauth2;
    }

    private static Credential authorize() throws IOException {
        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(DefaultAuthenticator.class.getResourceAsStream("/client_secrets.json")));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(
                dataStoreFactory).build();
        return new AuthorizationCodeInstalledApp(flow, new CustomLocalServerReceiver()).authorize("user");
    }

    private Optional<Tokeninfo> verifyToken(String accessToken) throws IOException {
        Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(accessToken).execute();
        if (!tokeninfo.getAudience().equals(clientSecrets.getDetails().getClientId())) {
            System.err.println("ERROR: audience does not match our client ID!");
            return Optional.empty();
        }

        return Optional.of(tokeninfo);
    }
}
