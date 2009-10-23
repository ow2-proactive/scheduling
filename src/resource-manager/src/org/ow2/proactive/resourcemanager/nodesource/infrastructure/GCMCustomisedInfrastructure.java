/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.util.HashMap;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.URIBuilder;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;


/**
 *
 * Infrastructure described in GCM descriptor which is capable to deploy a
 * single node. It is based on hosts list and GCMD template. Host lists is
 * a set of hosts which is available in the infrastructure. GCMD template
 * defines how to deploy a single node to each host. <br>
 *
 * NOTE: the infrastructure manager has a limitation "one node per host".
 *
 */
public class GCMCustomisedInfrastructure extends GCMInfrastructure {

    /** hosts list */
    HashMap<String, DeploymentData> hosts = new HashMap<String, DeploymentData>();
    /** path to the file with host names */
    @Configurable(fileBrowser = true)
    protected File hostsList;

    /**
     * Default constructor
     */
    public GCMCustomisedInfrastructure() {
    }

    /**
     * Adds information required to deploy nodes in the future.
     * Do not initiate a real nodes deployment/acquisition as it's up to the
     * policy.
     */
    public void configure(Object... parameters) throws RMException {
        if (parameters == null) {
            // nothing to do
            return;
        }
        if (parameters.length == 2) {
            if (parameters[1] == null) {
                throw new RMException("Host list file must be specified");
            }

            String hosts = new String((byte[]) parameters[1]);

            String[] hostsList = hosts.split("\\s");
            logger.debug("Number of hosts " + hostsList.length);

            for (String host : hostsList) {
                if (parameters[0] == null) {
                    throw new RMException("GCMD template file must be specified");
                }

                DeploymentData dd = new DeploymentData();
                dd.data = (byte[]) parameters[0];
                logger.debug("Registed deployment data for host " + host);
                this.hosts.put(host, dd);
            }
        }
    }

    /**
     * Asynchronous request of all nodes acquisition.
     * Nodes should register themselves by calling {@link RMCore#addNode(String, String)}
     */
    public void acquireAllNodes() {
        logger.debug("Acquire all nodes request");
        for (String host : hosts.keySet()) {
            DeploymentData dd = hosts.get(host);
            if (!dd.deployed) {
                logger.debug("Acquiring node on " + host);
                try {
                    deployGCMD(convertGCMdeploymentDataToGCMappl(dd.data, host));
                    dd.deployed = true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                logger.debug("Nodes have already been deployed");
            }
        }
    }

    /**
     * Asynchronous node acquisition request.
     * Proactive node should register itself by calling {@link RMCore#addNode(String, String)}
     */
    public void acquireNode() {
        logger.debug("Acquire node request");
        for (String host : hosts.keySet()) {
            DeploymentData dd = hosts.get(host);
            if (!dd.deployed) {
                try {
                    logger.debug("Acquiring node on " + host);
                    deployGCMD(convertGCMdeploymentDataToGCMappl(dd.data, host));
                    dd.deployed = true; // if deployment fails it will work incorrect
                    break;
                } catch (Exception e) {
                    logger.error("Cannot add nodes for GCM node source");
                }
            }
        }
    }

    /**
     * Releases the node. Removes proactive runtime if there is no more nodes.
     * Marks corresponding deployment data as undeployed.
     *
     * @param node node to release
     * @throws RMException if any problems occurred
     */
    public void removeNode(Node node) throws RMException {
        super.removeNode(node);

        String hostName = URIBuilder.getHostNameFromUrl(node.getNodeInformation().getURL());
        DeploymentData dd = hosts.get(hostName);
        if (dd != null) {
            logger.info("Host " + hostName + " released");
            dd.deployed = false;
        } else {
            logger.error("Cannot find deploymeny data for host " + hostName);
        }
    }

    /**
     * Node source description
     */
    public String getDescription() {
        return "Handles hosts from the list using specified gcm deployment descriptor\n"
            + "template with HOST java variable contract (see proactive documentation)";
    }
}
