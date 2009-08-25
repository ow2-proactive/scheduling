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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.dgc.VMID;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.policy.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 *
 * Infrastructure described in GCM descriptor.
 * Supports acquire and remove all nodes at once. Is not able to remove some particular
 * nodes from the infrastructure.
 *
 */
public class GCMInfrastructure extends InfrastructureManager {

    /**
     * Deployment data container
     */
    protected class DeploymentData implements Serializable {
        byte[] data;
        boolean deployed = false;
    }

    /** logger*/
    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);
    /** path to GCM deployment descriptor */
    protected static String GCMD_PROPERTY_NAME;
    /** path to GCM application descriptor */
    protected static String gcmApplicationFile;

    /** registered nodes number */
    protected int nodesCount = 0;
    /** deployment data list */
    protected List<DeploymentData> deploymentData = new LinkedList<DeploymentData>();
    /** configurable path to a deployment descriptor */
    @Configurable(fileBrowser = true)
    protected File descriptor;

    /**
     * Initializes required properties from resource manager configuration file.
     * Loads GCM application descriptor.
     */
    protected void initialize() {
        if (gcmApplicationFile == null) {
            GCMD_PROPERTY_NAME = PAResourceManagerProperties.RM_GCMD_PATH_PROPERTY_NAME.getValueAsString();
            gcmApplicationFile = PAResourceManagerProperties.RM_GCM_TEMPLATE_APPLICATION_FILE
                    .getValueAsString();

            //test that gcmApplicationFile is an absolute path or not
            if (!(new File(gcmApplicationFile).isAbsolute())) {
                //file path is relative, so we complete the path with the prefix RM_Home constant
                gcmApplicationFile = PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator +
                    gcmApplicationFile;
            }

            //check that GCM Application template file exists
            if (!(new File(gcmApplicationFile).exists())) {
                logger
                        .info("*********  ERROR ********** Cannot find default GCMApplication template file for deployment :" +
                            gcmApplicationFile +
                            ", Resource Manager will be unable to deploy nodes by GCM Deployment descriptor");
            } else if (GCMD_PROPERTY_NAME == null || "".equals(GCMD_PROPERTY_NAME)) {
                logger.info("*********  ERROR ********** Java Property used by " + gcmApplicationFile +
                    ", to specify GCMD deployment file is not defined," +
                    " Resource Manager will be unable to deploy nodes by GCM Deployment descriptor.");
            }
        }
    }

    /**
     * Proactive default constructor.
     */
    public GCMInfrastructure() {
    }

    /**
     * Adds information required to deploy nodes in the future.
     * Do not initiate a real nodes deployment/acquisition as it's up to the
     * policy.
     */
    public void addNodesAcquisitionInfo(Object... parameters) throws RMException {
        if (parameters == null) {
            // nothing to add
            return;
        }

        if (parameters.length != 1) {
            throw new RMException("Incorrect parameters for nodes acqusition");
        }

        DeploymentData dd = new DeploymentData();
        dd.data = (byte[]) parameters[0];
        deploymentData.add(dd);
    }

    /**
     * Asynchronous node acquisition request.
     * Not supported by this infrastructure manager.
     */
    public void acquireNode() {
        logger.error("acquireNode() is not a valid operation for GCM infrastructure");
    }

    /**
     * Asynchronous request of all nodes acquisition.
     * Nodes should register themselves by calling {@link RMCore#addNode(String)}
     */
    public void acquireAllNodes() {
        logger.debug("Acquire all nodes request");
        for (DeploymentData dd : deploymentData) {
            if (!dd.deployed) {
                try {
                    logger.debug("Deploying nodes");
                    deployGCMD(convertGCMdeploymentDataToGCMappl(dd.data, null));
                    dd.deployed = true;
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            } else {
                logger.debug("Nodes have already been deployed");
            }
        }
    }

    /**
     * Remove the node from underlying infrastructure. Terminates proactive runtime if there is no more nodes.
     * @param node node to release
     * @param forever if true removes the node and associated information so that it will be no possible to
     * deploy node again. It is not supported by this infrastructure manager.
     * @throws RMException if any problems occurred
     */
    public void removeNode(Node node, boolean forever) throws RMException {
        try {

            if (forever) {
                logger.warn("Cannot remove node forever in GCM infrastructure");
            }

            logger.info("Terminating the node " + node.getNodeInformation().getName());
            if (!isThereNodesInSameJVM(node)) {
                final Node n = node;
                nodeSource.executeInParallel(new Runnable() {
                    public void run() {
                        try {
                            logger.info("Terminating the runtime " + n.getProActiveRuntime().getURL());
                            n.getProActiveRuntime().killRT(false);
                        } catch (Exception e) {
                            //do nothing, no exception treatment for node just killed before
                        }
                    }
                });
            }
            nodesCount--;

            if (nodesCount == 0) {
                if (forever) {
                    // last node released
                    // forever is set to true, so remove all deployment
                    // data in order not to redeploy nodes in the future
                    deploymentData.clear();
                } else {
                    // last node release - clear deployment status
                    // for standard GCM it's the only one possible granularity
                    for (DeploymentData dd : deploymentData) {
                        logger.debug("Last node was removed");
                        dd.deployed = false;
                    }
                }
            }
        } catch (Exception e) {
            throw new RMException(e);
        }
    }

    /**
     * Starts deployment of specified GCM application
     * @param app application to deploy
     */
    protected void deployGCMD(GCMApplication app) throws AddingNodesException {
        Map<String, GCMVirtualNode> virtualNodes = app.getVirtualNodes();
        for (Entry<String, GCMVirtualNode> entry : virtualNodes.entrySet()) {
            try {
                entry.getValue().subscribeNodeAttachment(this, "receiveDeployedNode", true);
                app.startDeployment();
            } catch (ProActiveException e) {
                throw new AddingNodesException(e);
            }
        }
    }

    /**
     * Creates a GCM application object from an Array of bytes representing a GCM deployment xml file for a given host.
     * Creates a temporary file, write the content of gcmDeploymentData array in the file. Then it creates
     * a GCM Application from the Resource manager GCM application template (corresponding to
     * {@link RMConstants.templateGCMApplication}) with a node provider which is gcmDeploymentData
     * passed in parameter. Sets "HOST" java variable. <br>
     * @param gcmDeploymentData array of bytes representing a GCM deployment file.
     * @param host a value of "HOST" java variable contract
     * @return GCMApplication object ready to be deployed
     * @throws RMException
     */
    protected GCMApplication convertGCMdeploymentDataToGCMappl(byte[] gcmDeploymentData, String host)
            throws RMException {

        initialize();

        GCMApplication appl = null;
        try {

            File gcmDeployment = File.createTempFile("gcmDeployment", "xml");
            FileToBytesConverter.convertByteArrayToFile(gcmDeploymentData, gcmDeployment);
            System.setProperty(GCMD_PROPERTY_NAME, gcmDeployment.getAbsolutePath());

            if (host != null && host.length() > 0) {
                System.setProperty("HOST", host);

                synchronized ("HOST".intern()) {
                    appl = PAGCMDeployment.loadApplicationDescriptor(new File(gcmApplicationFile));
                }
            } else {
                appl = PAGCMDeployment.loadApplicationDescriptor(new File(gcmApplicationFile));
            }

            //delete gcmd temp file
            gcmDeployment.delete();

        } catch (FileNotFoundException e) {
            throw new RMException(e);
        } catch (IOException e) {
            throw new RMException(e);
        } catch (ProActiveException e) {
            throw new RMException(e);
        }

        return appl;
    }

    /**
     * Receives a new deployed node and adding it to the NodeSource.
     * A new node has been acquired by the deployment mechanism.
     * Register it in the GCMNodeSource.
     * @param node deployed
     * @param vnodeName virtual node name of the node.
     */
    public synchronized void receiveDeployedNode(Node node, String vnodeName) {
        try {
            nodeSource.getRMCore().addNode(node.getNodeInformation().getURL(), nodeSource.getName());
        } catch (RMException e) {
            logger.error("Could not add the node " + node.getNodeInformation().getURL(), e);
        }
    }

    /**
     * Node source string representation
     */
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName());
    }

    /**
     * Node source description
     */
    public String getDescription() {
        return "Infrastructure described in GCM deployment descriptor";
    }

    /**
     * Check if there are any other nodes handled by the NodeSource in the same JVM of the node
     * passed in parameter.
     * @param node Node to check if there any other node of the NodeSource in the same JVM
     * @return true there is another node in the node's JVM handled by the nodeSource, false otherwise.
     */
    public boolean isThereNodesInSameJVM(Node node) {
        VMID nodeID = node.getVMInformation().getVMID();
        String nodeToTestUrl = node.getNodeInformation().getURL();
        Collection<Node> nodesList = nodeSource.getAliveNodes();
        for (Node n : nodesList) {
            if (!n.getNodeInformation().getURL().equals(nodeToTestUrl) &&
                n.getVMInformation().getVMID().equals(nodeID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void registerAcquiredNode(Node node) throws RMException {
        nodesCount++;
    }
}
