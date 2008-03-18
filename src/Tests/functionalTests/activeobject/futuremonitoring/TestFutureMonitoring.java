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
package functionalTests.activeobject.futuremonitoring;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.exceptions.FutureMonitoringPingFailureException;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;


/**
 * Test monitoring the futures
 */
@GCMDeploymentReady
public class TestFutureMonitoring extends FunctionalTestDefaultNodes {

    public TestFutureMonitoring() {
        super(DeploymentType._4x1);
    }

    @Test
    public void action() throws Exception {
        Node node1 = super.getANode();
        Node node2 = super.getANode();
        Node node3 = super.getANode();

        // With AC
        boolean exception = false;
        A a1 = (A) PAActiveObject.newActive(A.class.getName(), null, node1);
        A future = a1.sleepForever();
        A a2 = (A) PAActiveObject.newActive(A.class.getName(), null, node2);
        A ac = a2.wrapFuture(future);
        a2.crash();
        try {
            //System.out.println(ac);
            ac.toString();
        } catch (FutureMonitoringPingFailureException fmpfe) {
            exception = true;
        }
        assertTrue(exception);

        // With AC and Terminate AO
        boolean exceptionT = false;
        A a1T = (A) PAActiveObject.newActive(A.class.getName(), null, node1);
        A futureT = a1T.sleepForever();
        A a2T = (A) PAActiveObject.newActive(A.class.getName(), null, node3);
        A acT = a2T.wrapFuture(futureT);
        a2T.crashWithTerminate();
        try {
            //System.out.println(ac);
            acT.toString();
        } catch (FutureMonitoringPingFailureException fmpfe) {
            exceptionT = true;
        }
        assertTrue(exceptionT);

        // Without AC
        exception = false;
        A a1bis = (A) PAActiveObject.newActive(A.class.getName(), null, node1);
        a1bis.crash();
        try {
            //System.out.println(future);
            future.toString();
        } catch (FutureMonitoringPingFailureException fmpfe) {
            exception = true;
        }
        assertTrue(exception);
    }
}
