package com.uddernetworks.paintassist.actions;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

public interface ActionListener {

    void init() throws IOException;

    void listen(BiConsumer<List<Action>, Long> onRun);

}
