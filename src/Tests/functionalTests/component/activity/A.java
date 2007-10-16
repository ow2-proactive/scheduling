/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.component.activity;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.core.component.body.ComponentRunActive;


public class A implements ComponentInitActive, ComponentEndActive,
    ComponentRunActive, InitActive, RunActive, EndActive {
    public static String message = "";
    public static final String INIT_COMPONENT_ACTIVITY = "init-component-activity";
    public static final String RUN_COMPONENT_ACTIVITY = "run-component-activity";
    public static final String END_COMPONENT_ACTIVITY = "end-component-activity";
    public static final String INIT_FUNCTIONAL_ACTIVITY = "init-functional-activity";
    public static final String RUN_FUNCTIONAL_ACTIVITY = "run-functional-activity";
    public static final String END_FUNCTIONAL_ACTIVITY = "end-functional-activity";
    private static Lock lock = new Lock();

    public void initComponentActivity(Body body) {
        message += INIT_COMPONENT_ACTIVITY;
        lock.acquireLock(); // get the lock for the duration of the component activity
    }

    public void runComponentActivity(Body body) {
        message += RUN_COMPONENT_ACTIVITY;

        Service service = new Service(body);

        // serve startFc
        service.blockingServeOldest();

        // because the ComponentRunActive is not the default one, we have
        // to explicitely initialize, start and end the functional activity :
        initActivity(body);
        runActivity(body);
        endActivity(body);

        // serveStopFc
        service.blockingServeOldest();
    }

    public void endComponentActivity(Body body) {
        message += END_COMPONENT_ACTIVITY;
        lock.releaseLock();
    }

    public void initActivity(Body body) {
        message += INIT_FUNCTIONAL_ACTIVITY;
    }

    public void runActivity(Body body) {
        message += RUN_FUNCTIONAL_ACTIVITY;
    }

    public void endActivity(Body body) {
        message += END_FUNCTIONAL_ACTIVITY;
    }

    public static Lock getLock() {
        return lock;
    }
}
