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
package functionalTests.activeobject.request.forgetonsend;

import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.proxy.SendingQueue;

import functionalTests.FunctionalTest;
import functionalTests.GCMDeploymentReady;


/**
 * Test ForgetOnSend strategies. Must run on a single JVM
 */
@GCMDeploymentReady
public class Test extends FunctionalTest {
    A a1, a2, a3;
    SlowlySerializableObject obj1, obj4;
    String name;

    @Before
    public void action() throws Exception {
        a1 = (A) PAActiveObject.newActive(A.class.getName(), new Object[] { "A1" });
        a2 = (A) PAActiveObject.newActive(A.class.getName(), new Object[] { "A2" });
        a3 = (A) PAActiveObject.newActive(A.class.getName(), new Object[] { "A3" });

        obj1 = new SlowlySerializableObject("1", 1000);
        obj4 = new SlowlySerializableObject("4", 4000);
        PAActiveObject.setForgetOnSend("fos");

        // Check Causal Ordering
        a1.rdv(); // 1
        a2.rdv(); // 2
        a1.fos(obj1); // 3
        a1.fos(obj4); // 5
        a1.fos(obj1); // 6
        a2.fos(obj4); // 4
        a1.rdv(); // 7
        a2.rdv(); // 8

        // Check sterility constraints
        a1.sterilityCheck(a1, a2, a3);
        Thread.sleep(2000);
    }

    @org.junit.Test
    public void postConditions() throws Exception {
        // Check Causal Ordering
        String[] trace = A.getClockTicks().toArray(new String[1]);
        String[] shouldBe = new String[] { "rdv()@A1", "rdv()@A2", "fos(1)@A1", "fos(4)@A2", "fos(4)@A1",
                "fos(1)@A1", "rdv()@A1", "rdv()@A2" };
        assertTrue(Arrays.equals(trace, shouldBe));

        // Check sterility constraints
        assertTrue(A.verifySterility());
    }
}
