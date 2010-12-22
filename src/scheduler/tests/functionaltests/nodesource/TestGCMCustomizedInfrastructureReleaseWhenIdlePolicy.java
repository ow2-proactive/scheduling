/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.nodesource;

import java.io.File;

import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMCustomisedInfrastructure;
import org.ow2.proactive.scheduler.resourcemanager.nodesource.policy.ReleaseResourcesWhenSchedulerIdle;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionaltests.RMTHelper;
import functionaltests.SchedulerTHelper;


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

    /** timeout for node acquisition */
    protected static final int TIMEOUT = 60 * 1000;

    @Override
    protected String getDescriptor() {
        return TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
                "/functionaltests/nodesource/1node.xml").getPath();
    }

    @Override
    protected void createDefaultNodeSource(String sourceName) throws Exception {

        byte[] hosts = "localhost1 localhost2 localhost3 localhost4 localhost5".getBytes();
        // creating node source
        // first parameter of im is empty rm url
        RMTHelper.getResourceManager().createNodeSource(sourceName,
                GCMCustomisedInfrastructure.class.getName(),
                new Object[] { "", GCMDeploymentData, hosts, TIMEOUT },
                ReleaseResourcesWhenSchedulerIdle.class.getName(), getPolicyParams());

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);

    }

    @Override
    protected void init() throws Exception {
        RMFactory.setOsJavaProperty();
        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(getDescriptor())));
        //we override the gcm application file
        SchedulerTHelper.startSchedulerWithEmptyResourceManager(SchedulerTHelper.class.getResource(
                "config/functionalTRMPropertiesForCustomisedIM.ini").getPath());
        RMTHelper.connectToExistingRM();
    }
}
