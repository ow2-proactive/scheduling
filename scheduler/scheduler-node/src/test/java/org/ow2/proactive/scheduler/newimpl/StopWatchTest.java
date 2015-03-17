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

        Thread.sleep(1);

        long duration = watch.stop();
        assertNotEquals(0, duration);

        assertEquals(duration, watch.stop());
    }


}