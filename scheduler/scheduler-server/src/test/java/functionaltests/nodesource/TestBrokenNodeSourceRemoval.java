/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionaltests.nodesource;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.resourcemanager.nodesource.policy.ReleaseResourcesWhenSchedulerIdle;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.tests.FunctionalTest;
import org.junit.Assert;

import functionaltests.RMTHelper;
import functionaltests.SchedulerTHelper;


public class TestBrokenNodeSourceRemoval extends FunctionalTest {

    protected byte[] GCMDeploymentData;

    protected int defaultDescriptorNodesNb = 1;
    private RMTHelper helper = RMTHelper.getDefaultInstance(SchedulerTHelper.PNP_PORT);

    protected Object[] getPolicyParams() throws Exception {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(SchedulerTHelper.schedulerUrl);
        Credentials creds = Credentials.createCredentials(new CredData(SchedulerTHelper.admin_username,
            SchedulerTHelper.admin_password), auth.getPublicKey());
        return new Object[] { "ALL", "ME", SchedulerTHelper.schedulerUrl, creds.getBase64(), "30000" };
    }

    protected String getDescriptor() throws URISyntaxException {
        return new File(TestBrokenNodeSourceRemoval.class
                .getResource("/functionaltests/nodesource/1node.xml").toURI()).getAbsolutePath();
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {
        // creating node source
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        helper.getResourceManager().createNodeSource(sourceName, LocalInfrastructure.class.getName(),
                new Object[] { creds, defaultDescriptorNodesNb, RMTHelper.defaultNodesTimeout, "" },
                ReleaseResourcesWhenSchedulerIdle.class.getName(), getPolicyParams());

        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
    }

    private ResourceManager startRMPreservingDB() throws Exception {
        helper.reset();
        String rmconf = new File(PAResourceManagerProperties.getAbsolutePath(getClass().getResource(
                "/functionaltests/config/rm-with-db.ini").getFile())).getAbsolutePath();
        helper.startRM(rmconf, SchedulerTHelper.PNP_PORT);
        return helper.getResourceManager();
    }

    protected boolean removeNodeSource(String sourceName) throws Exception {
        // removing node source
        BooleanWrapper res = helper.getResourceManager().removeNodeSource(sourceName, true);
        return res.getBooleanValue();
    }

    protected void init() throws Exception {
        RMFactory.setOsJavaProperty();
        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(getDescriptor())));
        SchedulerTHelper.startSchedulerWithEmptyResourceManager();
        SchedulerTHelper.log("Phase 1 - created RM & Scheduler");
    }

    @org.junit.Test
    public void action() throws Exception {

        SchedulerTHelper.log("Phase 1 - creating RM & Scheduler");
        init();

        // creating node source with the scheduler aware policy
        SchedulerTHelper.log("Phase 2 - creating the node source");
        String source1 = "Node_source_1";
        createDefaultNodeSource(source1);

        // killing RM & Scheduler 
        SchedulerTHelper.log("Phase 3 - restarting the RM & Scheduler");
        helper.getResourceManager().shutdown(true).getBooleanValue();
        SchedulerTHelper.killScheduler();

        // starting the RM
        ResourceManager resourceManager = startRMPreservingDB();

        // the resource manager should try to recover the node source
        // but it will fail because the scheduler does not exist
        SchedulerTHelper.log("Phase 4 - checking the number of nodesources");
        ArrayList<RMNodeSourceEvent> nodeSources = resourceManager.getMonitoring().getState().getNodeSource();

        Assert.assertEquals(0, nodeSources.size());

        // but we should be able to successfully remove the broken node source
        SchedulerTHelper.log("Phase 5 - checking the removal of nodesource");
        Assert.assertTrue(removeNodeSource(source1));
    }

}
