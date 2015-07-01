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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.io.IOException;
import java.security.KeyException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.CommandLineBuilder;
import org.ow2.proactive.resourcemanager.utils.OperatingSystem;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.apache.axiom.om.OMElement;
import org.ggf.schemas.bes._2006._08.bes_factory.HPCBPServiceStub.ActivityStateEnumeration;
import org.ggf.schemas.bes._2006._08.bes_factory.HPCBPServiceStub.EndpointReferenceType;
import org.ggf.schemas.bes._2006._08.bes_factory.HPCBPServiceStub.GetActivityStatusResponseType;
import org.ggf.schemas.bes._2006._08.bes_factory.HPCBPServiceStub.ReferenceParametersType;


public class WinHPCInfrastructure extends DefaultInfrastructureManager {

    /**
     * maximum number of nodes this infrastructure can ask simultaneously to the WinHPC scheduler
     */
    @Configurable(description = "Maximum number of nodes to deploy")
    protected int maxNodes = 1;
    /** The atomic value of max nodes */
    protected AtomicInteger atomicMaxNodes = null;

    /**
     * A url of HPC basic profile web service
     */
    @Configurable(description = "Url of the WinHPC web service")
    String serviceUrl = "https://<computerName>/HPCBasicProfile";

    @Configurable(description = "Username for windows scheduler connection")
    private String userName;

    @Configurable(password = true, description = "Password for windows scheduler connection")
    private String password;

    @Configurable(fileBrowser = true, description = "Name of the trustStore")
    private String trustStore;

    @Configurable(password = true, description = "Password for the trustStore")
    private String trustStorePassword;

    /**
     * Path to the Java executable on the remote hosts
     */
    @Configurable(description = "Absolute path of the java\nexecutable on the remote hosts")
    protected String javaPath = System.getProperty("java.home") + "/bin/java";

    /**
     * Path to the Resource Manager installation on the remote hosts
     */
    @Configurable(description = "Absolute path of the Resource Manager\nroot directory on the remote hosts")
    protected String rmPath = PAResourceManagerProperties.RM_HOME.getValueAsString();

    /**
     * Path to the credentials file user for RM authentication
     */
    @Configurable(credential = true, description = "Absolute path of the credential file")
    protected File RMCredentialsPath;

    /**
     * Additional java options to append to the command executed on the remote host
     */
    @Configurable(description = "Options for the java command\nlaunching the node on the remote hosts")
    protected String javaOptions;

    @Configurable(description = "Additional classpath for the java command\nlaunching the node on the remote hosts")
    protected String extraClassPath;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    protected Integer timeout = 60 * 1000;//1 mn

    /** Credentials used by remote nodes to register to the NS */
    private Credentials credentials = null;
    /** The credentials value as a string */
    private String credBase64 = null;
    /** the path of the trust store which holds hpc server's certificate */
    private String trustStorePath;
    /** the list of submitted jobs */
    private Map<String, EndpointReferenceType[]> submittedJobs = new Hashtable<String, EndpointReferenceType[]>();
    /** ensures that the deploying node's timeout is not finished */
    private Map<String, Boolean> dnTimeout = new Hashtable<String, Boolean>();
    /** to retrieve job's data from deploying node's url */
    private Map<String, EndpointReferenceType[]> deployingNodeToEndpoint = new Hashtable<String, EndpointReferenceType[]>();
    /** the deployer instance */
    private transient org.ow2.proactive.resourcemanager.nodesource.infrastructure.WinHPCDeployer deployer;
    /** the refresh rate of the job's state in ms */
    private static final int JOB_STATE_REFRESH_RATE = 1000;

    /** threshold of retry in case of error/retry */
    private static final int ERROR_HANDLE_THRESHOLD = 5;

    @Override
    public void acquireAllNodes() {
        while (this.atomicMaxNodes.getAndDecrement() >= 1) {
            acquireNodeImpl();
        }
        //we decremented one too many
        this.atomicMaxNodes.getAndIncrement();
        logger.debug("Maximum number of node acquisition reached");
    }

