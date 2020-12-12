/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common;

import org.junit.Assert;
import org.junit.Test;


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
    public void testSchedulerEvent() throws Throwable {
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
        Assert.assertEquals(SchedulerEvent.TASK_PROGRESS.ordinal(), 25);
        Assert.assertEquals(SchedulerEvent.DB_DOWN.ordinal(), 26);
        Assert.assertEquals(SchedulerEvent.JOB_IN_ERROR.ordinal(), 27);
        Assert.assertEquals(SchedulerEvent.TASK_IN_ERROR.ordinal(), 28);
        Assert.assertEquals(SchedulerEvent.JOB_RESTARTED_FROM_ERROR.ordinal(), 29);
        Assert.assertEquals(SchedulerEvent.JOB_UPDATED.ordinal(), 30);
        Assert.assertEquals(SchedulerEvent.TASK_IN_ERROR_TO_FINISHED.ordinal(), 31);
        Assert.assertEquals(SchedulerEvent.JOB_RUNNING_TO_FINISHED_WITH_ERRORS.ordinal(), 32);
        Assert.assertEquals(SchedulerEvent.JOB_ABORTED.ordinal(), 33);
        Assert.assertEquals(SchedulerEvent.TASK_VISU_ACTIVATED.ordinal(), 34);

        Assert.assertEquals(35, SchedulerEvent.values().length);
    }

}
