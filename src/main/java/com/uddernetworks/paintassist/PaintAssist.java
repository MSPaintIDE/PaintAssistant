package com.uddernetworks.paintassist;

import com.google.api.services.oauth2.model.Tokeninfo;
import com.uddernetworks.paintassist.actions.ActionListener;
import com.uddernetworks.paintassist.auth.Authenticator;

import java.util.Optional;

public interface PaintAssist {
    Optional<Tokeninfo> activate();

    Authenticator getAuthenticator();

    ActionListener getActionListener();
}
