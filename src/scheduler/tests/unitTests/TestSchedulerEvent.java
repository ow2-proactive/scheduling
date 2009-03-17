package unitTests;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerEvent;


/**
 * TestSchedulerEvent tests that the scheduler event ordinal have not changed.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestSchedulerEvent {

    @Test
    public void run() throws Throwable {
        Assert.assertEquals(SchedulerEvent.FROZEN.ordinal(), 0);
        Assert.assertEquals(SchedulerEvent.RESUMED.ordinal(), 1);
        Assert.assertEquals(SchedulerEvent.SHUTDOWN.ordinal(), 2);
        Assert.assertEquals(SchedulerEvent.SHUTTING_DOWN.ordinal(), 3);
        Assert.assertEquals(SchedulerEvent.STARTED.ordinal(), 4);
        Assert.assertEquals(SchedulerEvent.STOPPED.ordinal(), 5);
        Assert.assertEquals(SchedulerEvent.KILLED.ordinal(), 6);
        Assert.assertEquals(SchedulerEvent.JOB_PAUSED.ordinal(), 7);
        Assert.assertEquals(SchedulerEvent.JOB_PENDING_TO_RUNNING.ordinal(), 8);
        Assert.assertEquals(SchedulerEvent.JOB_RESUMED.ordinal(), 9);
        Assert.assertEquals(SchedulerEvent.JOB_SUBMITTED.ordinal(), 10);
        Assert.assertEquals(SchedulerEvent.JOB_RUNNING_TO_FINISHED.ordinal(), 11);
        Assert.assertEquals(SchedulerEvent.JOB_REMOVE_FINISHED.ordinal(), 12);
        Assert.assertEquals(SchedulerEvent.TASK_PENDING_TO_RUNNING.ordinal(), 13);
        Assert.assertEquals(SchedulerEvent.TASK_RUNNING_TO_FINISHED.ordinal(), 14);
        Assert.assertEquals(SchedulerEvent.TASK_WAITING_FOR_RESTART.ordinal(), 15);
        Assert.assertEquals(SchedulerEvent.JOB_CHANGE_PRIORITY.ordinal(), 16);
        Assert.assertEquals(SchedulerEvent.PAUSED.ordinal(), 17);
        Assert.assertEquals(SchedulerEvent.RM_DOWN.ordinal(), 18);
        Assert.assertEquals(SchedulerEvent.RM_UP.ordinal(), 19);
        Assert.assertEquals(SchedulerEvent.USERS_UPDATE.ordinal(), 20);
        Assert.assertEquals(SchedulerEvent.POLICY_CHANGED.ordinal(), 21);
        Assert.assertEquals(22, SchedulerEvent.values().length);
    }

}
