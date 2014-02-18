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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.nodesource.dataspace.DataSpaceNodeConfigurationAgent;
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
                configureForDataSpace(nodeToAdd);
            } else if (!dataSpaceStatus.equals(Boolean.TRUE.toString())) {
                // there was a problem of data space configuring
                logger.error("Cannot configure data spaces : " + dataSpaceStatus);
            } else {
                // data space is configured
                logger.debug("Data spaces is already configured for node " +
                    nodeToAdd.getNodeInformation().getURL());
            }

            // setting node JMX connector urls
            rmnodeToAdd.setJMXUrl(JMXTransportProtocol.RMI, nodeToAdd.getProperty(RMNodeStarter.JMX_URL +
                JMXTransportProtocol.RMI));
            rmnodeToAdd.setJMXUrl(JMXTransportProtocol.RO, nodeToAdd.getProperty(RMNodeStarter.JMX_URL +
                JMXTransportProtocol.RO));

            // blocking call involving running ping process on the node
            if (PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
                RMCore.topologyManager.addNode(nodeToAdd);
            }
            rmcore.internalAddNodeToCore(rmnodeToAdd);
        } catch (Exception e) {
            logger.warn("Cannot properly configure the node " + nodeURL +
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
    protected void configureForDataSpace(Node node) throws ActiveObjectCreationException, NodeException {
        DataSpaceNodeConfigurationAgent conf = (DataSpaceNodeConfigurationAgent) PAActiveObject.newActive(
                DataSpaceNodeConfigurationAgent.class.getName(), null, node);
        conf.configureNode();
    }

    /**
     * Method controls the execution of every request.
     * Tries to keep this active object alive in case of any exception.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            Request request = null;
            try {
                request = service.blockingRemoveOldest();
                if (request != null) {
                    try {
                        service.serve(request);
                    } catch (Throwable e) {
                        logger.error("Cannot serve request: " + request, e);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
