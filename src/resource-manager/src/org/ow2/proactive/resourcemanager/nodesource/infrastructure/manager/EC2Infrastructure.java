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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.dgc.VMID;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.ec2.EC2Deployer;
import org.ow2.proactive.resourcemanager.nodesource.policy.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.utils.FileToBytesConverter;

import com.xerox.amazonws.ec2.InstanceType;


/**
 *
 * Amazon Elastic Compute Cloud Infrastructure
 * <p>
 * Has a maximum node value, which means at least X nodes can be
 * deployed at the same time, to control the instances' costs
 * <p>
 * This Infrastructure supports acquiring nodes one by one,
 * all nodes at the same time, and releasing them one by one or every one of
 * them at the same time when necessary.
 * Node acquisition suffers a 2mn delay at best, and node release is immediate
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 *
 */
public class EC2Infrastructure extends InfrastructureManager {

    @Configurable(fileBrowser = true)
    protected File configurationFile;
    @Configurable
    protected String rmUrl;
    @Configurable
    protected String rmLogin;
    @Configurable(password = true)
    protected String rmPass;

    /**
     * Deployment data
     */
    protected EC2Deployer ec2d;

    /**
     * Image descriptor to use will try the first available if null
     */
    String imgd;

    /** logger */
    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);

    /**
     * Default constructor
     */
    public EC2Infrastructure() {

    }

    /**
     * {@inheritDoc}
     */
    public void acquireAllNodes() {
        if (ec2d.canGetMoreNodes()) {
            try {
                int num = ec2d.getMaxInstances() - ec2d.getCurrentInstances();
                this.ec2d.setNsName(nodeSource.getName());
                ec2d.runInstances(imgd);
                logger.info("Successfully acquired " + num + " EC2 instance" + ((num > 1) ? "s" : ""));
            } catch (Exception e) {
                logger.error("Unable to acquire all EC2 instances", e);
            }
        } else {
            logger.info("Maximum simultaneous EC2 reservations already attained");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void acquireNode() {
        if (ec2d.canGetMoreNodes()) {
            try {
                ec2d.runInstances(1, 1, imgd);
                this.ec2d.setNsName(nodeSource.getName());
                logger.info("Successfully acquired an EC2 instance");
                return;
            } catch (Exception e) {
                logger.error("Unable to acquire EC2 instance", e);
            }
        } else {
            logger.info("Maximum simultaneous EC2 reservations already attained");
        }
    }

    /**
     * Configures the Infrastructure
     *
     * @param parameters
     *            parameters[0]: Configuration file as byte array
     *            parameters[1]: Fully qualified URL of the Resource Manager (proto://IP:port)
     *            parameters[2]: RM login
     *            parameters[3]: RM passw
     * @throws RMException
     *             when the configuration could not be set
     */
    public void addNodesAcquisitionInfo(Object... parameters) throws RMException {

        /** parameters look fine */
        if (parameters != null && parameters.length == 4) {
            try {
                //                File ff = new File(parameters[0].toString());
                byte[] configFile = (byte[]) parameters[0];
                File ff = File.createTempFile("configFile", "props");
                FileToBytesConverter.convertByteArrayToFile(configFile, ff);
                readConf(ff.getAbsolutePath());
                ff.delete();
            } catch (Exception e) {
                readConf(PAResourceManagerProperties.RM_EC2_PATH_PROPERTY_NAME.getValueAsString());
                logger.debug("Expected File as 1st parameter for EC2Infrastructure: " + e.getMessage());
            }
            String rmu = parameters[1].toString();
            if (!rmu.matches("(http|rmi|rmissh)[:][/]{2}" + "(([0-9]{1,3}[.]){3}[0-9]{1,3})[:][0-9]+[/]?")) {
                throw new RMException(
                    "Expected fully qualified URL for parameter RM Url for EC2Infrastructure.\n"
                        + "i.e. protocol://IP:port/");
            }
            String rml = parameters[2].toString();
            String rmp = parameters[3].toString();

            this.ec2d.setUserData(rmu, rml, rmp);
        }
        /**
         * missing or absent parameters, aborting
         */
        else {
            throw new RMException("Invalid parameters for EC2Infrastructure creation");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeNode(Node node, boolean forever) throws RMException {

        String hostname = node.getVMInformation().getHostName();
        String ip = node.getVMInformation().getInetAddress().getHostAddress();

        if (!isThereNodesInSameJVM(node)) {

            logger.info("No node left, closing instance on URL :" + hostname + "/" + ip);

            if (this.ec2d.terminateInstanceByAddr(hostname, ip)) {
                logger.info("Instance closed: " + hostname + "/" + ip);
                return;
            } else {
                logger.error("Could not close instance: " + hostname + "/" + ip);
            }

        } else {
            try {
                node.getProActiveRuntime().killNode(node.getNodeInformation().getName());
            } catch (ProActiveException e) {
                logger.error("Could not kill node: " + node.getNodeInformation().getName() + " on " +
                    hostname + "/" + ip);
            }
        }
    }

    /**
     * Check if there are any other nodes handled by the NodeSource in the same JVM of the node
     * passed in parameter.
     *
     * @param node
     *            Node to check if there any other node of the NodeSource in the same JVM
     * @return true there is another node in the node's JVM handled by the nodeSource, false
     *         otherwise.
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
     * Node source description
     */
    public String getDescription() {
        return "Handles nodes from the Amazon Elastic Compute Cloud Service.";
    }

    /**
     * Attemps to read a configuration file, fills deployment description fields on success
     *
     * @param path
     *            path to the configuration file
     * @throws RMException
     *             incorrect file or missing properties in file
     */
    private void readConf(String path) throws RMException {
        File fp = new File(path);

        if (!fp.isAbsolute()) {
            fp = new File(PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator + path);
            if (!fp.exists()) {
                throw new RMException("Could not find configuration file: " + path);
            }
        }

        Properties props = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(fp);
            props.load(in);
        } catch (Exception e) {
            throw new RMException("Error while reading EC2 properties: " + e.getMessage());
        }

        /** check all mandatory fields are present */
        if (!props.containsKey("AWS_AKEY"))
            throw new RMException("Missing property AWS_AKEY in: " + path);
        if (!props.containsKey("AWS_SKEY"))
            throw new RMException("Missing property AWS_SKEY in: " + path);
        if (!props.containsKey("AWS_USER"))
            throw new RMException("Missing property AWS_USER in: " + path);
        if (!props.containsKey("AMI"))
            throw new RMException("Missing property AMI in: " + path);
        if (!props.containsKey("INSTANCE_TYPE"))
            throw new RMException("Missing property INSTANCE_TYPE: " + path);
        if (!props.containsKey("MAX_INST"))
            throw new RMException("Missing property MAX_INST in: " + path);

        this.ec2d = new EC2Deployer(props.getProperty("AWS_AKEY"), props.getProperty("AWS_SKEY"), props
                .getProperty("AWS_USER"));

        this.imgd = props.getProperty("AMI");
        int maxInsts = 1;
        try {
            maxInsts = Integer.parseInt(props.getProperty("MAX_INST"));
        } catch (Exception e) {
            maxInsts = 1;
        }

        this.ec2d.setNumInstances(1, maxInsts);
        this.ec2d.setInstanceType(props.getProperty("INSTANCE_TYPE"));

        try {
            in.close();
        } catch (IOException e) {
            throw new RMException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "EC2 Infrastructure";
    }

}
