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
package functionaltests.topology;

import functionaltests.utils.RMFunctionalTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import java.io.File;
import java.util.Collection;

/**
 * Local version of SelectionTest which tests only a few scenarios and verify
 * that they work when topology distance is disabled
 **/
public class LocalSelectionTest extends RMFunctionalTest {

    private ResourceManager resourceManager = null;

    private static final int NODE_NUMBER = 4;

    private void getNodesAndReleaseThem(int number, TopologyDescriptor descriptor, int expectedReceived, int expectedExtraNodesSize) {
        Criteria c = new Criteria(number);
        c.setTopology(descriptor);
        NodeSet ns = resourceManager.getNodes(c);
        Assert.assertEquals(expectedReceived, ns.size());
        Collection<Node> extra = ns.getExtraNodes();
        if (expectedExtraNodesSize == 0) {
            Assert.assertNull(extra);
        } else {
            Assert.assertEquals(expectedExtraNodesSize, extra.size());
        }
        resourceManager.releaseNodes(ns).getBooleanValue();
    }

    @Before
    public void getRM() throws Exception {
        String rmconf = new File(
                PAResourceManagerProperties.getAbsolutePath(getClass()
                        .getResource(
                                "/functionaltests/config/functionalTRMPropertiesWithTopology.ini")
                        .getFile())).getAbsolutePath();
        rmHelper.startRM(rmconf);

        resourceManager = rmHelper.getResourceManager();

    }

    @After
    public void removeNS() throws Exception {
        if (resourceManager != null) {
            try {
                resourceManager.removeNodeSource(this.getClass().getSimpleName(), true);
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void action() throws Exception {
        Assert.assertTrue("Topology must be enabled", PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean());
        Assert.assertTrue("Ressource manager must be deployed without nodes", resourceManager.getState().getFreeNodesNumber() == 0);

        rmHelper.createNodeSource(this.getClass().getSimpleName(), NODE_NUMBER);

        int counter = 0;
        while (resourceManager.getState().getFreeNodesNumber() < NODE_NUMBER) {
            Thread.sleep(1000);
            counter++;
            Assert.assertTrue("Node source must be deployed", counter < 30);
        }

        getNodesAndReleaseThem(1, TopologyDescriptor.ARBITRARY, 1, 0);
        getNodesAndReleaseThem(NODE_NUMBER, TopologyDescriptor.ARBITRARY, NODE_NUMBER, 0);
        getNodesAndReleaseThem(100, TopologyDescriptor.ARBITRARY, NODE_NUMBER, 0);

        getNodesAndReleaseThem(1, TopologyDescriptor.SINGLE_HOST, 1, 0);
        getNodesAndReleaseThem(NODE_NUMBER, TopologyDescriptor.SINGLE_HOST, NODE_NUMBER, 0);
        getNodesAndReleaseThem(100, TopologyDescriptor.SINGLE_HOST, NODE_NUMBER, 0);

        getNodesAndReleaseThem(1, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, 1, NODE_NUMBER - 1);
        getNodesAndReleaseThem(NODE_NUMBER - 1, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, NODE_NUMBER - 1, 1);
        getNodesAndReleaseThem(NODE_NUMBER, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, NODE_NUMBER, 0);
        getNodesAndReleaseThem(100, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, NODE_NUMBER, 0);

        getNodesAndReleaseThem(1, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, 1, NODE_NUMBER - 1);
        getNodesAndReleaseThem(NODE_NUMBER - 1, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, NODE_NUMBER - 1, 1);
        getNodesAndReleaseThem(NODE_NUMBER, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, NODE_NUMBER, 0);
        getNodesAndReleaseThem(100, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, NODE_NUMBER, 0);

        getNodesAndReleaseThem(1, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, 1, NODE_NUMBER - 1);
        getNodesAndReleaseThem(NODE_NUMBER - 1, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, NODE_NUMBER - 1, 1);
        getNodesAndReleaseThem(NODE_NUMBER, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, NODE_NUMBER, 0);
        getNodesAndReleaseThem(100, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, NODE_NUMBER, 0);

        getNodesAndReleaseThem(1, TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, 1, NODE_NUMBER - 1);
        getNodesAndReleaseThem(2, TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, 1, NODE_NUMBER - 1);
        getNodesAndReleaseThem(100, TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, 1, NODE_NUMBER - 1);

        getNodesAndReleaseThem(1, TopologyDescriptor.BEST_PROXIMITY, 1, 0);
        getNodesAndReleaseThem(NODE_NUMBER, TopologyDescriptor.BEST_PROXIMITY, NODE_NUMBER, 0);
        getNodesAndReleaseThem(100, TopologyDescriptor.BEST_PROXIMITY, NODE_NUMBER, 0);

    }
}
