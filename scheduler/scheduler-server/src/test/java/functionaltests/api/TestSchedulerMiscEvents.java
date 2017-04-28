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
package functionaltests.api;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerStatus;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.UserType;


/**
 * This class tests the different job events of the ProActive scheduler :
 * pauseJob, resumeJob, killjob, changePriority, etc...
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestSchedulerMiscEvents extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testSchedulerMiscEvents() throws Throwable {

        Scheduler schedAdminInterface = schedulerHelper.getSchedulerInterface(UserType.ADMIN);

        log("Try many tests about scheduler state");

        assertTrue(!schedAdminInterface.start());
        assertTrue(!schedAdminInterface.resume());
        assertTrue(schedAdminInterface.stop());
        log("waiting scheduler stopped  event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.STOPPED);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.STOPPED));

        assertTrue(!schedAdminInterface.pause());
        assertTrue(!schedAdminInterface.freeze());
        assertTrue(schedAdminInterface.start());
        log("waiting scheduler started event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.STARTED);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.STARTED));

        assertTrue(schedAdminInterface.pause());
        log("waiting scheduler paused event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.PAUSED);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.PAUSED));

        assertTrue(schedAdminInterface.freeze());
        log("waiting scheduler frozen event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.FROZEN);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.FROZEN));

        assertTrue(schedAdminInterface.stop());
        log("waiting scheduler stopped  event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.STOPPED);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.STOPPED));

        assertFalse(schedAdminInterface.resume());
        //TODO resume from stopped doesn't throw any event ?

        assertTrue(schedAdminInterface.start());
        log("waiting scheduler started event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.STARTED);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.STARTED));

        assertTrue(schedAdminInterface.freeze());
        log("waiting scheduler frozen event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.FROZEN);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.FROZEN));

        assertTrue(schedAdminInterface.resume());
        log("waiting scheduler resumed  event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.RESUMED);

        assertTrue(schedAdminInterface.pause());
        log("waiting scheduler paused event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.PAUSED);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.PAUSED));

        assertTrue(schedAdminInterface.resume());
        log("waiting scheduler resumed  event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.RESUMED);

        assertTrue(!schedAdminInterface.start());
        log("waiting scheduler shutting down event");
        assertTrue(schedAdminInterface.shutdown());

        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.SHUTTING_DOWN);

        log("waiting scheduler shutted down event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.SHUTDOWN);

        schedulerHelper.killScheduler(); // to make sure other tests get a clean Scheduler
    }
}
