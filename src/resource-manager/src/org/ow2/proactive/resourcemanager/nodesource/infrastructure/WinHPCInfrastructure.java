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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.security.KeyException;
import java.util.HashMap;
import java.util.Random;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;
import org.ggf.schemas.bes._2006._08.bes_factory.HPCBPServiceStub.ActivityStateEnumeration;
import org.ggf.schemas.bes._2006._08.bes_factory.HPCBPServiceStub.EndpointReferenceType;
import org.ggf.schemas.bes._2006._08.bes_factory.HPCBPServiceStub.GetActivityStatusResponseType;
import org.ggf.schemas.bes._2006._08.bes_factory.HPCBPServiceStub.ReferenceParametersType;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.utils.FileToBytesConverter;


public class WinHPCInfrastructure extends DefaultInfrastructureManager {

    /** logger */
    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);

    /**
     * maximum number of nodes this infrastructure can ask simultaneously to the WinHPC scheduler
     */
    @Configurable(description = "Maximum number of nodes to deploy")
    protected int maxNodes = 1;

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
     * URL of the resource manager the newly created nodes will attempt to contact
     */
    @Configurable(description = "URL of the Resource Manager")
    protected String RMUrl;

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

    /**
     * Credentials used by remote nodes to register to the NS
     */
    private Credentials credentials = null;

    private String trustStorePath;

    private String command = "";

    private HashMap<String, EndpointReferenceType[]> submittedJobs = new HashMap<String, EndpointReferenceType[]>();

    private WinHPCDeployer deployer;

    @Override
    public void acquireAllNodes() {
        int cur = submittedJobs.size();
        for (int i = 0; i < (maxNodes - cur); i++) {
            acquireNode();
        }
    }

    @Override
    public void acquireNode() {

        synchronized (submittedJobs) {
            if (submittedJobs.size() >= maxNodes) {
                logger.warn("Attempting to acquire nodes while maximum reached: max nodes " + maxNodes +
                    ", current nodes " + submittedJobs.size());
                return;
            }
        }

        // Set up the environment for the SSL verificaton
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", trustStorePassword);
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        try {
            deployer = new WinHPCDeployer(PAResourceManagerProperties.RM_HOME.getValueAsString() +
                "/config/rm/deployment/winhpc/", serviceUrl, userName, password);
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
        }

        if (deployer != null) {
            nodeSource.executeInParallel(new Runnable() {
                public void run() {
                    try {
                        startNode();
                    } catch (Exception e) {
                        logger.error("Could not acquire node ", e);
                        return;
                    }
                }
            });
        }
    }

    public void startNode() {

        try {
            // Set up the environment for the SSL verificaton
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.keyStorePassword", trustStorePassword);
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");

            deployer = new WinHPCDeployer(PAResourceManagerProperties.RM_HOME.getValueAsString() +
                "/config/rm/deployment/winhpc/", serviceUrl, userName, password);

            EndpointReferenceType[] eprs = new EndpointReferenceType[1];

            // Generate the HPCBP acitivty from Axis2 generated JSDL objects
            String nodeName = "WINHPC-" + nodeSource.getName() + "-" + randomString();
            String fullCommand = command + " -n " + nodeName;
            fullCommand += " -s " + nodeSource.getName();
            fullCommand = "cmd /C \" " + fullCommand + " \"";

            synchronized (submittedJobs) {
                if (submittedJobs.size() >= maxNodes) {
                    logger.warn("Attempting to acquire nodes while maximum reached: max nodes " + maxNodes +
                        ", current nodes " + submittedJobs.size());
                    return;
                } else {
                    submittedJobs.put(nodeName, eprs);
                }
            }

            logger.debug("Executing: " + fullCommand);
            eprs[0] = deployer.createActivity(WinHPCDeployer.createJSDLDocument(fullCommand));

            if (eprs[0] == null) {
                logger.warn("There was a problem creating the activity on the HPCBP web service.");
                return;
            }

            ReferenceParametersType rps = eprs[0].getReferenceParameters();
            OMElement[] elements = rps.getExtraElement();
            for (int i = 0; i < elements.length; i++) {
                logger.info(elements[i].toString());
            }

            // getting job status to detect failed jobs
            GetActivityStatusResponseType[] status = null;
            long timeStamp = System.currentTimeMillis();
            long timeout = 5000; // 1 sec
            do {
                Thread.currentThread().sleep(1000);
                status = deployer.getActivityStatuses(eprs);
                logger.debug("Node " + nodeName + " deployment status - " +
                    status[0].getActivityStatus().getState().toString());
                if (status[0].getActivityStatus().getState() == ActivityStateEnumeration.Failed) {
                    // job failed
                    synchronized (submittedJobs) {
                        submittedJobs.remove(nodeName);
                    }
                    break;
                }
                if (timeout > 0) {
                    timeout -= System.currentTimeMillis() - timeStamp;
                    timeStamp = System.currentTimeMillis();
                } else {
                    // did not detect failed job after 5 sec
                    // probably it is queued
                    break;
                }
            } while (status[0].getActivityStatus().getState() == ActivityStateEnumeration.Pending);

        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @Override
    public BooleanWrapper configure(Object... parameters) {

        try {
            maxNodes = Integer.parseInt(parameters[0].toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Max Nodes value has to be integer");
        }
        serviceUrl = parameters[1].toString();
        userName = parameters[2].toString();
        password = parameters[3].toString();

        try {
            String dir = System.getProperty("java.io.tmpdir");
            if (dir == null) {
                dir = "/tmp";
            }
            File file = new File(dir + "/castore");// + randomString());
            logger.info("Saving trust store file to " + file.getAbsolutePath());
            FileToBytesConverter.convertByteArrayToFile((byte[]) parameters[4], file);
            trustStorePath = file.getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot save trust store file", e);
        }

        trustStorePassword = parameters[5].toString();

        javaPath = parameters[6].toString();
        rmPath = parameters[7].toString();
        RMUrl = parameters[8].toString();

        try {
            this.credentials = Credentials.getCredentialsBase64((byte[]) parameters[9]);
        } catch (KeyException e) {
            throw new IllegalArgumentException("Could not retrieve base64 credentials", e);
        }

        javaOptions = parameters[10].toString();
        extraClassPath = parameters[11].toString();

        String classpath = rmPath + "/dist/lib/ProActive.jar;";
        classpath += rmPath + "/dist/lib/ProActive_ResourceManager.jar;";
        classpath += rmPath + "/dist/lib/ProActive_Scheduler-worker.jar;";
        classpath += rmPath + "/dist/lib/ProActive_SRM-common.jar;";
        classpath += rmPath + "/dist/lib/script-js.jar;";
        classpath += rmPath + "/dist/lib/jruby-engine.jar;";
        classpath += rmPath + "/dist/lib/jython-engine.jar;";
        classpath += rmPath + "/dist/lib/commons-logging-1.0.4.jar";

        if (extraClassPath.length() > 0) {
            classpath += ";" + extraClassPath;
        }

        command = this.javaPath + " -cp " + classpath;
        command += " " + CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine();
        command += rmPath + "/config/security.java.policy-server";
        command += " -Dproactive.useIPaddress=true ";
        command += " " + this.javaOptions + " ";
        command += " org.ow2.proactive.resourcemanager.utils.PAAgentServiceRMStarter ";

        command += " -r " + RMUrl;

        try {
            command += " -v " + new String(this.credentials.getBase64()) + " ";
        } catch (KeyException e1) {
            throw new IllegalArgumentException("Could not get base64 credentials", e1);
        }

        return new BooleanWrapper(true);
    }

    protected String randomString() {
        Random rand = new Random();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String nn = "";
        for (int i = 0; i < 16; i++) {
            nn += alpha.charAt(rand.nextInt(alpha.length()));
        }
        return nn;
    }

    @Override
    public void shutDown() {
        try {
            new File(trustStorePath).delete();

            synchronized (submittedJobs) {
                for (EndpointReferenceType[] ert : submittedJobs.values()) {
                    if (deployer != null) {
                        deployer.terminateActivity(ert);
                    } else {
                        logger.error("Win HPC deployer cannot be null");
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Cannot remove file " + trustStorePath);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeNode(Node node) throws RMException {
        // the job will be finished when JVM is killed
        synchronized (submittedJobs) {
            logger.debug("Removing node " + node.getNodeInformation().getName());
            if (submittedJobs.containsKey(node.getNodeInformation().getName())) {
                submittedJobs.remove(node.getNodeInformation().getName());
            } else {
                logger.warn("Unknown node " + node.getNodeInformation().getName());
            }
        }
        super.removeNode(node);
    }

    public String getDescription() {
        return "Windows HPC infrasturcure";
    }
}
