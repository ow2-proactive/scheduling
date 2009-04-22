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
package functionaltests.nodesource;

import static junit.framework.Assert.assertTrue;

import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMCustomisedInfrastructure;
import org.ow2.proactive.scheduler.resourcemanager.nodesource.policy.ReleaseResourcesWhenSchedulerIdle;


/**
 *
 * Test checks the correct behavior of node source consisted of customized GCM infrastructure manager
 * and ReleaseResourcesWhenSchedulerIdle acquisition policy.
 *
 * NOTE: The way how customized GCM infrastructure is used here is not quite correct. It uses local host
 * to deploy several nodes so it wont be able to remove them correctly one by one. But for the scenario
 * in this test it's not required.
 *
 */
public class TestGCMCustomizedInfrastructureReleaseWhenIdlePolicy extends
        TestGCMInfrastructureReleaseWhenIdlePolicy {
    protected String getDescriptor() {
        return TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
                "/functionaltests/nodesource/1node.xml").getPath();
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {

        byte[] hosts = "localhost1 localhost2 localhost3 localhost4 localhost5".getBytes();
        // creating node source
        admin.createNodesource(sourceName, GCMCustomisedInfrastructure.class.getName(), new Object[] {
                GCMDeploymentData, hosts }, ReleaseResourcesWhenSchedulerIdle.class.getName(),
                getPolicyParams());

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesCreatedEvents().size() == 1);
    }
}
