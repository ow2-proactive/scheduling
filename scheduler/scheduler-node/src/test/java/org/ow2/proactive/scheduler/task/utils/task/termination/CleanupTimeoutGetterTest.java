package org.ow2.proactive.scheduler.task.utils.task.termination;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;

public class CleanupTimeoutGetterTest {

    private static final long CLEANUP_TIME_DEFAULT_SECONDS = 10;
    private static final String CLEANUP_TIME_PROPERTY_NAME = RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME;

    @Test
    public void testThatDefaultTimeoutIsReturnedIfNoPropertyIsSet() {
        testThatReturnedTimeoutIs(CLEANUP_TIME_DEFAULT_SECONDS);
    }

    @Test
    public void testThatDefaultTimeoutIsReturnedIfPropertyIsSetToGarbage() {
        System.setProperty(CLEANUP_TIME_PROPERTY_NAME, "bahasd3342");
        testThatReturnedTimeoutIs(CLEANUP_TIME_DEFAULT_SECONDS);
        System.clearProperty(CLEANUP_TIME_PROPERTY_NAME);
    }

    @Test
    public void testThatCorrectValueIsReturnedIfProperyIsSet() {
        System.setProperty(CLEANUP_TIME_PROPERTY_NAME, "15");
        testThatReturnedTimeoutIs(15L);
        System.clearProperty(CLEANUP_TIME_PROPERTY_NAME);
    }

    private void testThatReturnedTimeoutIs(long expectedTimeout) {
        CleanupTimeoutGetter cleanupTimeoutGetter =
                new CleanupTimeoutGetter();
        assertThat(cleanupTimeoutGetter.getCleanupTimeSeconds(), is(expectedTimeout));
    }

}