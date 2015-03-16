package org.ow2.proactive.scheduler.util;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

public class Watch {

    private Stopwatch watch;

    private Watch() {
        watch = Stopwatch.createStarted();
    }

    public static Watch startNewWatch() {
        return new Watch();
    }

    public long elapsedMilliseconds() {
        return watch.elapsed(TimeUnit.MILLISECONDS);
    }

    public Watch getCopy() {
        Watch newInstance = new Watch();
        newInstance.watch = this.watch;
        return newInstance;
    }

}
