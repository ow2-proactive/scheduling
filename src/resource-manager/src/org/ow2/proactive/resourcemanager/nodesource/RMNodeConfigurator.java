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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.resourcemanager.nodesource;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.nodesource.dataspace.DataSpaceNodeConfigurationAgent;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * This class is responsible for the node configuration
 */
public class RMNodeConfigurator {
    /** class' logger */
    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);
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
            configureForDataSpace(nodeToAdd);
            // blocking call involving running ping process on the node
            RMCore.topologyManager.addNode(nodeToAdd);
            rmcore.internalAddNodeToCore(nodeURL);
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
}
