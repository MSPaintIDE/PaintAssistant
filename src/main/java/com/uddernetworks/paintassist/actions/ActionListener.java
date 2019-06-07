package com.uddernetworks.paintassist.actions;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

public interface ActionListener {

    /**
     * Initializes to and connects to the firebase database. Requires authentication via
     * {@link com.uddernetworks.paintassist.auth.Authenticator} beforehand.
     *
     * @throws IOException If an IO exception occurs
     */
    void init() throws IOException;

    /**
     * Adds a listener to activate when database changes happen.
     *
     * @param onRun The BiConsumer invoked when database changes happening, the parameters being the {@link Action}s
     *              being ran, and the time they were added to by the server in milliseconds.
     */
    void listen(BiConsumer<List<Action>, Long> onRun);

    /**
     * Clears all listeners added by {@link ActionListener#listen(BiConsumer)}.
     */
    void clearListeners();

}