    @Override
    public void acquireNode() {
        if (this.atomicMaxNodes.getAndDecrement() >= 1) {
            acquireNodeImpl();
        } else {
            //one decremented once too many
            this.atomicMaxNodes.getAndIncrement();
            logger.debug("Maximum number of node acquisition reached");
        }
        synchronized (submittedJobs) {
            if (submittedJobs.size() >= maxNodes) {
                logger.warn("Attempting to acquire nodes while maximum reached: max nodes " + maxNodes +
                    ", current nodes " + submittedJobs.size());
                return;
            }
        }

    }

    /** async node acquisition implementation */
    private void acquireNodeImpl() {
        nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    startNode();
                } catch (Exception e) {
                    //could handle the exception here to increment the number of available nodes
                    //or get it done in the handleFailedDeployment* methods...
                    logger.error("Could not acquire node ", e);
                    return;
                }
            }
        });
    }

    /**
     * Queues a new command which starts a new node.
     */
    private void startNode() throws RMException {
        EndpointReferenceType[] eprs = new EndpointReferenceType[1];
        //Creates the command line builder
        CommandLineBuilder clb = this.getCommandLineBuilder();
        String nodeName = clb.getNodeName();
        //Generate the HPCBP acitivty from Axis2 generated JSDL objects
        //escaping the built command if contains quotes
        String fullCommand = null;
        String obfuscatedFullCommand = null;
        try {
            fullCommand = "cmd /C \" " + clb.buildCommandLine(true).replace("\"", "\\\"") + " \"";
            obfuscatedFullCommand = "cmd /C \" " + clb.buildCommandLine(false).replace("\"", "\\\"") + " \"";
        } catch (IOException e) {
            this.handleFailedDeployment(clb, e);
        }

        String dNode = super.addDeployingNode(nodeName, obfuscatedFullCommand,
                "Node deployment on Windows HPC", timeout);
        //we add the timeout flag
        this.dnTimeout.put(dNode, false);
        this.submittedJobs.put(nodeName, eprs);
        this.deployingNodeToEndpoint.put(dNode, eprs);

        logger.debug("Executing: " + fullCommand);
        try {
            eprs[0] = this.getDeployer().createActivity(
                    org.ow2.proactive.resourcemanager.nodesource.infrastructure.WinHPCDeployer
                            .createJSDLDocument(fullCommand));
        } catch (Exception e) {
            this.handleFailedDeployment(dNode, clb, e);
        }

        ReferenceParametersType rps = eprs[0].getReferenceParameters();
        OMElement[] elements = rps.getExtraElement();
        if (logger.isDebugEnabled()) {
            for (int i = 0; i < elements.length; i++) {
                logger.debug(elements[i].toString());
            }
        }

        // getting job status to detect failed jobs
        GetActivityStatusResponseType[] status = null;
        String statusString = null;
        int threshold = WinHPCInfrastructure.ERROR_HANDLE_THRESHOLD;
        Throwable thresholdCause = null;
        Boolean hasTimeouted = false;
        do {
            try {
                Thread.sleep(WinHPCInfrastructure.JOB_STATE_REFRESH_RATE);
            } catch (InterruptedException e) {
                threshold--;
                thresholdCause = e;
            }
            try {
                status = this.getDeployer().getActivityStatuses(eprs);
            } catch (RMException rmex) {
                threshold--;
                thresholdCause = rmex;
            }
            String currentStatus = status[0].getActivityStatus().getState().toString();
            if (logger.isDebugEnabled()) {
                logger.debug("Node " + nodeName + " deployment status - " + currentStatus);
            }
            if (status[0].getActivityStatus().getState() == ActivityStateEnumeration.Failed) {
                // job failed
                this.handleFailedDeployment(dNode, clb, "The job's status is failed.");
            }
            //if the status changed, we update it
            if (currentStatus != null && !currentStatus.equals(statusString)) {
                statusString = currentStatus;
                super.updateDeployingNodeDescription(dNode, "Node deployment on Windows HPC" +
                    System.lineSeparator() + "job's status: " + statusString);
            }

            if (super.checkNodeIsAcquiredAndDo(nodeName, null, null)) {
                //node has been acquired, can exit
                return;
            } else {
                //waiting
            }
        } while (((hasTimeouted = this.dnTimeout.get(dNode)) != null) && !hasTimeouted && threshold > 0);

        //we exited the loop because of the threshold
        if (threshold <= 0) {
            this.handleFailedDeployment(dNode, clb, thresholdCause);
        }

        //if we exit because of a timeout
        if (hasTimeouted != null && hasTimeouted) {
            //we remove it
            this.dnTimeout.remove(dNode);
            //has been terminated during the deploying node removal
            this.submittedJobs.remove(clb.getNodeName());
            throw new RMException("Deploying Node " + nodeName + " not expected any more");
        }
    }

    /**
     * Terminates the job associated with this deploying node.
     */
    @Override
    protected void notifyDeployingNodeLost(String pnURL) {
        //we notify the control loop to exit
        logger.debug("Terminating the job for node " + pnURL);
        this.dnTimeout.put(pnURL, true);
        //we remove the job
        EndpointReferenceType[] epr = this.deployingNodeToEndpoint.remove(pnURL);
        if (epr != null) {
            try {
                this.getDeployer().terminateActivity(epr);
            } catch (RMException e) {
                logger.error("Cannot terminate the job associated with deploying node " + pnURL, e);
            }
        }
    }

    /**
     * Changes the status of the deploying node passed as parameter
     * and cleanup of internal maintained structures.
     * @param dNode
     * @param clb
     * @param cause
     * @throws RMException
     */
    private void handleFailedDeployment(String dNode, CommandLineBuilder clb, String cause)
            throws RMException {
        String nodeName = clb.getNodeName();
        this.submittedJobs.remove(nodeName);
        this.dnTimeout.remove(dNode);
        super.declareDeployingNodeLost(dNode, cause);
        throw new RMException("The job's status is failed.");
    }

    /**
     * Changes the status of the deploying node to lost an updates its description
     * @param dNode The deploying node's url to update
     * @param e the exception that caused the deployment to fail
     * @throws RMException
     */
    private void handleFailedDeployment(String dNode, CommandLineBuilder clb, Throwable e) throws RMException {
        String error = Utils.getStacktrace(e);
        super.declareDeployingNodeLost(dNode, "The deployment failed because of an error: " +
            System.lineSeparator() + error);
        String nodeName = clb.getNodeName();
        this.submittedJobs.remove(nodeName);
        this.dnTimeout.remove(dNode);
        throw new RMException("The deployment failed because of an error", e);
    }

    /**
     * Creates a lost node to notify the user that the deployment
     * faile because of an error
     * @param clb
     * @param e the error that caused the deployment to failed.
     * @throws RMException
     */
    private void handleFailedDeployment(CommandLineBuilder clb, Throwable e) throws RMException {
        String error = Utils.getStacktrace(e);
        String command = null;
        try {
            command = clb.buildCommandLine(false);
        } catch (Exception ex) {
            command = "Cannot determine the command used to start the node.";
        }
        String lostNode = super.addDeployingNode(clb.getNodeName(), command,
                "Cannot deploy the node because of an error:" + System.lineSeparator() + error,
                60000);
        super.declareDeployingNodeLost(lostNode, null);
        String nodeName = clb.getNodeName();
        this.submittedJobs.remove(nodeName);
        throw new RMException("The deployment failed because of an error", e);
    }

    /**
     * parameters[0] = maxNodes
     * parameters[1] = serviceUrl
     * parameters[2] = username
     * parameters[3] = password
     * parameters[4] = keystore
     * parameters[5] = keystore's password
     * parameters[6] = java path
     * parameters[7] = rmPath
     * parameters[8] = credentials
     * parameters[9] = java options
     * parameters[10] = extra classpath
     * parameters[11] = timeout
     */
    @Override
    public void configure(Object... parameters) {
        try {
            this.maxNodes = Integer.parseInt(parameters[0].toString());
            this.atomicMaxNodes = new AtomicInteger(this.maxNodes);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Max Nodes value has to be integer");
        }
        this.serviceUrl = parameters[1].toString();
        this.userName = parameters[2].toString();
        this.password = parameters[3].toString();

        try {
            String dir = System.getProperty("java.io.tmpdir");
            File file = new File(dir, "castore");// + randomString());
            logger.info("Saving trust store file to " + file.getAbsolutePath());
            FileToBytesConverter.convertByteArrayToFile((byte[]) parameters[4], file);
            this.trustStorePath = file.getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot save trust store file", e);
        }

        this.trustStorePassword = parameters[5].toString();

        this.javaPath = parameters[6].toString();
        this.rmPath = parameters[7].toString();

        try {
            this.credentials = Credentials.getCredentialsBase64((byte[]) parameters[8]);
            this.credBase64 = new String(this.credentials.getBase64());
        } catch (KeyException e) {
            throw new IllegalArgumentException("Could not retrieve base64 credentials", e);
        }

        this.javaOptions = parameters[9].toString();
        this.extraClassPath = parameters[10].toString();

        try {
            this.timeout = Integer.parseInt(parameters[11].toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Timeout value must be an number");
        }

        // Set up the environment for the SSL verificaton
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", trustStorePassword);
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    @Override
    public void shutDown() {
        new File(trustStorePath).delete();
        for (EndpointReferenceType[] ert : submittedJobs.values()) {
            try {
                this.getDeployer().terminateActivity(ert);
            } catch (Exception e) {
                logger.warn("Cannot remove file " + trustStorePath);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeNode(Node node) throws RMException {
        // the job will be finished when JVM is killed
        String nodeName = node.getNodeInformation().getName();
        logger.debug("Removing node " + nodeName);
        if (submittedJobs.remove(nodeName) != null) {
            this.atomicMaxNodes.incrementAndGet();
            //the job is automatically finished
        } else {
            logger.warn("Unknown node " + node.getNodeInformation().getName());
        }
        final Node n = node;
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    n.getProActiveRuntime().killRT(false);
                } catch (Exception e) {
                    logger.trace("An exception occurred during node removal", e);
                }
            }
        });
    }

    public String getDescription() {
        return "Windows HPC infrasturcure";
    }

    /**
     * Builds the command line builder to be used for the remote node
     * startup
     * @return The appropriate command line builder
     */
    @SuppressWarnings("deprecation")
    private CommandLineBuilder getCommandLineBuilder() {
        CommandLineBuilder result = super.getEmptyCommandLineBuilder();
        result.setTargetOS(OperatingSystem.WINDOWS);
        result.setRmHome(this.rmPath);
        result.setJavaPath(this.javaPath);
        //not only user provided...
        StringBuilder sb = new StringBuilder();
        sb.append(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine());
        sb.append(this.rmPath);
        sb.append(OperatingSystem.WINDOWS.fs);
        sb.append("config");
        sb.append(OperatingSystem.WINDOWS.fs);
        sb.append("security.java.policy-client ");
        sb.append(this.javaOptions);
        result.setPaProperties(sb.toString());
        result.setCredentialsValueAndNullOthers(this.credBase64);
        result.setRmURL(super.rmUrl);
        result.setSourceName(this.nodeSource.getName());
        result.setNodeName("WINHPC-" + result.getSourceName() + "-" + ProActiveCounter.getUniqID());
        return result;
    }

    /**
     * @return the win hpc deployer instance
     * @throws RMException
     */
    private synchronized org.ow2.proactive.resourcemanager.nodesource.infrastructure.WinHPCDeployer getDeployer()
            throws RMException {
        if (this.deployer == null) {
            try {
                this.deployer = new org.ow2.proactive.resourcemanager.nodesource.infrastructure.WinHPCDeployer(
                    new File(PAResourceManagerProperties.RM_HOME.getValueAsString(), "config" +
                        File.separator + "rm" + File.separator + "deployment" + File.separator + "winhpc" +
                        File.separator).getAbsolutePath(), serviceUrl, userName, password);
            } catch (Exception e) {
                throw new RMException("Cannot instantiate the win hpc deployer", e);
            }
        }
        return this.deployer;
    }
}
