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
import java.net.InetAddress;
import java.net.URL;

import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMCustomisedInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.TimeSlotPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionaltests.RMTHelper;


/**
 *
 * Test checks the correct behavior of node source consisted of GCM infrastructure manager
 * and time slot acquisition policy.
 *
 * This test may failed by timeout if the machine is too slow, so gcm deployment takes more than 15 secs
 *
 * NOTE: The way how customized GCM infrastructure is used here is not quite correct. It uses local host
 * to deploy several nodes so it wont be able to remove them correctly one by one. But for the scenario
 * in this test it's not required.
 *
 */
public class TestGCMCustomizedInfrastructureTimeSlotPolicy extends TestLocalInfrastructureTimeSlotPolicy {

    protected byte[] emptyGCMD;
    protected byte[] GCMDeploymentData;
    protected byte[] hostsListData;
    /** timeout for node acquisition */
    private static final int TIMEOUT = 60 * 1000;

    private RMTHelper helper = RMTHelper.getDefaultInstance();

    @Override
    protected void init() throws Exception {
        URL oneNodeescriptor = TestGCMCustomizedInfrastructureTimeSlotPolicy.class
                .getResource("/functionaltests/nodesource/1node.xml");
        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(oneNodeescriptor.toURI())));
        URL emptyNodeDescriptor = TestGCMCustomizedInfrastructureTimeSlotPolicy.class
                .getResource("/functionaltests/nodesource/empty_gcmd.xml");
        emptyGCMD = FileToBytesConverter.convertFileToByteArray((new File(emptyNodeDescriptor.toURI())));
        // overriding gcma file
        helper.getResourceManager(new File(RMTHelper.class.getResource(
                "/functionaltests/config/functionalTRMPropertiesForCustomisedIM.ini").toURI())
                .getAbsolutePath(), RMTHelper.defaultUserName, RMTHelper.defaultUserPassword);
        hostsListData = InetAddress.getLocalHost().getHostName().getBytes();
    }

    @Override
    protected void createEmptyNodeSource(String sourceName) throws Exception {
        helper.getResourceManager().createNodeSource(
                sourceName,
                // first parameter of im is rm url, empty means default
                GCMCustomisedInfrastructure.class.getName(),
                new Object[] { "", emptyGCMD, hostsListData, TIMEOUT }, TimeSlotPolicy.class.getName(),
                getPolicyParams());
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
    }

    @Override
    protected void createDefaultNodeSource(String sourceName) throws Exception {
        // creating node source
        helper.getResourceManager().createNodeSource(sourceName, GCMCustomisedInfrastructure.class.getName(),
                // first parameter of im is rm url, empty means default
                new Object[] { "", GCMDeploymentData, hostsListData, TIMEOUT },
                TimeSlotPolicy.class.getName(), getPolicyParams());

        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);

        for (int i = 0; i < descriptorNodeNumber; i++) {
            //deploying node added
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //deploying node removed
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            //node added
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //we eat the configuring to free event
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }
}
