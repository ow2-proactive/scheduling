/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package unitTests;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerEvent;


/**
 * TestSchedulerEvent tests that the scheduler event ordinal have not been changed.<br>
 * The ordinal of the SchedulerEvent class must not changed as it is used by user.
 * Changing this ordinal may imply many bugs in client implementation that already use the API. 
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
        Assert.assertEquals(SchedulerEvent.JOB_PENDING_TO_FINISHED.ordinal(), 22);
        Assert.assertEquals(SchedulerEvent.TASK_REPLICATED.ordinal(), 23);
        Assert.assertEquals(SchedulerEvent.TASK_SKIPPED.ordinal(), 24);
        Assert.assertEquals(25, SchedulerEvent.values().length);
    }

}
