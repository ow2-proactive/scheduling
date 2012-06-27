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

    private RMTHelper helper = RMTHelper.getDefaultInstance();

    @Override
    protected String getDescriptor() throws URISyntaxException {
        return new File(TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
                "/functionaltests/nodesource/1node.xml").toURI()).getAbsolutePath();
    }

    @Override
    protected void createDefaultNodeSource(String sourceName) throws Exception {

        byte[] hosts = "localhost1 localhost2 localhost3 localhost4 localhost5".getBytes();
        // creating node source
        // first parameter of im is empty rm url
        helper.getResourceManager().createNodeSource(sourceName, GCMCustomisedInfrastructure.class.getName(),
                new Object[] { "", GCMDeploymentData, hosts, TIMEOUT },
                ReleaseResourcesWhenSchedulerIdle.class.getName(), getPolicyParams());

        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);

    }

    @Override
    protected void init() throws Exception {
        RMFactory.setOsJavaProperty();
        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(getDescriptor())));
        //we override the gcm application file
        if ((System.getProperty("proactive.test.runAsMe") != null) &&
            (System.getProperty("pas.launcher.forkas.method").equals("pwd"))) {
            SchedulerTHelper.startSchedulerWithEmptyResourceManager(new File(SchedulerTHelper.class
                    .getResource("config/functionalTRMPropertiesForRunAsMe.ini").toURI()).getAbsolutePath());
        } else {
            SchedulerTHelper.startSchedulerWithEmptyResourceManager(new File(SchedulerTHelper.class
                    .getResource("config/functionalTRMPropertiesForCustomisedIM.ini").toURI())
                    .getAbsolutePath());
        }

        helper.getResourceManager(null, RMTHelper.defaultUserName, RMTHelper.defaultUserPassword);
    }
}
