package com.uddernetworks.paintassist.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.uddernetworks.paintassist.CustomLocalServerReceiver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DefaultAuthenticator implements Authenticator {

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
        return this.tokenInfo == null;
    }

    @Override
    public void unAuthenticate() {

    }

    @Override
    public void authenticate() {
        authenticate(x -> {}, x -> {});
    }

    @Override
    public void authenticate(Consumer<Tokeninfo> onSuccess, Consumer<Optional<Exception>> onError) {
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
            verifyToken(accessToken).ifPresentOrElse(token -> onSuccess.accept(this.tokenInfo = token), () -> onError.accept(Optional.empty()));
        } catch (Exception e) {
            e.printStackTrace();
            onError.accept(Optional.of(e));
        }
    }

    @Override
    public Tokeninfo getTokenInfo() {
        return this.tokenInfo;
    }

    private static Credential authorize() throws IOException {
        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(DefaultAuthenticator.class.getResourceAsStream("/client_secrets.json")));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(
                dataStoreFactory).build();
        return new AuthorizationCodeInstalledApp(flow, new CustomLocalServerReceiver()).authorize("user");
    }

    private static Optional<Tokeninfo> verifyToken(String accessToken) throws IOException {
        Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(accessToken).execute();
        if (!tokeninfo.getAudience().equals(clientSecrets.getDetails().getClientId())) {
            System.err.println("ERROR: audience does not match our client ID!");
            return Optional.empty();
        }

        return Optional.of(tokeninfo);
    }

    private static void userInfo() throws IOException {
        Userinfoplus userinfo = oauth2.userinfo().get().execute();
        System.out.println(userinfo.toPrettyString());
    }
}
