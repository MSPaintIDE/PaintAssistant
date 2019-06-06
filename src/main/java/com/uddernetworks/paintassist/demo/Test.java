package com.uddernetworks.paintassist.demo;

import com.uddernetworks.paintassist.DefaultPaintAssist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {

    private static Logger LOGGER = LoggerFactory.getLogger(Test.class);

    // TODO: Remove this class
    public static void main(String[] args) throws InterruptedException {
        var paintAssist = new DefaultPaintAssist();
        paintAssist.activate();

        paintAssist.getActionListener().listen((actions, time) -> LOGGER.info("Running " + actions));

        Thread.sleep(900_000);
    }

}
