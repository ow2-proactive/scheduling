/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionnaltests;

import static junit.framework.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerEvent;


/**
 * This class tests the different job events of the ProActive scheduler :
 * pauseJob, resumeJob, killjob, changePriority, etc...
 *
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive 4.0
 */
public class TestSchedulerMiscEvents extends FunctionalTDefaultScheduler {

    private SchedulerEventReceiver receiver = null;
    private AdminSchedulerInterface schedAdminInterface;

    /**
     *  Starting and linking new scheduler ! <br/>
     *  This method will join a new scheduler and connect it as user.<br/>
     *  Then, it will register an event receiver to check the dispatched event.
     */
    @Before
    public void preRun() throws Exception {
        //Create an Event receiver AO in order to observe jobs and tasks states changes
        receiver = (SchedulerEventReceiver) PAActiveObject.newActive(SchedulerEventReceiver.class.getName(),
                new Object[] {});
        schedUserInterface.disconnect();
        //Log as admin
        schedAdminInterface = schedulerAuth.logAsAdmin(username, password);
        //Register as EventListener AO previously created
        schedAdminInterface.addSchedulerEventListener(receiver, SchedulerEvent.FROZEN, SchedulerEvent.KILLED,
                SchedulerEvent.PAUSED, SchedulerEvent.RESUMED, SchedulerEvent.SHUTTING_DOWN,
                SchedulerEvent.SHUTDOWN, SchedulerEvent.STARTED, SchedulerEvent.STOPPED);
    }

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        log("Try many tests about scheduler status");
        assertTrue(!schedAdminInterface.start().booleanValue());
        assertTrue(!schedAdminInterface.resume().booleanValue());
        assertTrue(schedAdminInterface.stop().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.STOPPED);
        assertTrue(!schedAdminInterface.pause().booleanValue());
        assertTrue(!schedAdminInterface.freeze().booleanValue());
        assertTrue(schedAdminInterface.start().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.STARTED);
        assertTrue(schedAdminInterface.pause().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.PAUSED);
        assertTrue(schedAdminInterface.freeze().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.FROZEN);
        assertTrue(schedAdminInterface.stop().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.STOPPED);
        assertTrue(!schedAdminInterface.resume().booleanValue());
        assertTrue(schedAdminInterface.start().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.STARTED);
        assertTrue(schedAdminInterface.freeze().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.FROZEN);
        assertTrue(schedAdminInterface.resume().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.RESUMED);
        assertTrue(schedAdminInterface.pause().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.PAUSED);
        assertTrue(schedAdminInterface.resume().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.RESUMED);
        assertTrue(!schedAdminInterface.start().booleanValue());
        assertTrue(schedAdminInterface.shutdown().booleanValue());
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.SHUTTING_DOWN);
        receiver.waitForNEvent(1);
        receiver.checkLastMiscEvents(SchedulerEvent.SHUTDOWN);
    }

    /**
     * Disconnect the scheduler.
     *
     * @throws Exception if an error occurred
     */
    @After
    public void afterTestJobSubmission() throws Exception {
    }

}
