/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.utils;

import java.io.File;
import java.util.List;

import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.tests.ProActiveSetup;

import functionaltests.monitor.RMMonitorsHandler;


public class RMNodeSourceHelper {

    public static void defineLocalNodeSourceAndWait(String name, int nodeNumber, ResourceManager rm,
            RMMonitorsHandler monitor) throws Exception {
        RMFactory.setOsJavaProperty();
        RMTHelper.log("Define a node source " + name);
        //first emtpy im parameter is default rm url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        rm.defineNodeSource(name,
                            LocalInfrastructure.class.getName(),
                            new Object[] { creds, nodeNumber, RMTHelper.DEFAULT_NODES_TIMEOUT,
                                           RMTHelper.setup.getJvmParameters() },
                            StaticPolicy.class.getName(),
                            null,
                            RMFunctionalTest.NODES_NOT_RECOVERABLE);
        rm.setNodeSourcePingFrequency(5000, name);

        waitForNodeSourceDefinition(name, monitor);
    }

    public static void deployNodeSourceAndWait(String name, ResourceManager rm, RMMonitorsHandler monitor,
            int nodesNumberToWaitFor) {
        RMTHelper.log("Deploy node source " + name);
        rm.deployNodeSource(name);
        rm.setNodeSourcePingFrequency(5000, name);

        RMTHelper.waitForNodeSourceCreation(name, nodesNumberToWaitFor, monitor);
    }

    public static void waitForNodeSourceDefinition(String name, RMMonitorsHandler monitor) {
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_DEFINED, name, monitor);
    }

    public static void waitForNodeSourceUndeployment(String name, int numberOfNodesToBeRemoved,
            RMMonitorsHandler monitor) {
        RMTHelper.waitForAnyMultipleNodeEvent(RMEventType.NODE_REMOVED, numberOfNodesToBeRemoved, monitor);
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_UNDEPLOYED, name, monitor);
    }

    public static void waitForNodeSourceRemoval(String name, int numberOfNodesToBeRemoved, RMMonitorsHandler monitor) {
        RMTHelper.waitForAnyMultipleNodeEvent(RMEventType.NODE_REMOVED, numberOfNodesToBeRemoved, monitor);
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, name, monitor);
    }

}
