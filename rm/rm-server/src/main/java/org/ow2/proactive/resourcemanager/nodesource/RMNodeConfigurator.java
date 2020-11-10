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
package org.ow2.proactive.resourcemanager.nodesource;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;


/**
 * This class is responsible for the node configuration
 */
@ActiveObject
public class RMNodeConfigurator implements RunActive {
    /** class' logger */
    private static final Logger logger = Logger.getLogger(RMNodeConfigurator.class);

    /** rmcore reference to be able to add the node to the core after the configuration went well */
    private RMCore rmcore;

    /** PA Constructor */
    public RMNodeConfigurator() {
    }

    public RMNodeConfigurator(RMCore rmcore) {
        this.rmcore = rmcore;
    }

    /**
     * Configures the node.
     * Every different configuration steps must be handled in this method.
     * @param rmnodeToAdd the rmnode to be configured
     */
    public void configureNode(RMNode rmnodeToAdd) {
        String nodeURL = rmnodeToAdd.getNodeURL();
        try {
            Node nodeToAdd = rmnodeToAdd.getNode();

            String dataSpaceStatus = nodeToAdd.getProperty(RMNodeStarter.DATASPACES_STATUS_PROP_NAME);
            if (dataSpaceStatus == null) {
                // no data space configured on the node
                logger.debug("Configuring data spaces for node " + nodeToAdd.getNodeInformation().getURL());
                if (!configureForDataSpace(nodeToAdd)) {
                    // the dataspaces could not be configured, the node should be killed.
                    nodeToAdd.getProActiveRuntime().killRT(true);
                    throw new NotConfiguredException("Failed to configure dataspaces, check the node logs for more details");
                }
            } else if (!dataSpaceStatus.equals(Boolean.TRUE.toString())) {
                // there was a problem of data space configuring
                nodeToAdd.getProActiveRuntime().killRT(true);
                throw new NotConfiguredException("Cannot configure data spaces : " + dataSpaceStatus);
            } else {
                // data space is configured
                logger.debug("Data spaces is already configured for node " + nodeToAdd.getNodeInformation().getURL());
            }

            // get the node tags specified in the node property NODE_TAGS_PROP_NAME (separated by comma)
            String tagsString = nodeToAdd.getProperty(RMNodeStarter.NODE_TAGS_PROP_NAME);
            Set<String> tags = Arrays.stream(tagsString.split(","))
                                     .map(String::trim)
                                     .filter(s -> !s.isEmpty())
                                     .collect(Collectors.toSet());
            rmnodeToAdd.setNodeTags(tags);

            // setting node JMX connector urls
            rmnodeToAdd.setJMXUrl(JMXTransportProtocol.RMI,
                                  nodeToAdd.getProperty(RMNodeStarter.JMX_URL + JMXTransportProtocol.RMI));
            rmnodeToAdd.setJMXUrl(JMXTransportProtocol.RO,
                                  nodeToAdd.getProperty(RMNodeStarter.JMX_URL + JMXTransportProtocol.RO));

            // blocking call involving running ping process on the node
            if (PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
                RMCore.topologyManager.addNode(nodeToAdd);
            }
            rmcore.internalAddNodeToCore(rmnodeToAdd);
        } catch (Exception e) {
            logger.error("Cannot properly configure the node " + nodeURL +
                         " because of an error during configuration phase", e);
            //if a problem occurs during the configuration step,
            //the node is set to down
            rmcore.setDownNode(nodeURL);
        }
    }

    /**
     * Configure node for dataSpaces
     *
     * @param node the node to be configured
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */
    protected boolean configureForDataSpace(Node node) throws ActiveObjectCreationException, NodeException {
        return RMNodeStarter.configureNodeForDataSpace(node);
    }

    /**
     * Method controls the execution of every request.
     * Tries to keep this active object alive in case of any exception.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            try {
                Request request = service.blockingRemoveOldest();
                if (request != null) {
                    try {
                        service.serve(request);
                    } catch (Throwable e) {
                        logger.error("Cannot serve request: " + request, e);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("runActivity interrupted", e);
            }
        }
    }
}
