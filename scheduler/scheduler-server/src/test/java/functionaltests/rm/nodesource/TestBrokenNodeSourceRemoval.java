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
package functionaltests.rm.nodesource;

import java.io.File;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
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
import org.junit.Test;

import functionaltests.utils.RMTHelper;
import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestUsers;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.*;


public class TestBrokenNodeSourceRemoval extends SchedulerFunctionalTest {

    protected int defaultDescriptorNodesNb = 1;
    private RMTHelper rmHelper;

    protected void createDefaultNodeSource(String sourceName) throws Exception {
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        schedulerHelper.getResourceManager().createNodeSource(sourceName, LocalInfrastructure.class.getName(),
          new Object[] { creds, defaultDescriptorNodesNb, RMTHelper.DEFAULT_NODES_TIMEOUT, "" },
          ReleaseResourcesWhenSchedulerIdle.class.getName(), getPolicyParams());

        schedulerHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
    }

    protected Object[] getPolicyParams() throws Exception {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(SchedulerTHelper.getLocalUrl());
        Credentials creds = Credentials.createCredentials(
                new CredData(TestUsers.DEMO.username, TestUsers.DEMO.password), auth.getPublicKey());
        return new Object[] { "ALL", "ME", SchedulerTHelper.getLocalUrl(), creds.getBase64(), "30000" };
    }

    private ResourceManager startRMPreservingDB() throws Exception {
        String rmconf = new File(PAResourceManagerProperties.getAbsolutePath(getClass().getResource(
          "/functionaltests/config/rm-with-db.ini").getFile())).getAbsolutePath();
        rmHelper.startRM(rmconf);
        return rmHelper.getResourceManager();
    }

    protected boolean removeNodeSource(String sourceName) throws Exception {
        // removing node source
        BooleanWrapper res = rmHelper.getResourceManager().removeNodeSource(sourceName, true);
        return res.getBooleanValue();
    }

    @Before
    public void createRMHelper() throws Exception {
        rmHelper = new RMTHelper();
    }

    @After
    public void killStartedRM() throws Exception {
        rmHelper.killRM();
    }

    @Test
    public void action() throws Exception {
        RMFactory.setOsJavaProperty();

        schedulerHelper.startSchedulerWithEmptyResourceManager();

        // creating node source with the scheduler aware policy
        log("Phase 2 - creating the node source");
        String source1 = "Node_source_1";
        createDefaultNodeSource(source1);

        // killing RM & Scheduler 
        log("Phase 3 - restarting the RM & Scheduler");
        schedulerHelper.killScheduler();

        // starting the RM
        ResourceManager resourceManager = startRMPreservingDB();

        // the resource manager should try to recover the node source
        // but it will fail because the scheduler does not exist
        log("Phase 4 - checking the number of nodesources");
        ArrayList<RMNodeSourceEvent> nodeSources = resourceManager.getMonitoring().getState().getNodeSource();

        assertEquals(0, nodeSources.size());

        // but we should be able to successfully remove the broken node source
        log("Phase 5 - checking the removal of nodesource");
        assertTrue(removeNodeSource(source1));
    }

}
