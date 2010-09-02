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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.ssh.SSHClient;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.PAAgentServiceRMStarter;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * This class implements the basics common operations that can be performed on a resource manager (i.e batching job system like PBS, Torque, LSF...).
 * It ensures that the internal number of nodes is coherent regarding acquisition a removal requests after timeouts occur. For instance, even if a {@link #getDeleteJobCommand()}
 * fails, the node is removed from the core anyway, if the SSH command to the frontend or {@link #getSubmitJobCommand()} fail, this IM ensure that after the {@link #nodeTimeOut} occurs
 * no more nodes will be registered (this IM maintains an internal "black list").<br /><br />
 *
 * Service Providers have to implements 3 methods:<br />
 * <ul>
 * 	<li>{@link #getBatchinJobSystemName()}: The name of the target resource manager, PBS, Torque, LSF are such examples. The returned string is not really significant, it is only used to build meaningful nodes' name or for logging.</li>
 *  <li>{@link #getDeleteJobCommand()}: The command required to delete a job on the target resource manager.</li>
 *  <li>{@link #getSubmitJobCommand()}: The command required to submit a new job.</li>
 * </ul>
 */
public abstract class BatchJobInfrastructure extends InfrastructureManager {
    /**
     * Path to the Java executable on the remote hosts
     */
    @Configurable(description = "Absolute path of the java\nexecutable on the remote hosts")
    protected String javaPath = System.getProperty("java.home") + "/bin/java";

    /**
     * Use the Java from JAVA_HOME if defined
     */
    {
        String jhome = System.getenv("JAVA_HOME");
        File f = new File(jhome);
        if (f.exists() && f.isDirectory()) {
            javaPath = jhome + ((jhome.endsWith("/")) ? "" : "/") + "bin/java";
        }
    }

    /**
     * ShhClient options (@see {@link SSHClient})
     */
    @Configurable(description = "Options of the ssh command used\nto log in the batch system head node")
    protected String sshOptions;
    /**
     * Path to the Scheduling installation on the remote hosts
     */
    @Configurable(description = "Absolute path of the Resource Manager\nroot directory on the remote hosts")
    protected String schedulingPath = PAResourceManagerProperties.RM_HOME.getValueAsString();
    /**
     * Additional java options to append to the command executed on the remote host
     */
    @Configurable(description = "Options used by the java command\nlaunching the node on the remote hosts")
    protected String javaOptions = CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.isSet() ? CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL
            .getCmdLine() +
        CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue()
            : "";
    /**
     * maximum number of nodes this infrastructure can ask simultaneously to the Job Batching system
     */
    @Configurable(description = "The maximum number of nodes\nto be requested to the batch system")
    protected int maxNodes = 1;
    /**
     * time out after which one nodes are not expected to register anymore. When this time expires,
     * nodes which register are in reponse of the previous "acquireNode" request are discarded.
     * This time out is also used to time out submit job and delete job command's exit status
     */
    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    protected int nodeTimeOut = 1000 * 60 * 5;//5mn
    /**
     * name of the server on which the job batching software is running.
     * will be contacted using ssh
     */
    @Configurable(description = "The batch system\nhead node address")
    protected String serverName;
    /**
     * URL of the resource manager the newly created nodes will attempt to contact
     */
    @Configurable(description = "Resource Manager's url")
    protected String rmUrl = PAActiveObject.getActiveObjectNodeUrl(PAActiveObject.getStubOnThis()).replace(
            PAResourceManagerProperties.RM_NODE_NAME.getValueAsString(), "");
    /**
     * Path to the credentials file user for RM authentication
     */
    @Configurable(credential = true, description = "Absolute path of the rm.cred file")
    protected File rmCredentialsPath;
    /**
     * options for the submit job command executed on {@link #serverName}
     */
    @Configurable(description = "Options used by the\njob submission command")
    protected String submitJobOpt;
    /**
     * Shutdown flag
     */
    protected boolean shutdown = false;
    /**
     * Class' logger
     */
    protected static org.apache.log4j.Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);

    /**
     * Credentials used by remote nodes to register to the NS
     */
    private Credentials credentials = null;

    /**
     * nodes currently up and running, nodeName -> jobID
     */
    private Hashtable<String, String> currentNodes = new Hashtable<String, String>();
    /**
     * expected nodes. safe check...
     */
    private Set<String> expectedNodes = Collections.synchronizedSet(new HashSet<String>());
    /**
     * the number of pending nodes
     */
    private volatile Integer pendingNodes = 0;
    /**
     * nodes that spontaneously registered to the IM
     */
    private Set<String> registeredNodes = Collections.synchronizedSet(new HashSet<String>());

    /**
     * Acquires as much nodes as possible, making one distinct reservation per node
     */
    @Override
    public void acquireAllNodes() {
        synchronized (currentNodes) {
            //pendingNodes and currentNodes updated in acquireNode
            for (; (currentNodes.size() + pendingNodes) < maxNodes;) {
                acquireNode();
            }
        }
    }

    /**
     * Acquires a single node through pbs
     */
    @Override
    public void acquireNode() {
        final String bjs = getBatchinJobSystemName();
        synchronized (currentNodes) {
            int currentNodesSize = currentNodes.size();
            if ((currentNodesSize + pendingNodes) >= maxNodes) {
                logger.warn("Attempting to acquire nodes while maximum reached");
                return;
            } else {
                pendingNodes++;
            }
            logger.debug("Acquiring a new " + bjs + " node. # of current nodes: " + currentNodesSize +
                " - # of pending nodes: " + pendingNodes);
        }

        // new thread: call will block until registration of the node to the RM
        nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    //currentNodes & pendingNodes are updated in startNode
                    startNode();
                    logger.debug("new " + bjs + " Node acquired. # of current nodes: " + currentNodes.size() +
                        " - # of pending nodes: " + pendingNodes);
                    return;
                } catch (Exception e) {
                    logger.error("Could not acquire node ", e);
                }
                //deployment failed, one "pendingNodes" (volatile) not expected anymore...
                pendingNodes--;
                logger
                        .debug("# of pending nodes arranged given the last checked exception. # of current nodes: " +
                            currentNodes.size() + " - # of pending nodes: " + pendingNodes);
            }
        });
    }

    /**
     * Builds the command line to execute on the PBS frontend and wait for every launched nodes
     * to register. If the node doesn't register (ie. runs {@link #registerAcquiredNode(Node)} isn't called)
     * before the timeout (configurable) value, an exception is raised.
     * If the qSub command submitted to the PBS frontend fails, the node supposed to be launched is not expected anymore and
     * will be discarded at registration time.
     * @throws RMException
     */
    private void startNode() throws RMException {

        InetAddress host = null;
        try {
            host = InetAddress.getByName(this.serverName);
        } catch (UnknownHostException e) {
            throw new RMException(e);
        }
        String deleteCmd = getDeleteJobCommand();
        String submitCmd = getSubmitJobCommand();
        // generate the node name
        // current rmcore shortID should be added to ensure uniqueness
        String nodeName = getBatchinJobSystemName() + "-" + nodeSource.getName() + "-" +
            ProActiveCounter.getUniqID();

        // build the command: echo "script.sh params"|qsub params
        String cmd = "echo \\\"";
        cmd += schedulingPath + getPAAgentRMNodeStarterScriptPath() + " ";
        cmd += this.javaPath + " ";
        try {
            cmd += new String(this.credentials.getBase64()) + " ";
        } catch (KeyException e1) {
            throw new RMException("Could not get base64 credentials", e1);
        }
        cmd += this.rmUrl + " ";
        cmd += nodeName + " ";
        cmd += this.nodeSource.getName() + " ";
        cmd += this.javaOptions + " ";
        cmd += "\\\"";
        cmd += "| " + submitCmd + " " + this.submitJobOpt;

        //add an expected node. every unexpected node will be discarded
        expectedNodes.add(nodeName);

        // executing the command
        Process p;
        try {
            p = Utils.runSSHCommand(host, cmd, this.sshOptions);
        } catch (IOException e1) {
            throw new RMException("Cannot execute ssh command: " + cmd + " on host: " + this.serverName, e1);
        }

        // recover the Job ID through stdout
        String id = "";
        InputStream in = p.getInputStream();
        int b = -1;
        try {
            while ((b = in.read()) > -1) {
                if (b == '\n') {
                    break;
                }
                id += (char) b;
            }
        } catch (IOException e) {
        }

        // check for registration
        final long timeout = nodeTimeOut;
        long t1 = System.currentTimeMillis();
        boolean isJobIDValid = false;//Hack. SSHClient fails but qSub succeeds. Tries to wait for this node registration...
        int circuitBreakerThreshold = 5;
        jobMonitoring: {
            while (true) {
                try {
                    int exitCode = p.exitValue();
                    if (exitCode != 0 && !isJobIDValid) {
                        logger
                                .warn("SSH subprocess at " +
                                    host.getHostName() +
                                    " exit code != 0 but IM tries to recover from this error...Current submit command's output: " +
                                    id + " and associated node's name: " + nodeName);
                        String extractedID = this.extractSubmitOutput(id);
                        if (extractedID != null && !extractedID.equals("")) {
                            isJobIDValid = true;
                            if (expectedNodes.contains(nodeName)) {
                                logger
                                        .warn("jobID " + id +
                                            " retrieved from SSH subprocess' output. Waiting for this node to register.");
                            } else {
                                logger.warn("It seems that node " + nodeName +
                                    " is already registered. Everything is OK.");
                            }
                        } else {
                            //if nodeName is not in the expectedNodes list, it has already been registered.
                            if (expectedNodes.remove(nodeName)) {
                                logger.error("Cannot get jobID from " + submitCmd + " output. Node " +
                                    nodeName + " is not expected anymore.");
                                throw new RMException("SSH subprocess at " + host.getHostName() +
                                    " exited abnormally (" + exitCode + ").");
                            } else {
                                logger
                                        .error("Node " +
                                            nodeName +
                                            " seems to be already registered but we don't have any associated valid jobID. We won't be able to submit a valid " +
                                            deleteCmd + " command to remove the node.");
                            }
                        }
                    }
                } catch (IllegalThreadStateException e) {
                    // process has not returned yet
                }

                if (registeredNodes.remove(nodeName)) {
                    break jobMonitoring;
                }

                long t2 = System.currentTimeMillis();
                if (t2 - t1 > timeout || shutdown) {
                    //nodeName not expected anymore. if
                    synchronized (expectedNodes) {
                        if (expectedNodes.remove(nodeName)) {
                            p.destroy();
                            try {
                                deleteJob(id);
                            } catch (RMException e) {
                                logger.warn("node " + nodeName + " timed out and " + deleteCmd +
                                    " command for jobID " + id +
                                    " failed. Cannot ensure that the associated " + submitCmd +
                                    " command is deleted.", e);
                            }
                            throw new RMException("Node " + nodeName + " timed out.");
                        } else {
                            //the node registered lately?
                            if (registeredNodes.remove(nodeName)) {
                                //ok, we reached a valid state
                                break jobMonitoring;
                            } else {
                                throw new RMException("Invalid state, node " + nodeName +
                                    " seems to be registered but is not found.");
                            }
                        }
                    }
                }
                logger.debug("Waiting for node " + nodeName + " registration... time to timeout: " +
                    (timeout - (System.currentTimeMillis() - t1)));
                try {
                    Thread.sleep(4000);
                } catch (Exception e) {
                    circuitBreakerThreshold--;
                    if (circuitBreakerThreshold <= 0) {
                        logger.error("While monitoring ssh subprocess.", e);
                        throw new RMException("Exception occurred while monitoring ssh subprocess.", e);
                    }
                }

            }
        }
        addNodeAndDecrementPendingNode(nodeName, this.extractSubmitOutput(id));
    }

    @Override
    public BooleanWrapper configure(Object... parameters) {
        if (parameters != null && parameters.length >= 10) {
            int index = 0;
            this.javaPath = parameters[index++].toString();
            if (this.javaPath == null || this.javaPath.equals("")) {
                this.javaPath = "java";
            }
            this.sshOptions = parameters[index++].toString();
            this.schedulingPath = parameters[index++].toString();
            this.javaOptions = parameters[index++].toString();
            try {
                this.maxNodes = Integer.parseInt(parameters[index++].toString());
            } catch (Exception e) {
                this.maxNodes = 1;
            }
            try {
                this.nodeTimeOut = Integer.parseInt(parameters[index++].toString());
            } catch (Exception e) {
                this.nodeTimeOut = 1000 * 60 * 5;
            }
            this.serverName = parameters[index++].toString();
            this.rmUrl = parameters[index++].toString();
            if (parameters[index] == null) {
                throw new IllegalArgumentException("Credentials must be specified");
            }
            try {
                this.credentials = Credentials.getCredentialsBase64((byte[]) parameters[index++]);
            } catch (KeyException e) {
                throw new IllegalArgumentException("Could not retrieve base64 credentials", e);
            }
            if (parameters[index] != null) {
                this.submitJobOpt = parameters[index++].toString().replaceAll("\"", "\\\"");
            }
        } else {
            throw new IllegalArgumentException("Invalid parameters for IM creation");
        }

        return new BooleanWrapper(true);
    }

    @Override
    public void registerAcquiredNode(Node node) throws RMException {
        String nodeName = node.getNodeInformation().getName();
        synchronized (expectedNodes) {
            if (expectedNodes.remove(nodeName)) {
                registeredNodes.add(nodeName);
                logger.debug("New expected node registered: " + nodeName);
            } else {
                logger.debug("Non expected node not registered: " + nodeName);
                throw new RMException("Node " + nodeName + " not expected. Rejecting it.");
            }
        }
    }

    @Override
    public void removeNode(Node node) throws RMException {
        String deleteCmd = getDeleteJobCommand();
        String jobID = null;
        String nodeName = node.getNodeInformation().getName();
        if ((jobID = currentNodes.get(nodeName)) != null) {
            try {
                deleteJob(jobID);
            } catch (RMException e) {
                logger.warn(deleteCmd + " command failed, cannot ensure job " + jobID +
                    " is deleted. Anyway, node " + nodeName + " is removed from the infrastructure manager.",
                        e);
            }
            //atomic remove is important, furthermore we ensure consistent trace
            synchronized (currentNodes) {
                currentNodes.remove(nodeName);
                logger.debug("Node " + nodeName + " removed. # of current nodes: " + currentNodes.size() +
                    " # of pending nodes: " + pendingNodes);
            }
        } else {
            logger.error("Node " + nodeName + " is not known as a Node belonging to this " +
                PBSInfrastructure.class.getSimpleName());
        }
    }

    /**
     * Runs a {@link #getDeleteJobCommand()} command on the remote host for the given jobID and monitors the exit.
     * @param jobID the jobID string to delete
     * @throws RMException if the {@link #getDeleteJobCommand()} command failed
     */
    private void deleteJob(String jobID) throws RMException {
        String deleteCmd = getDeleteJobCommand();
        String cmd = deleteCmd + " " + jobID;
        Process del = null;
        try {
            del = Utils.runSSHCommand(InetAddress.getByName(this.serverName), cmd, this.sshOptions);
        } catch (Exception e1) {
            logger.warn("Cannot ssh " + this.serverName + " to issue " + deleteCmd +
                " command. job with jobID: " + jobID + " won't be deleted.", e1);
            throw new RMException("Cannot ssh " + this.serverName + " to issue " + deleteCmd +
                " command. job with jobID: " + jobID + " won't be deleted.", e1);
        }
        long timeStamp = System.currentTimeMillis();
        while (true) {
            try {
                int exitCode = del.exitValue();
                if (exitCode != 0) {
                    logger.error("Cannot delete job " + jobID + ". " + deleteCmd +
                        " command returned != 0 -> " + exitCode);
                    throw new RMException("Cannot delete job " + jobID + ". " + deleteCmd +
                        " command returned != 0 -> " + exitCode);
                } else {
                    logger.debug("Job " + jobID + " deleted.");
                    return;
                }
            } catch (IllegalThreadStateException e) {
                //the thread hasn't exited yet... don't eat exception, trace it...
                logger.trace("waiting for " + deleteCmd + " exit code.", e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //the thread was interrupted... don't eat exception, trace it...
                logger.trace("sleep interrupted while waiting for " + deleteCmd + " to exit.", e);
            }
            if ((System.currentTimeMillis() - timeStamp) >= nodeTimeOut) {
                logger.error("Cannot delete job " + jobID + ". " + deleteCmd + " command timed out.");
                throw new RMException("Cannot delete job " + jobID + ". " + deleteCmd + " command timed out.");
            }
        }
    }

    /**
     * @return short description of the IM
     */
    public String getDescription() {
        return "Acquires nodes from a " + getBatchinJobSystemName() + " resource manager.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getBatchinJobSystemName() + " Infrastructure";
    }

    @Override
    public void shutDown() {
        shutdown = true;
    }

    /**
     * Adds the given node's name and its associated jobID to the
     * {@link #currentNodes} hashtable and decrements the number of pending nodes.
     * @param nodeName
     * @param id
     */
    private void addNodeAndDecrementPendingNode(String nodeName, String id) {
        synchronized (currentNodes) {
            currentNodes.put(nodeName, id);
            pendingNodes--;
        }
    }

    /*##########################################
     * SPI Methods
     ##########################################*/
    /**
     * To be able to get from implementations the command that will be used to
     * submit a new Job
     * @return submit job command on the target Batching Job System.
     */
    protected abstract String getSubmitJobCommand();

    /**
     * To be able to get from implementations the command that will be used to
     * delete a job
     * @return delete job command on the target Batching Job System.
     */
    protected abstract String getDeleteJobCommand();

    /**
     * Return a string to identify the type of the target Batching Job System.
     * Return's content is not really significant, it is only used to build
     * nodes name and for logging...
     * @return target Batching Job System's name
     */
    protected abstract String getBatchinJobSystemName();

    /**
     * Parses the submit ({@link #getSubmitJobCommand()}) command output to extract job's ID.
     * @param output the submit command output
     * @return the job's ID in case of success, empty string or null if the method is unable to compute the job's ID.
     */
    protected abstract String extractSubmitOutput(String output);

    /**
     * Returns the path of the script used to start {@link PAAgentServiceRMStarter} on the
     * Batching Job System nodes. Implementation must override this methods if they provide a
     * new script.
     *
     * @return "/scripts/unix/cluster/jobBatchingInfrastructure.sh" in the current implementation
     */
    protected String getPAAgentRMNodeStarterScriptPath() {
        return "/scripts/unix/cluster/jobBatchingInfrastructure.sh";
    }

}
