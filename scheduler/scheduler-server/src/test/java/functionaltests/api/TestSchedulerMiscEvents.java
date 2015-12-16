/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.api;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerStatus;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.UserType;


/**
 * This class tests the different job events of the ProActive scheduler :
 * pauseJob, resumeJob, killjob, changePriority, etc...
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestSchedulerMiscEvents extends SchedulerFunctionalTest {

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
