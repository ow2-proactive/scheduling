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
package functionaltests.nodesource;

import java.io.File;

import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.TimeSlotPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.junit.Test;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;

import static functionaltests.utils.RMTHelper.log;


/**
 *
 * Test checks the correct behavior of node source consisted of GCM infrastructure manager
 * and time slot acquisition policy.
 *
 * This test may failed by timeout if the machine is too slow, so gcm deployment takes more than 15 secs
 *
 */
public class TestLocalInfrastructureTimeSlotPolicy extends RMFunctionalTest {

    protected int descriptorNodeNumber = 1;

    protected Object[] getPolicyParams() {
        return new Object[] { "ME", "ALL", TimeSlotPolicy.dateFormat.format(System.currentTimeMillis()),
                TimeSlotPolicy.dateFormat.format(System.currentTimeMillis() + 15000), "0", "true" };
    }

    protected void createEmptyNodeSource(String sourceName) throws Exception {
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        //first null parameter is the rmHelper url
        rmHelper.getResourceManager().createNodeSource(sourceName, LocalInfrastructure.class.getName(),
                new Object[] { creds, 0, RMTHelper.DEFAULT_NODES_TIMEOUT, "" }, TimeSlotPolicy.class.getName(),
                getPolicyParams());

        rmHelper.waitForNodeSourceCreation(sourceName);
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {
        // creating node source
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        rmHelper.getResourceManager().createNodeSource(sourceName, LocalInfrastructure.class.getName(),
          new Object[] { creds, descriptorNodeNumber, RMTHelper.DEFAULT_NODES_TIMEOUT, "" },
          //first parameter is empty rmHelper url
          TimeSlotPolicy.class.getName(), getPolicyParams());

        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);

        for (int i = 0; i < descriptorNodeNumber; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //we eat the configuring to free event
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    protected void removeNodeSource(String sourceName) throws Exception {
        // removing node source
        rmHelper.getResourceManager().removeNodeSource(sourceName, true);

        //wait for the event of the node source removal
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

    @Test
    public void action() throws Exception {
        String source1 = "Node_source_1";

        log("Test 1 - creation/removal of empty node source");
        createEmptyNodeSource(source1);
        removeNodeSource(source1);

        log("Test 2 - creation/removal of the node source with nodes");
        createDefaultNodeSource(source1);

        log("Test 2 - nodes will be removed in 15 secs");
        // wait for the nodes release
        for (int i = 0; i < descriptorNodeNumber; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        removeNodeSource(source1);
    }
}
