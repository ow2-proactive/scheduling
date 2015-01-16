package org.ow2.proactive.scheduler.task.utils;

import org.junit.Test;

import static org.junit.Assert.*;


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