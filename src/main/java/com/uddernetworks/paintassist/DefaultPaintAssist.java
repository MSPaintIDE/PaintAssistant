package com.uddernetworks.paintassist;

import com.google.api.services.oauth2.model.Tokeninfo;
import com.uddernetworks.paintassist.actions.ActionListener;
import com.uddernetworks.paintassist.actions.DefaultActionsListener;
import com.uddernetworks.paintassist.auth.Authenticator;
import com.uddernetworks.paintassist.auth.DefaultAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class DefaultPaintAssist implements PaintAssist {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultPaintAssist.class);

    private Authenticator authenticator;
    private ActionListener actionListener;

    public DefaultPaintAssist() {
        this.authenticator = new DefaultAuthenticator();
        this.actionListener = new DefaultActionsListener(this);
    }

    @Override
    public Optional<Tokeninfo> activate() {
        var tokenInfo = this.authenticator.authenticate();
        if (tokenInfo.isEmpty()) {
            LOGGER.error("There was a problem during authorization");
            return Optional.empty();
        }

        try {
            this.actionListener.init();
        } catch (IOException e) {
            LOGGER.error("There was a problem initializing the ActionListener", e);
            return Optional.empty();
        }

        return tokenInfo;
    }

    @Override
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public ActionListener getActionListener() {
        return actionListener;
    }
}
