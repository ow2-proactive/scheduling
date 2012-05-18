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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.util.HashMap;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
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
@Deprecated
public class GCMCustomisedInfrastructure extends GCMInfrastructure {

    private static final long serialVersionUID = 32L;

    /** hosts list */
    HashMap<String, DeploymentData> hosts = new HashMap<String, DeploymentData>();
    /** path to the file with host names */
    @Configurable(fileBrowser = true, description = "List of host to use\nfor the deployment")
    protected File hostsList;
    /**
     * the timeout after which one the node is considered to be lost
     * (ie. its registration after this timeout will be discarded)
     */
    @Configurable(description = "Timeout after which one the node\nis considered to be lost")
    protected int timeout = 1 * 60 * 1000;//1mn

    /**
     * Default constructor
     */
    public GCMCustomisedInfrastructure() {
    }

    /**
     * Adds information required to deploy nodes in the future.
     * Do not initiate a real nodes deployment/acquisition as it's up to the
     * policy.
     * parameters[0] = gcmd template
     * parameters[1] = host list
     * parameters[2] = timeout
     */
    @Override
    public void configure(Object... parameters) {
        if (parameters == null) {
            // nothing to do
            throw new IllegalArgumentException("No parameters were specified");
        }
        if (parameters.length == 3) {
            if (parameters[1] == null) {
                throw new IllegalArgumentException("Host list file must be specified");
            }

            String hosts = new String((byte[]) parameters[1]);

            String[] hostsList = hosts.split("\\s");
            logger.debug("Number of hosts " + hostsList.length);
            if (hostsList != null) {
                for (String host : hostsList) {
                    if (host == null || "".equals(host)) {
                        continue;
                    }
                    if (parameters[0] == null) {
                        throw new IllegalArgumentException("GCMD template file must be specified");
                    }

                    DeploymentData dd = new DeploymentData();
                    dd.data = (byte[]) parameters[0];
                    logger.debug("Registered deployment data for host " + host);
                    this.hosts.put(host, dd);
                }
            }
            try {
                this.timeout = Integer.parseInt(parameters[2].toString());
            } catch (NumberFormatException e) {
                logger
                        .warn("Cannot determine the value of the supplied parameter for the timeout attribute, using the default one: " +
                            this.timeout);
            }
        } else {
            throw new IllegalArgumentException("Wrong number of parameters");
        }

    }

    /**
     * Asynchronous request of all nodes acquisition.
     * Proactive node should register itself by calling {@link NodeSource#acquireNode(String, org.ow2.proactive.resourcemanager.authentication.Client)}
     */
    @Override
    public void acquireAllNodes() {
        logger.debug("Acquire all nodes request");
        for (String host : hosts.keySet()) {
            DeploymentData dd = hosts.get(host);
            if (!dd.deployed) {
                try {
                    this.acquireNodeImpl(dd, host);
                    dd.deployed = true;
                } catch (Exception e) {
                    logger.error("Cannot add nodes for GCM node source", e);
                }
            } else {
                logger.debug("Nodes have already been deployed");
            }
        }
    }

    /**
     * Asynchronous node acquisition request.
     * Proactive node should register itself by calling {@link NodeSource#acquireNode(String, org.ow2.proactive.resourcemanager.authentication.Client)}
     */
    @Override
    public void acquireNode() {
        logger.debug("Acquire node request");
        for (String host : hosts.keySet()) {
            DeploymentData dd = hosts.get(host);
            if (!dd.deployed) {
                try {
                    this.acquireNodeImpl(dd, host);
                    dd.deployed = true; // if deployment fails it will work incorrect
                    break;
                } catch (Exception e) {
                    logger.error("Cannot add nodes for GCM node source", e);
                }
            }
        }
    }

    /**
     * The method wich effectively launch the node deployment
     * @param dd the deployment data used for this deployment
     * @param host the host use for the deployment
     * @param nodeName the name of the node
     * @throws Exception
     */
    private void acquireNodeImpl(DeploymentData dd, String host) throws Exception {
        String nodeName = "GCMCustomised-" + host + "-" + ProActiveCounter.getUniqID();
        //hack - createGCMNode append the gcmnode name + the vm capacity, here forcibly 0
        String deployingNodeName = nodeName + "_" + Constants.GCM_NODE_NAME + "0";
        //create a deploying node
        String dnURL = super.addDeployingNode(deployingNodeName,
                "Deployed by GCMDeployment descriptor on host " + host, "Node deploying on host " + host,
                this.timeout);
        logger.debug("Acquiring node on " + host);
        VariableContractImpl vContract = new VariableContractImpl();
        StringBuilder properties = new StringBuilder();
        properties.append(CentralPAPropertyRepository.PA_RUNTIME_NAME.getCmdLine());
        properties.append(nodeName);
        vContract.setVariableFromProgram(DESCRIPTOR_DEFAULT_VARIABLE_JVM_ARGS, properties.toString(),
                VariableContractType.ProgramVariable);
        try {
            deployGCMD(convertGCMdeploymentDataToGCMappl(dd.data, host, vContract));
        } catch (Exception e) {
            handleExceptionAtDeployment(dnURL, e);
            throw e;
        }
    }

    /**
     * Declares a deploying node lost
     * @param dnURL the Deploying node url to declare lost
     * @param t the cause of the faulty deployment
     */
    private void handleExceptionAtDeployment(String dnURL, Throwable t) {
        if (dnURL != null) {
            String lf = System.getProperty("line.separator");
            super.declareDeployingNodeLost(dnURL, "An exception occurred during node deployment:" + lf +
                Utils.getStacktrace(t));
        } else {
            logger.warn("Cannot declare deploying node lost, no deploying node URL found");
        }
    }

    /**
     * Releases the node. Removes proactive runtime if there is no more nodes.
     * Marks corresponding deployment data as undeployed.
     *
     * @param node node to release
     * @throws RMException if any problems occurred
     */
    @Override
    public void removeNode(Node node) throws RMException {
        super.removeNode(node);

        String hostName = URIBuilder.getHostNameFromUrl(node.getNodeInformation().getURL());
        DeploymentData dd = hosts.get(hostName);
        if (dd != null) {
            logger.info("Host " + hostName + " released");
            dd.deployed = false;
        } else {
            logger.error("Cannot find deployment data for host " + hostName);
        }
    }

    /**
     * Node source description
     */
    @Override
    public String getDescription() {
        return "[DEPRECATED] Handles hosts from the list using specified gcm deployment descriptor\n"
            + "template with HOST java variable contract (see proactive documentation)";
    }
}
