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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyException;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.ssh.SSHClient;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;


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

    /** The frequency in ms for the refresh of the node acquisition */
    private static final int NODE_ACQUISITION_CHECK_RATE = 1000;
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
    @Configurable(description = "Options for the ssh command used\nto log in the batch system head node")
    protected String sshOptions;
    /**
     * Path to the Scheduling installation on the remote hosts
     */
    @Configurable(description = "Absolute path of the Resource Manager (or Scheduler)\nroot directory on the remote hosts")
    protected String schedulingPath = PAResourceManagerProperties.RM_HOME.getValueAsString();
    /**
     * Additional java options to append to the command executed on the remote host
     */
    @Configurable(description = "Options for the java command\nlaunching the node on the remote hosts")
    protected String javaOptions;
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
    @Configurable(description = "The batch system\nhead node name or IP adress")
    protected String serverName;
    /**
     * Path to the credentials file user for RM authentication
     */
    @Configurable(credential = true, description = "Absolute path of the credential file")
    protected File rmCredentialsPath;
    /**
     * options for the submit job command executed on {@link #serverName}
     */
    @Configurable(description = "Options for the\njob submission command")
    protected String submitJobOpt;
    /**
     * Shutdown flag
     */
    protected boolean shutdown = false;
    /**
     * Credentials used by remote nodes to register to the NS
     */
    private Credentials credentials = null;

    /**
     * nodes currently up and running, nodeName -> jobID
     */
    private Hashtable<String, String> currentNodes = new Hashtable<String, String>();
    /**
     * the number of pending nodes
     */
    private volatile Integer deployingNodes = 0;
    /**
     * To notify the control loop of the pending node timeout
     */
    private ConcurrentHashMap<String, Boolean> pnTimeout = new ConcurrentHashMap<String, Boolean>();

    /**
     * Acquires as much nodes as possible, making one distinct reservation per node
     */
    @Override
    public void acquireAllNodes() {
        synchronized (currentNodes) {
            //deployingNodes and currentNodes updated in acquireNode
            for (; (currentNodes.size() + deployingNodes) < maxNodes;) {
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
            if ((currentNodesSize + deployingNodes) >= maxNodes) {
                logger.warn("Attempting to acquire nodes while maximum reached");
                return;
            } else {
                deployingNodes++;
            }
            logger.debug("Acquiring a new " + bjs + " node. # of current nodes: " + currentNodesSize +
                " - # of deploying nodes: " + deployingNodes);
        }

        // new thread: call will block until registration of the node to the RM
        nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    //currentNodes & deployingNodes are updated in startNode
                    startNode();
                    logger.debug("new " + bjs + " Node acquired. # of current nodes: " + currentNodes.size() +
                        " - # of deploying nodes: " + deployingNodes);
                    return;
                } catch (Exception e) {
                    logger.error("Could not acquire node ", e);
                }
                //deployment failed, one "deployingNodes" (volatile) not expected anymore...
                deployingNodes--;
                logger
                        .debug("# of deploying nodes arranged given the last checked exception. # of current nodes: " +
                            currentNodes.size() + " - # of deploying nodes: " + deployingNodes);
            }
        });
    }

    /**
     * Builds the command line to execute on the PBS frontend and wait for every launched nodes
     * to register. If the node doesn't register (ie. runs {@link #internalRegisterAcquiredNode(Node)} isn't called)
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

        //add an deploying node.
        final String dnURL = super.addDeployingNode(nodeName, cmd, "Deploying node on " +
            getBatchinJobSystemName() + " scheduler", this.nodeTimeOut);
        this.pnTimeout.put(dnURL, new Boolean(false));

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
        // at this point, the ssh process should have already exited because it
        // only handle the job submission, not the execution... furthermore
        // the "id" is defined
        String lf = System.getProperty("line.separator");
        final long timeout = nodeTimeOut;
        long t1 = System.currentTimeMillis();
        boolean isJobIDValid = false;//Hack. SSHClient fails but qSub succeeds. Tries to wait for this node registration...
        int circuitBreakerThreshold = 5;
        while (!this.pnTimeout.get(dnURL) && circuitBreakerThreshold > 0) {
            try {
                int exitCode = p.exitValue();
                if (exitCode != 0 && !isJobIDValid) {
                    logger
                            .warn("SSH subprocess at " +
                                host.getHostName() +
                                " exit code != 0 but IM tries to recover from this error...Current submit command's output: " +
                                id + " and associated node's name: " + nodeName);
                    String extractedID = this.extractSubmitOutput(id);
                    String errput = this.extractProcessErrput(p);
                    final String description = "SSH command failed to launch node on " +
                        getBatchinJobSystemName() + " scheduler" + lf + "   >Error code: " + exitCode + lf +
                        "   >Errput: " + errput + "   >Output: " + id;
                    // the job id may be valid, trying to wait for the node registration...
                    if (extractedID != null && !extractedID.equals("")) {
                        isJobIDValid = true;
                    }
                    //defines how to recover from this state
                    //throws a RMException if we can't
                    handleWrongJobTermination(isJobIDValid, nodeName, dnURL, host, id, description, exitCode,
                            submitCmd, deleteCmd);
                }
            } catch (IllegalThreadStateException e) {
                // process has not returned yet
            }

            if (super.checkNodeIsAcquiredAndDo(nodeName, null, null)) {
                //registration is ok
                p.destroy();
                addNodeAndDecrementDeployingNode(nodeName, this.extractSubmitOutput(id));
                return;
            }

            try {

                logger.debug("Waiting for node " + nodeName + " registration... time to timeout: " +
                    (timeout - (System.currentTimeMillis() - t1)));
                Thread.sleep(BatchJobInfrastructure.NODE_ACQUISITION_CHECK_RATE);
            } catch (Exception e) {
                circuitBreakerThreshold--;
                logger.error("While monitoring ssh subprocess.", e);
            }
        }//end of while loop, either deploying node timeout/removed of threshold reached

        // the node is not excpected anymore
        if (pnTimeout.get(dnURL)) {
            //we remove it
            this.pnTimeout.remove(dnURL);
            //we destroy the process
            p.destroy();
            throw new RMException("Deploying Node " + nodeName + " not expected any more");
        }

        if (circuitBreakerThreshold <= 0) {
            logger.error("Circuit breaker threshold reached while monitoring ssh subprocess.");
            throw new RMException("Several exceptions occurred while monitoring ssh subprocess.");
        }

        //if we are here we reached an invalid state
        throw new RMException(
            "Invalid state, exit from a control loop with threshold > 0 and expected deploying node");
    }

    /**
     * Used to determine if we can recover from an exit value != 0 of the submit job command
     * @param isJobIDValid If the implementation has been able to retrieve a job id from the command output
     * @param nodeName The node's name
     * @param dnURL The deploying node's url
     * @param host The host on which one the scheduler (batch job) is running
     * @param id The job id
     * @param description The description of the state ( what is wrong )
     * @param exitCode The process' exit code
     * @param submitCmd The command used to submit the job
     * @param deleteCmd The command used to delete the job
     * @throws RMException If we cannot recover from this state and the deployed node is not expected anymore
     */
    private void handleWrongJobTermination(final boolean isJobIDValid, final String nodeName,
            final String dnURL, final InetAddress host, final String id, final String description,
            final int exitCode, final String submitCmd, final String deleteCmd) throws RMException {
        if (super.checkNodeIsAcquiredAndDo(nodeName, null,
        //executed if the node has not registered
                new Runnable() {
                    public void run() {
                        //if the job id is not valid and the node hasn't registered yet
                        //we discard the node acquisition
                        if (!isJobIDValid) {
                            BatchJobInfrastructure.this.declareDeployingNodeLost(dnURL, description);
                        }
                    }
                })) {
            //ok the node is registered...
            if (isJobIDValid) {
                //the job id is ok, just log something
                logger.warn("It seems that node " + nodeName + " is already registered. Everything is OK.");
            } else {
                //the node is registered but we haven't got any job id to kill it
                logger
                        .error("Node " +
                            nodeName +
                            " seems to be already registered but we don't have any associated valid jobID. We won't be able to submit a valid " +
                            deleteCmd + " command to remove the node.");

            }
        } else {
            //ko, the node didn't register
            if (isJobIDValid) {
                //if the job id is valid, we still wait for it
                logger.warn("jobID " + id +
                    " retrieved from SSH subprocess' output. Waiting for this node to register.");
            } else {
                //otherwise, the node acquisition has been discarded, the node has been declared as lost
                //in checkNodeIsAcquiredAndDo callback
                logger.error("Cannot get jobID from " + submitCmd + " output. Node " + nodeName +
                    " is not expected anymore.");
                throw new RMException("SSH subprocess at " + host.getHostName() + " exited abnormally (" +
                    exitCode + ").");
            }
        }
    }

    /**
     * Configures this infrastructure manager
     * parameters[0] = java path
     * parameters[1] = ssh options
     * parameters[2] = scheduling path
     * parameters[3] = java options
     * parameters[4] = max node
     * parameters[5] = node timeout
     * parameters[6] = scheduler server name
     * parameters[7] = PA scheduler credentials
     * parameters[8] = submit job options
     */
    @Override
    public void configure(Object... parameters) {
        if (parameters != null && parameters.length >= 9) {
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

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAcquiredNode(Node node) throws RMException {
        String nodeName = node.getNodeInformation().getName();
        logger.debug("New expected node registered: " + nodeName);
    }

    /**
     * This method is called by Infrastructure Manager in case of a pending node removal.
     * We take advantage of it to specify to the remote process control loop of the removal.
     * This one will then exit.
     */
    @Override
    protected void notifyDeployingNodeLost(String pnURL) {
        this.pnTimeout.put(pnURL, true);
    }

    /**
     * {@inheritDoc}
     */
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
                    " # of deploying nodes: " + deployingNodes);
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
     * {@link #currentNodes} hashtable and decrements the number of deploying nodes.
     * @param nodeName
     * @param id
     */
    private void addNodeAndDecrementDeployingNode(String nodeName, String id) {
        synchronized (currentNodes) {
            currentNodes.put(nodeName, id);
            deployingNodes--;
        }
    }

    /**
     * To extract the SSH process' errput
     * @param p The ssh process
     * @return a string which is the process errput
     */
    private String extractProcessErrput(Process p) {
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            String lf = System.getProperty("line.separator");
            while (br.ready()) {
                if ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(lf);
                }
            }
        } catch (IOException e) {
            sb.append("Cannot extract process errput");
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.debug("Cannot close process error stream", e);
            }
        }
        return sb.toString();
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
     * Returns the path of the script used to start {@link RMNodeStarter} on the
     * Batching Job System nodes. Implementation must override this methods if they provide a
     * new script.
     *
     * @return "/scripts/unix/cluster/jobBatchingInfrastructure.sh" in the current implementation
     */
    protected String getPAAgentRMNodeStarterScriptPath() {
        return "/scripts/unix/cluster/jobBatchingInfrastructure.sh";
    }

}
