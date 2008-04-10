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
package functionalTests.activeobject.creation.parallel;

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;

import functionalTests.GCMDeploymentReady;
import functionalTests.GCMFunctionalTestDefaultNodes;
import functionalTests.activeobject.creation.A;


/**
 * Test newActiveInParallel method with an array for constructor parameters
 * 
 * @author The ProActive Team
 * 
 * Created on Nov 8, 2005
 */
@GCMDeploymentReady
public class TestArrayOfArray extends GCMFunctionalTestDefaultNodes {
    Node[] nodes = null;

    public TestArrayOfArray() {
        super(DeploymentType._2x2);
    }

    @Before
    public void before() {
        if (nodes == null) {
            nodes = new Node[4];
            for (int i = 0; i < DeploymentType._2x2.size; i++) {
                nodes[i] = super.getANode();
            }
        }
    }

    /*
     * Test if newActiveInParallel verify that the total of constructors is equal to the total of
     * nodes.
     */
    @Test(expected = Exception.class)
    public void testNbConstructorsEqualsNbNodes() throws Exception {
        PAActiveObject.newActiveInParallel(A.class.getName(), new Object[][] { { "toto" }, { "tata" } },
                nodes);
    }

    @Test
    public void testNewActiveInParallel() throws Exception {
        A[] aos = (A[]) PAActiveObject.newActiveInParallel(A.class.getName(), new Object[][] { { "toto" },
                { "tata" }, { "titi" }, { "tutu" } }, nodes);
        assertTrue(aos != null);
        assertTrue(aos.length == 4);
    }
}
