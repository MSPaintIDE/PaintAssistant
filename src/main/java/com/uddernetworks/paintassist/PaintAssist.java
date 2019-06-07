package com.uddernetworks.paintassist;

import com.google.api.services.oauth2.model.Tokeninfo;
import com.uddernetworks.paintassist.actions.ActionListener;
import com.uddernetworks.paintassist.auth.Authenticator;

import java.util.Optional;

public interface PaintAssist {

    /**
     * Authenticates via {@link Authenticator#authenticate()} and initializes via {@link ActionListener#init()} the
     * current client.
     *
     * @return The {@link Tokeninfo} generated if successful
     */
    Optional<Tokeninfo> activate();

    /**
     * Gets the {@link Authenticator} used
     *
     * @return The {@link Authenticator}
     */
    Authenticator getAuthenticator();

    /**
     * Gets the {@link ActionListener} used
     *
     * @return The {@link ActionListener}
     */
    ActionListener getActionListener();
}
