package org.ow2.proactive.scheduler.newimpl;

import org.ow2.proactive.scheduler.newimpl.utils.StopWatch;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class StopWatchTest {

    @Test
    public void test() throws Exception {
        StopWatch watch = new StopWatch();

        assertEquals(0, watch.stop());

        watch.start();

        // The precision of StopWatch is operating system dependent. Because it uses System.System.nanoTime()
        // Which is at least as precise as currentTimeMillis(), which is operating system dependent.
        // Therefore sleep should at least be several 10's of milliseconds
        Thread.sleep(50);

        long duration = watch.stop();
        assertNotEquals(0, duration);

        assertEquals(duration, watch.stop());
    }

}