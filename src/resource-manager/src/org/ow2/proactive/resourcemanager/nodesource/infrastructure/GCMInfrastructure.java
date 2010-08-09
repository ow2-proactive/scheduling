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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 *
 * Infrastructure described in GCM descriptor.
 * Supports acquire and remove all nodes at once. Is not able to remove some particular
 * nodes from the infrastructure.
 *
 */
public class GCMInfrastructure extends DefaultInfrastructureManager {

    /**  */
    private static final long serialVersionUID = 21L;

    /**
     * Deployment data container
     */
    protected class DeploymentData implements Serializable {
        /**  */
        private static final long serialVersionUID = 21L;
        byte[] data;
        boolean deployed = false;
    }

    /** path to GCM deployment descriptor */
    protected static String GCMD_PROPERTY_NAME;
    /** path to GCM application descriptor */
    protected static String gcmApplicationFile;

    /** deployment data list */
    protected DeploymentData deploymentData = new DeploymentData();
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
    @Override
    public BooleanWrapper configure(Object... parameters) {
        if (parameters == null) {
            // nothing to add
            throw new IllegalArgumentException("No parameters were specified");
        }

        if (parameters.length != 1) {
            throw new IllegalArgumentException("Incorrect parameters for nodes acqusition");
        }

        if (parameters[0] == null) {
            // gcmd descriptor was not specified
            throw new IllegalArgumentException("GCMD file must be specified");
        }
        deploymentData.data = (byte[]) parameters[0];

        return new BooleanWrapper(true);
    }

    /**
     * Asynchronous node acquisition request.
     * Not supported by this infrastructure manager.
     */
    @Override
    public void acquireNode() {
        logger.error("acquireNode() is not a valid operation for GCM infrastructure");
    }

    /**
     * Asynchronous request of all nodes acquisition.
     * Proactive node should register itself by calling {@link NodeSource#acquireNode(String, org.ow2.proactive.resourcemanager.authentication.Client)}
     */
    @Override
    public void acquireAllNodes() {
        logger.debug("Acquire all nodes request");
        if (!deploymentData.deployed) {
            try {
                logger.debug("Deploying nodes");
                if (deploymentData.data != null) {
                    deployGCMD(convertGCMdeploymentDataToGCMappl(deploymentData.data, null));
                } else {
                    logger.warn("Empty gcmd descriptor");
                }
                deploymentData.deployed = true;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.warn("Nodes have already been deployed");
        }
    }

    /**
     * Remove the node from underlying infrastructure. Terminates proactive runtime if there is no more nodes.
     * @param node node to release
     * @throws RMException if any problems occurred
     */
    @Override
    public void removeNode(Node node) throws RMException {
        try {
            super.removeNode(node);
            if (nodesCount == 0) {
                // last node release - clear deployment status
                // for standard GCM it's the only one possible granularity
                logger.debug("Last node was removed");
                deploymentData.deployed = false;
            }
        } catch (Exception e) {
            throw new RMException(e);
        }
    }

    /**
     * Starts deployment of specified GCM application
     * @param app application to deploy
     * @throws ProActiveException
     */
    protected void deployGCMD(GCMApplication app) throws ProActiveException {
        Map<String, GCMVirtualNode> virtualNodes = app.getVirtualNodes();
        for (Entry<String, GCMVirtualNode> entry : virtualNodes.entrySet()) {
            entry.getValue().subscribeNodeAttachment(this, "receiveDeployedNode", true);
            app.startDeployment();
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
        // make the call to node source through the stub in order to
        // correctly control the security access in the core
        nodeSource.getStub().acquireNode(node.getNodeInformation().getURL(), nodeSource.getProvider());
    }

    /**
     * Node source string representation
     */
    @Override
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName());
    }

    /**
     * Node source description
     */
    @Override
    public String getDescription() {
        return "Infrastructure described in GCM deployment descriptor";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerAcquiredNode(Node node) throws RMException {
        nodesCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutDown() {
    }
}
