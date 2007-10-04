package org.objectweb.proactive.ic2d.jmxmonitoring.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class IC2DThreadPool {
    private static Executor tpe = Executors.newCachedThreadPool();

    public static void execute(Runnable command) {
        tpe.execute(command);
    }
}
