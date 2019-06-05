package com.uddernetworks.paintassist.actions;

import java.io.IOException;
import java.util.function.BiConsumer;

public interface ActionListener {

    void init() throws IOException;

    void listen(BiConsumer<Action, Long> onRun, Action... actions);

}
