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
package functionalTests.hpc.exchange;

import static junit.framework.Assert.assertTrue;

import org.objectweb.proactive.api.PAActiveObject;

import functionalTests.FunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;


@GCMDeploymentReady
public class TestComplexArray extends FunctionalTestDefaultNodes {
    private B b1, b2, b3;

    public TestComplexArray() {
        super(DeploymentType._2x2);
    }

    @org.junit.Test
    public void action() throws Exception {
        b1 = (B) PAActiveObject.newActive(B.class.getName(), new Object[] {}, super.getANode());
        b2 = (B) PAActiveObject.newActive(B.class.getName(), new Object[] {}, super.getANode());
        b3 = (B) PAActiveObject.newActive(B.class.getName(), new Object[] {}, super.getANode());

        b1.start(1, b1, b2, b3);
        b2.start(2, b1, b2, b3);
        b3.start(3, b1, b2, b3);
    }

    @org.junit.After
    public void after() throws Exception {
        double cs_b1_1 = b1.getChecksum1();
        double cs_b2_1 = b2.getChecksum1();

        double cs_b2_2 = b2.getChecksum2();
        double cs_b3_2 = b3.getChecksum2();

        assertTrue(cs_b1_1 == cs_b2_1);
        assertTrue(cs_b2_2 == cs_b3_2);
    }
}
