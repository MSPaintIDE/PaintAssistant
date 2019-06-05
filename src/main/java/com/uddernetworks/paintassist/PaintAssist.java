package com.uddernetworks.paintassist;

import com.uddernetworks.paintassist.actions.Action;
import com.uddernetworks.paintassist.actions.ActionListener;
import com.uddernetworks.paintassist.actions.DefaultActionsListener;
import com.uddernetworks.paintassist.auth.Authenticator;
import com.uddernetworks.paintassist.auth.DefaultAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PaintAssist {

    private static Logger LOGGER = LoggerFactory.getLogger(PaintAssist.class);

    private Authenticator authenticator;
    private ActionListener actionListener;

    public PaintAssist() {
        this.authenticator = new DefaultAuthenticator();
        this.actionListener = new DefaultActionsListener(this);
    }

    public void activate() {
        this.authenticator.authenticate(tokenInfo -> {
            try {
                this.actionListener.init();
                this.actionListener.listen((action, time) -> {
                    LOGGER.info("Running " + action.getDatabaseName());
                }, Action.STOP, Action.HIGHLIGHT, Action.COMPILE, Action.RUN);
            } catch (IOException e) {
                LOGGER.error("There was a problem initializing the ActionListener", e);
            }
        }, errorOptional -> errorOptional.ifPresentOrElse(
                e -> LOGGER.error("There was an error authenticating", e),
                () -> LOGGER.error("There was an error authenticating.")));
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public ActionListener getActionListener() {
        return actionListener;
    }
}
