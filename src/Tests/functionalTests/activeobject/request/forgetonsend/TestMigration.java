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

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;


/**
 * When migrating an Active Object, the migration should wait until the SendingQueue is empty to
 * avoid multiple and un-useful serializations. 
 */
@GCMDeploymentReady
public class TestMigration extends FunctionalTestDefaultNodes {

    private C c1, c2, c3, c4;

    public TestMigration() {
        super(DeploymentType._4x1);
    }

    @Test
    public void migration() throws Exception {

        Node node1 = super.getANode();
        Node node2 = super.getANode();
        Node node3 = super.getANode();
        Node node4 = super.getANode();

        c1 = (C) PAActiveObject.newActive(C.class.getName(), new Object[] { "C1" }, node1);
        c2 = (C) PAActiveObject.newActive(C.class.getName(), new Object[] { "C2" }, node2);
        c3 = (C) PAActiveObject.newActive(C.class.getName(), new Object[] { "C3" }, node3);
        c4 = (C) PAActiveObject.newActive(C.class.getName(), new Object[] { "C4" }, node4);

        int r1 = c1.getRuntimeHashCode();
        int r2 = c2.getRuntimeHashCode();
        int r3 = c3.getRuntimeHashCode();
        int r4 = c4.getRuntimeHashCode();

        c1.sendTwoFos(c2);
        c1.moveTo(node3);

        Thread.sleep(10000);

        // Check Migration
        assertTrue(c2.getFooASerializer() == r1); // fooA should be sent from node1
        assertTrue(c2.getFooBSerializer() == r1); // fooB should be sent from node1 too
        assertTrue(c2.getServices().equals("ab")); // Check FIFO
    }
}