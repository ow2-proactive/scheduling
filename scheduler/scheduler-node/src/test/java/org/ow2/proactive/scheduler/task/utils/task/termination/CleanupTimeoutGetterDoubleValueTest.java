package org.ow2.proactive.scheduler.task.utils.task.termination;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import org.junit.Test;


public class CleanupTimeoutGetterDoubleValueTest {

    private static final long CLEANUP_TIME_DEFAULT_SECONDS = 10;

    @Test
    public void testThatDefaultTimeoutIsReturnedDoubled() {
        CleanupTimeoutGetter cleanupTimeoutGetter =
                new CleanupTimeoutGetterDoubleValue();
        assertThat(cleanupTimeoutGetter.getCleanupTimeSeconds(), is(CLEANUP_TIME_DEFAULT_SECONDS*2));
    }
}