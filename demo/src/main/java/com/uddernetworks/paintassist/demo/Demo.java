package com.uddernetworks.paintassist.demo;

import com.uddernetworks.paintassist.DefaultPaintAssist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Demo {
    private static Logger LOGGER = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) throws InterruptedException {
        LOGGER.info("\n\nThis is a test of MS Paint IDE Google Assistant integration. Your browser will be opened asking you to sign in.\n\n");
        var paintAssist = new DefaultPaintAssist();
        paintAssist.activate();

        paintAssist.getActionListener().listen((actions, time) -> LOGGER.info("Received the following actions: " + actions));

        Thread.sleep(500);
        System.out.println("\n\n\n\n");
        LOGGER.info("Now that you are logged in, you can ask google \"Hey Google, talk to MS Paint IDE\" and then \"Log me in\" if you aren't already logged in. Finally, say \"Compile my code\" or another variation of action, and below you will see the results.");

        Thread.sleep(TimeUnit.DAYS.toMillis(1));
    }
}
