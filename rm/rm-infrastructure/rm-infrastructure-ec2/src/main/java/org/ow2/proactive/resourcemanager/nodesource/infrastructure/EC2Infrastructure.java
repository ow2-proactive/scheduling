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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter.CommandLineBuilder;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter.OperatingSystem;
import org.ow2.proactive.utils.FileToBytesConverter;

import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;


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
 * 
 * AmazonEc2 AMI requirements (all platforms):
 * 		- java command is in the execution path
 * 		- RM_HOME environment variable points to the installation folder of the Resource manager
 * 		- at boot time, the instance executes the command received as user data 
 * 		  (accessible, for instance, from http://169.254.169.254/latest/user-data)	
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 *
 */
public class EC2Infrastructure extends InfrastructureManager {

    /**
     * The java home path on the ec2 instance
     */
    private static final String REMOTE_JAVA_EXE = "java";

    /**
     * The resource manager home on the ec2 instance
     * 
     */
    private static final String REMOTE_RM_HOME_WIN = "%RM_HOME%";

    private static final String REMOTE_RM_HOME_UNIX = "$RM_HOME";

    @Configurable(fileBrowser = true, description = "Absolute path of EC2 configuration file")
    protected File configurationFile;
    @Configurable(credential = true, description = "Absolute path of the credential file")
    protected File RMCredentialsPath;
    /**
     * The credentials as a String BASE64 encoded
     */
    private String creds64;

    @Configurable(description = "The communication protocol the remote node")
    protected String communicationProtocol = "pamr";

    @Configurable(description = "Additional JVM options \n Ex: -Dproperty1=value1 -Dproperty2=value2")
    protected String additionalJVMOptions;

    /** Deployment data */
    protected EC2Deployer ec2d;

    /**
     * The list of identifed and registered EC2 instances indexed with the nodeURL
     * they deployed.
     * Essentially used as safe check if the terminate by DNS name doesn't work...
     */
    private Hashtable<String, Instance> nodeNameToInstance = new Hashtable<String, Instance>();

    /** delay after which a requested EC2 instance is considered lost in ms */
    private static final long TIMEOUT_DELAY = 60000 * 45; // 45mn

    /** the retry threshold */
    private static final int RETRY_THRESHOLD = 5;

    /** Hashtable to be able to associate a deploying node to a given ec2 instance */
    private final Hashtable<String, Instance> deployingNodeToInstance = new Hashtable<String, Instance>();

    /** Image descriptor to use will try the first available if null */
    private String imgd;

    /**
     * Default constructor
     */
    public EC2Infrastructure() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acquireAllNodes() {
        synchronized (this.ec2d) {
            int circuitBroker = EC2Infrastructure.RETRY_THRESHOLD;
            while (ec2d.canGetMoreNodes()) {
                try {
                    this.startEC2Instance();
                } catch (RMException e) {
                    circuitBroker--;
                    if (circuitBroker > 0) {
                        logger.warn("An exception occurred while starting a new EC2 instance. Retrying", e);
                    } else {
                        logger.error("Cannot start a new EC2 instance after several attempts", e);
                        return;
                    }
                }
            }
            logger.info("Maximum simultaneous EC2 reservations reached");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acquireNode() {
        synchronized (this.ec2d) {
            if (ec2d.canGetMoreNodes()) {
                try {
                    this.startEC2Instance();
                } catch (RMException e) {
                    logger.error("Cannot start a new EC2 instance", e);
                    return;
                }
            } else {
                logger.info("Maximum simultaneous EC2 reservations reached");
            }
        }
    }

    /**
     * Starts a new EC2 instance.
     * Adds a new entry in {@link #deployingNodeToInstance}
     * @throws RMException if the deployment failed
     */
    private void startEC2Instance() throws RMException {
        String deployingNodeURL = null;
        CommandLineBuilder clb = this.buildEC2CommandLine();
        try {
            String cmd = clb.buildCommandLine(true);

            //Add Additional jvm options
            if ((additionalJVMOptions != null) && (additionalJVMOptions.trim() != "")) {
                cmd = cmd + " " + additionalJVMOptions;
            }
            logger
                    .info(this.getClass().getName() + "-executing commmand for nodes deployment.\n cmd= " +
                        cmd);
            //            System.out.println(this.getClass().getName()+ "-executing commmand for nodes deployment.\n cmd= "+cmd);
            deployingNodeURL = super.addDeployingNode(clb.getNodeName(), cmd,
                    "Deploying node on EC2 instance", TIMEOUT_DELAY);
            //we always deploy one by one to be able to match an instance with a deploying node
            List<Instance> booteds = ec2d.runInstances(1, 1, this.imgd, cmd);
            Instance booted = null;
            int size = booteds.size();
            if (size != 1) {
                //doing some cleanup, safe check
                logger.warn("The number of started instances doesn't match, expected 1 get " + size);
                if (size <= 0) {
                    logger.error("No instance started");
                    if (deployingNodeURL != null) {
                        //declaring the node lost
                        super.declareDeployingNodeLost(deployingNodeURL,
                                "The associated EC2 instance didn't start.");
                    }
                    return;
                } else {//size >=2
                    booted = booteds.remove(0);
                    for (Instance toDestroy : booteds) {
                        //creating deploying nodes to notify the user graphically about the issue which could cost money...
                        String lostNodeURL = super.addDeployingNode(clb.getNodeName(), cmd,
                                "An EC2 instance started but wasn't required.", TIMEOUT_DELAY);
                        String lostDescription = "An EC2 instance started but wasn't required. This instance is now powered off.";
                        if (!this.ec2d.terminateInstance(toDestroy)) {
                            logger.error("Cannot terminate the instance " + toDestroy.getInstanceId() +
                                " with ip " + this.ec2d.getInstanceHostname(toDestroy.getInstanceId()) +
                                ", started whereas not expected. Terminate it by yourself.");
                            lostDescription = "An EC2 instance started but wasn't required and we weren't able to terminate it." +
                                System.getProperty("line.separator");
                            lostDescription += "Terminate the instance " + toDestroy.getInstanceId() +
                                " with ip " + this.ec2d.getInstanceHostname(toDestroy.getInstanceId()) +
                                " by yourself";
                        }
                        super.declareDeployingNodeLost(lostNodeURL, lostDescription);
                    }
                }
            } else {
                booted = booteds.remove(0);
            }
            //booted != null
            this.deployingNodeToInstance.put(deployingNodeURL, booted);
            this.nodeNameToInstance.put(clb.getNodeName(), booted);
            logger.info("New EC2 instance started");
        } catch (ProActiveException e) {
            this.handledStartInstanceException(e, deployingNodeURL);
        } catch (IOException e) {
            //thrown by cmb.buildCommand(), deployingNodeURL cannot be != null but safe check
            this.handledStartInstanceException(e, deployingNodeURL);
        }
    }

    /**
     * Treat a throwable thrown during ec2 instance start procedure, rethrow a RM Exception after logging the throwable
     * and declare the associated pendingNodeURL Lost...
     * @param e the throwable
     * @param deployingNodeURL the url of the pending node if it exists ( can be null )
     * @throws RMException the throwable encapsulated in a rm exception
     */
    private void handledStartInstanceException(Throwable e, String deployingNodeURL) throws RMException {
        logger.error("Unable to acquire EC2 instance", e);
        if (deployingNodeURL != null) {
            String lf = System.getProperty("line.separator");
            super.declareDeployingNodeLost(deployingNodeURL,
                    "Cannot deploy a new EC2 instance because of an Exception" + lf +
                        "Be sure that the instance is effectively powered off" + lf + Utils.getStacktrace(e));
        }
        throw new RMException("Cannot start a new EC2 instance", e);
    }

    /**
     * Builds the command that will be launched in the EC2 instance
     * @return The command line builder correctly set
     */
    private CommandLineBuilder buildEC2CommandLine() {
        CommandLineBuilder result = super.getEmptyCommandLineBuilder();

        ImageDescription descr = ec2d.getAvailableImages(this.imgd, true);
        result.setRmURL(this.rmUrl);
        result.setCredentialsValueAndNullOthers(this.creds64);
        result.setJavaPath(REMOTE_JAVA_EXE);
        Map<String, String> properties = new HashMap<String, String>();
        properties
                .put(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName(), communicationProtocol);
        result.setPaProperties(properties);
        String nodeSourceName = this.nodeSource.getName();
        result.setSourceName(nodeSourceName);
        String nodeName = "EC2-" + nodeSourceName + "-node-" + ProActiveCounter.getUniqID();

        if (descr.getPlatform().contains("windows")) {
            result.setTargetOS(OperatingSystem.WINDOWS);
            result.setRmHome(REMOTE_RM_HOME_WIN);
            properties.put(PAResourceManagerProperties.RM_HOME.getKey(), REMOTE_RM_HOME_WIN);
            properties.put(CentralPAPropertyRepository.PA_HOME.getName(), REMOTE_RM_HOME_WIN);
        } else {
            result.setTargetOS(OperatingSystem.UNIX);
            result.setRmHome(REMOTE_RM_HOME_UNIX);
            properties.put(PAResourceManagerProperties.RM_HOME.getKey(), REMOTE_RM_HOME_UNIX);
            properties.put(CentralPAPropertyRepository.PA_HOME.getName(), REMOTE_RM_HOME_UNIX);
        }

        result.setNodeName(nodeName);
        return result;
    }

    /**
     * Configures the Infrastructure
     *
     * @param parameters
     *            parameters[0]: Configuration file as byte array
     *            parameters[1]: RM credentials
     *            parameters[2]: communication protocol
     *            parameters[3]: additional jvm args
     *            parameters[4]: EC2 server URL
     */
    @Override
    public void configure(Object... parameters) {
        /** parameters look fine */
        if (parameters != null && parameters.length == 4) {

            // ======  parameters[0]: Configuration file as byte array ======

            if (parameters[0] == null) {
                throw new IllegalArgumentException("EC2 config file must be specified");
            }

            try {
                //                File ff = new File(parameters[0].toString());
                byte[] configFile = (byte[]) parameters[0];

                File ff = File.createTempFile("configFile", "props");
                FileToBytesConverter.convertByteArrayToFile(configFile, ff);
                readConf(ff.getAbsolutePath());
                ff.delete();
            } catch (Exception e) {
                readConf(PAResourceManagerProperties.RM_EC2_PATH_PROPERTY_NAME.getValueAsString());
                logger.error("Expected File as 1st parameter for EC2Infrastructure: " + e.getMessage(), e);
            }

            // ============= parameters[1]: RM credentials ==============
            if (parameters[1] == null) {
                throw new IllegalArgumentException("Credentials must be specified");
            }
            this.creds64 = new String((byte[]) parameters[1]);

            // ================= parameters[2]: communication protocol ==============
            //TODO: check known protocol 
            if (parameters[2] == null) {

                throw new IllegalArgumentException("Communication protocol must be specified");
            }

            this.communicationProtocol = parameters[2].toString();

            // ================= parameters[3]: additional jvm args =================
            if (parameters[3] == null) {
                this.additionalJVMOptions = "";
            } else
                this.additionalJVMOptions = parameters[3].toString();
        }

        /**
         * missing or absent parameters, aborting
         */
        else {
            throw new IllegalArgumentException("Invalid parameters for EC2Infrastructure creation");
        }

    }

    /**
     * Removes a node from this im. If the holding EC2 instance doesn't contain more node,
     * terminates it.
     */
    @Override
    public void removeNode(Node node) throws RMException {
        String nodeName = node.getNodeInformation().getName();
        Instance instance = nodeNameToInstance.remove(nodeName);
        if (instance == null) {
            //we don't find the instance, trying to kill it with its dns name
            logger.warn("Cannot find the ec2 instance associated with the node " + nodeName +
                ". Trying to recover...");
            synchronized (this.ec2d) {
                InetAddress addr = node.getVMInformation().getInetAddress();
                if (this.ec2d.terminateInstanceByAddr(addr)) {
                    logger.info("Instance closed: " + addr.toString());
                } else {
                    logger.error("Could not close instance: " + addr.toString());
                }
            }
        } else {
            //we found the instance we deployed for this node
            String instanceID = instance.getInstanceId();
            String instanceIP = this.ec2d.getInstanceHostname(instanceID);
            if (this.ec2d.terminateInstance(instance)) {
                logger.info("Instance closed: " + instanceID + " with ip " + instanceIP);
                return;
            } else {
                logger.error("Could not close instance: " + instanceID + " with ip " + instanceIP);
            }
        }
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
    private void readConf(String path) {
        File fp = new File(path);

        if (!fp.isAbsolute()) {
            fp = new File(PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator + path);
            if (!fp.exists()) {
                throw new IllegalArgumentException("Could not find configuration file: " + path);
            }
        }

        Properties props = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(fp);
            props.load(in);
            in.close();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while reading EC2 properties: " + e.getMessage(), e);
        }

        /** check all mandatory fields are present */
        if (!props.containsKey("AWS_AKEY"))
            throw new IllegalArgumentException("Missing property AWS_AKEY in: " + path);
        if (!props.containsKey("AWS_SKEY"))
            throw new IllegalArgumentException("Missing property AWS_SKEY in: " + path);
        if (!props.containsKey("AWS_USER"))
            throw new IllegalArgumentException("Missing property AWS_USER in: " + path);
        if (!props.containsKey("AMI"))
            throw new IllegalArgumentException("Missing property AMI in: " + path);
        if (!props.containsKey("INSTANCE_TYPE"))
            throw new IllegalArgumentException("Missing property INSTANCE_TYPE: " + path);
        if (!props.containsKey("MAX_INST"))
            throw new IllegalArgumentException("Missing property MAX_INST in: " + path);

        String logInfo = "*************************************** \n";
        logInfo += "EC2 configuratioin: \n";
        this.ec2d = new EC2Deployer(props.getProperty("AWS_AKEY"), props.getProperty("AWS_SKEY"), props
                .getProperty("AWS_USER"));
        logInfo += "AWS_USER=" + props.getProperty("AWS_USER") + "\n";
        logInfo += "ASW_AKEY=" + props.getProperty("AWS_AKEY") + "\n";

        this.imgd = props.getProperty("AMI");
        logInfo += "AMI=" + props.getProperty("AMI") + "\n";
        int maxInsts = 1;
        try {
            maxInsts = Integer.parseInt(props.getProperty("MAX_INST"));
            logInfo += "MAX_INST=" + props.getProperty("MAX_INST") + "\n";

        } catch (Exception e) {
            maxInsts = 1;
            logInfo += "Exception occured while parsing MAX_INST value " + props.getProperty("MAX_INST") +
                "\n";
            logInfo += "MAX_INST=1 \n";
        }

        this.ec2d.setNumInstances(1, maxInsts);
        this.ec2d.setInstanceType(props.getProperty("INSTANCE_TYPE"));
        logInfo += "INSTANCE_TYPE=" + props.getProperty("INSTANCE_TYPE") + "\n";

        String ec2Url = null;
        if (props.containsKey("EC2_HOST")) {
            ec2Url = props.getProperty("EC2_HOST");

        }
        if (ec2Url != null) {
            ec2d.setEc2RegionHost(ec2Url);
            logInfo += "EC2_HOST=" + props.getProperty("EC2_HOST") + "\n";
        }

        logInfo += "*************************************** \n";
        //        System.out.println(logInfo);
        logger.info(logInfo);

        try {
            in.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "EC2 Infrastructure";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAcquiredNode(Node node) throws RMException {
        //nothing to do here, safe check...
        synchronized (this.ec2d) {
            String nodeName = node.getNodeInformation().getName();
            Instance instance = nodeNameToInstance.get(nodeName);
            if (instance == null) {
                logger
                        .warn("Node " + nodeName +
                            " registered but isn't associated with any instance... trying to determine it by its IP address");
                InetAddress nodeAddr = node.getVMInformation().getInetAddress();
                for (Entry<String, Instance> entry : nodeNameToInstance.entrySet()) {
                    Instance inst = entry.getValue();
                    String ec2Host = null;
                    try {
                        ec2Host = this.ec2d.getInstanceHostname(inst.getInstanceId());
                        if (ec2Host.equals("")) {
                            logger.warn("Cannot determine hostname of EC2 instance " + inst.getInstanceId() +
                                ". Ignoring it.");
                            continue;
                        }
                        InetAddress ec2Addr = InetAddress.getByName(ec2Host);
                        if (nodeAddr.equals(ec2Addr)) {
                            logger.info("Found requested EC2 instance " + inst.getInstanceId() + " with ip " +
                                ec2Host + " for node " + nodeName);
                            return;
                        }
                    } catch (Exception e) {
                        logger.error("Cannot determine IP address of EC2 instance " + inst.getInstanceId() +
                            ": " + ec2Host, e);
                    }
                }
                throw new RMException("Cannot find EC2 instance holding node " +
                    node.getNodeInformation().getURL() + ". Discarding id.");
            } else {
                logger.info("Node " + nodeName + " registered and is associated to EC2 instance " +
                    instance.getInstanceId() + " with ip " +
                    this.ec2d.getInstanceHostname(instance.getInstanceId()));
            }
        }
    }

    /**
     * Cleanup deployed instances,
     * so that instances that did not register to the nodesource be removed as well
     * 
     * @see org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.InfrastructureManager#shutDown()
     */
    @Override
    public void shutDown() {
        synchronized (this.ec2d) {
            int ret = this.ec2d.terminateAll();
            if (ret > 0) {
                logger.info("Terminated " + ret + " EC2 nodes.");
            }
        }
    }

    /**
     * If the deploying node is removed or if a timeout occurred, this method is called by
     * parent im. We benefit from this to terminate the associated EC2 instance
     */
    @Override
    protected void notifyDeployingNodeLost(String nodeURL) {
        Instance toRemove = this.deployingNodeToInstance.remove(nodeURL);
        if (toRemove != null) {
            //terminate the instance which is not expected anymore
            synchronized (this.ec2d) {
                if (!ec2d.terminateInstance(toRemove)) {
                    logger.warn("Cannot terminate the EC2 instance " + toRemove.getInstanceId() +
                        " with ip " + this.ec2d.getInstanceHostname(toRemove.getInstanceId()) +
                        ". Do it manually.");
                }
            }
        } else {
            logger.warn("Deploying node removal not associated with any EC2 instance.");
        }
    }
}
