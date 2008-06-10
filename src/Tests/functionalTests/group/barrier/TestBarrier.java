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
package functionalTests.group.barrier;

import static junit.framework.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;


/**
 * perform a barrier call on an SPMD group
 *
 * @author The ProActive Team
 */
@GCMDeploymentReady
public class TestBarrier extends GCMFunctionalTestDefaultNodes {
    private A spmdgroup = null;

    public TestBarrier() {
        super(2, 1);
    }

    @Before
    public void preConditions() throws Exception {
        //@snippet-start spmd_creation
        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { NodeFactory.getDefaultNode(), super.getANode(), super.getANode() };
        this.spmdgroup = (A) PASPMD.newSPMDGroup(A.class.getName(), params, nodes);
        //@snippet-end spmd_creation
        assertTrue(spmdgroup != null);
        assertTrue(PAGroup.size(spmdgroup) == 3);
    }

    @org.junit.Test
    public void action() throws Exception {
        this.spmdgroup.start();

        String errors = "";
        Iterator<A> it = PAGroup.getGroup(this.spmdgroup).iterator();
        while (it.hasNext()) {
            errors += ((A) it.next()).getErrors();
        }
        System.err.print(errors);
        assertTrue("".equals(errors));
    }
}
