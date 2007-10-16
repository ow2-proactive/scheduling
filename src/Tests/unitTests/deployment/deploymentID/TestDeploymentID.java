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
package unitTests.deployment.deploymentID;

import java.rmi.dgc.VMID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.core.DeploymentID;


public class TestDeploymentID {
    String[] nodes;
    VMID vmid;

    @Before
    public void config() {
        vmid = new VMID();
        nodes = new String[] { "node1", "node2", "node3" };
    }

    @Test
    public void testDeploymentIDConstructor() {
        DeploymentID depID = new DeploymentID(vmid);
        for (String nodeStr : nodes) {
            depID.addDepNode(nodeStr);
        }

        Assert.assertEquals(vmid.toString(), depID.getDeplpoyerVMID());
        for (int i = 0; i < nodes.length; i++) {
            Assert.assertEquals(nodes[i], depID.getDepNode(i));
        }

        DeploymentID depID2 = new DeploymentID(depID.toString());
        Assert.assertEquals(depID, depID2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeException() {
        DeploymentID depID = new DeploymentID(vmid);
        depID.addDepNode("toto" + DeploymentID.SEPARATOR + "tata");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException() {
        new DeploymentID(vmid.toString());
    }
}
