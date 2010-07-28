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
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Collections;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.ssh.SSHClient;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * 
 * Acquires nodes provided by an existing PBS cluster
 * <p>
 * The point of this Infrastructure is to interface with
 * an existing installation of a PBS (ie Torque) Scheduler:
 * node acquisition will be achieved by running
 * runtimes as Torque jobs: submitting a job acquires a node,
 * killing it stops it.
 * <p>
 * PBS jobs will be submitted through SSH from the RM to the PBS server;
 * make sure the RM and the nodes will be able to communicate once
 * the node is up.
 * <p>
 * If you need more control over you deployment, you may consider
 * using {@link GCMInfrastructure} instead, which contains the 
 * functionalities of this Infrastructure, but requires more configuration.
 * 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 * 
 */
public class PBSInfrastructure extends InfrastructureManager {

    /**  */
	private static final long serialVersionUID = 21L;
    /**
     * maximum number of nodes this infrastructure can ask simultaneously to the pbs scheduler
     */
    @Configurable
    protected int maxNodes = 1;
    /**
     * time out after which one pbs commands won't be expected to return anymore
     */
    @Configurable(description = "in ms")
    protected int pbsCmdTimeOut = 1000 * 60 * 5;//5mn
    /**
     * address of the server on which the pbs scheduler is running
     * will be contacted using ssh
     */
    @Configurable
    protected String PBSServer;
    /**
     * URL of the resource manager the newly created nodes will attempt to contact
     */
    @Configurable
    protected String RMUrl;
    /**
     * Path to the credentials file user for RM authentication
     */
    @Configurable(credential = true)
    protected File RMCredentialsPath;
    /**
     * options for the qsub command executed on {@link #PBSServer}
     * default value is acceptable, see 
     * {@link http://www.clusterresources.com/torquedocs21/commands/qsub.shtml} for more.
     */
    @Configurable
    protected String qsubOptions = " -l \"nodes=1:ppn=1\"";
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
        synchronized (currentNodes) {
            int currentNodesSize = currentNodes.size();
            if ((currentNodesSize + pendingNodes) >= maxNodes) {
                logger.warn("Attempting to acquire nodes while maximum reached");
                return;
            } else {
                pendingNodes++;
            }
            logger.debug("Acquiring a new PBS node. # of current nodes: " + currentNodesSize +
                " - # of pending nodes: " + pendingNodes);
        }

        // new thread: call will block until registration of the node to the RM
        nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    //currentNodes & pendingNodes are updated in startNode
                    startNode();
                    logger.debug("new PBS Node acquired. # of current nodes: " + currentNodes.size() +
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
     * Execute a specific command on a remote host through SSH
     *
     * @param host the remote host on which to execute the command
     * @param cmd the command to execute on the remote host
     * @return the Process in which the SSH command is running;
     *          NOT the actual process on the remote host, although it can be used
     *          to read the remote process' output.
     * @throws RMException SSH command execution failed
     */
    protected Process runSSHCommand(InetAddress host, String cmd) throws RMException {
        // build the SSH command using ProActive's SSH client:
        // will recover keys/identities if they exist
        String sshCmd = System.getProperty("java.home") + "/bin/java";
        sshCmd += " -cp " + PAResourceManagerProperties.RM_HOME.getValueAsString() +
            "/dist/lib/ProActive.jar";
        sshCmd += " -Dproactive.useIPaddress=true ";
        sshCmd += SSHClient.class.getName();
        sshCmd += " " + sshOptions;
        sshCmd += " " + host.getHostName();
        sshCmd += " \"" + cmd + "\"";

        try {
            if (host.equals(InetAddress.getLocalHost()) || host.equals(InetAddress.getByName("127.0.0.1"))) {
                logger.debug("The command will be executed locally");
                sshCmd = cmd.replaceAll("\\\\", "");
            }
        } catch (UnknownHostException e) {
        }

        logger.info("Executing SSH command: '" + sshCmd + "'");

        Process p = null;
        // start the SSH command in a new process and not a thread:
        // easier killing, prevents the client from polluting stdout
        try {
            String[] cmdArray = null;
            switch (OperatingSystem.getOperatingSystem()) {
                case unix:
                    cmdArray = new String[] { CentralPAPropertyRepository.PA_GCMD_UNIX_SHELL.getValue(),
                            "-c", sshCmd };
                    break;
                case windows:
                    sshCmd.replaceAll("/", "\\\\");
                    cmdArray = new String[] { sshCmd };
                    break;
            }

            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            p = pb.start();

        } catch (Exception e1) {
            throw new RMException("Failed to execute SSH command: " + sshCmd, e1);
        }
        return p;
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
            host = InetAddress.getByName(this.PBSServer);
        } catch (UnknownHostException e) {
            throw new RMException(e);
        }

        // generate the node name
        // current rmcore shortID should be added to ensure uniqueness
        String nodeName = "PBS-" + nodeSource.getName() + "-" + ProActiveCounter.getUniqID();

        // build the command: echo "script.sh params"|qsub params 
        String cmd = "echo \\\"";
        cmd += schedulingPath + "/scripts/unix/cluster/pbsInfrastructure.sh ";
        cmd += this.javaPath + " ";
        try {
            cmd += new String(this.credentials.getBase64()) + " ";
        } catch (KeyException e1) {
            throw new RMException("Could not get base64 credentials", e1);
        }
        cmd += this.RMUrl + " ";
        cmd += nodeName + " ";
        cmd += this.nodeSource.getName() + " ";
        cmd += this.javaOptions + " ";
        cmd += "\\\"";
        cmd += "| qsub " + this.qsubOptions;

        //add an expected node. every unexpected node will be discarded
        expectedNodes.add(nodeName);

        // executing the command
        Process p = runSSHCommand(host, cmd);

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
        final long timeout = pbsCmdTimeOut;
        long t1 = System.currentTimeMillis();
        boolean isJobIDValid = false;//Hack. SSHClient fails but qSub succeeds. Tries to wait for this node registration...
        int circuitBreakerThreshold = 5;
        jobMonitoring: {
            while (true) {
                try {
                    int exitCode = p.exitValue();
                    if (exitCode != 0 && !isJobIDValid) {
                        logger.warn("SSH subprocess at " + host.getHostName() +
                            " exit code != 0 but IM tries to recover from this error...Current jobID: " + id +
                            " and associated node's name: " + nodeName);
                        if (id.matches("\\d+[.]*.*")) {
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
                                logger.error("Cannot get jobID from qSub output. Node " + nodeName +
                                    " is not expected anymore.");
                                throw new RMException("SSH subprocess at " + host.getHostName() +
                                    " exited abnormally (" + exitCode + ").");
                            } else {
                                logger
                                        .error("Node " +
                                            nodeName +
                                            " seems to be already registered but we don't have any associated valid jobID. We won't be able to submit a valid qDel command to remove the node.");
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
                                qDel(id);
                            } catch (RMException e) {
                                logger.warn("node " + nodeName + " timed out and qDel command for jobID " +
                                    id +
                                    " failed. Cannot ensure that the associated qSub command is deleted.", e);
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
        addNodeAndDecrementPendingNode(nodeName, id);
    }

    @Override
    public BooleanWrapper configure(Object... parameters) {
        if (parameters != null && parameters.length >= 10) {
            int index = 0;
            this.javaPath = parameters[index++].toString();
            if (!new File(this.javaPath).isAbsolute()) {
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
                this.pbsCmdTimeOut = Integer.parseInt(parameters[index++].toString());
            } catch (Exception e) {
                this.pbsCmdTimeOut = 1000 * 60 * 5;
            }
            this.PBSServer = parameters[index++].toString();
            this.RMUrl = parameters[index++].toString();
            if (parameters[index] == null) {
                throw new IllegalArgumentException("Credentials must be specified");
            }
            try {
                this.credentials = Credentials.getCredentialsBase64((byte[]) parameters[index++]);
            } catch (KeyException e) {
                throw new IllegalArgumentException("Could not retrieve base64 credentials", e);
            }
            if (parameters[index] != null) {
                this.qsubOptions = parameters[index++].toString().replaceAll("\"", "\\\"");
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
        String jobID = null;
        String nodeName = node.getNodeInformation().getName();
        if ((jobID = currentNodes.get(nodeName)) != null) {
            try {
                qDel(jobID);
            } catch (RMException e) {
                logger.warn("qDel command failed, cannot ensure job " + jobID + " is deleted. Anyway, node " +
                    nodeName + " is removed from the infrastructure manager.", e);
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
     * Runs a qDel command on the remote host for the given jobID and monitors the exit
     * This method doesn't throw any exception if qDel exit code equals 168 ( wrong state ), 153 ( invalid jobID ).
     * @param jobID the jobID string to delete
     * @throws RMException if the qDel command failed
     */
    private void qDel(String jobID) throws RMException {
        String cmd = "qdel " + jobID;
        Process qDel = null;
        try {
            qDel = runSSHCommand(InetAddress.getByName(this.PBSServer), cmd);
        } catch (UnknownHostException e1) {
            logger.warn("Cannot ssh " + this.PBSServer + " to issue qDel command. job with jobID: " + jobID +
                " won't be deleted.");
            throw new RMException("Cannot ssh " + this.PBSServer +
                " to issue qDel command. job with jobID: " + jobID + " won't be deleted.", e1);
        }
        long timeStamp = System.currentTimeMillis();
        while (true) {
            try {
                int exitCode = qDel.exitValue();
                if (exitCode != 0 &&
                /* invalid job state, we consider that the node has already exited */
                exitCode != 168 &&
                /* invalid job id, can occurs if Torque flushes completed jobs at a too fast pace */
                exitCode != 153) {
                    logger
                            .error("Cannot delete job " + jobID + ". qDel command returned != 0 -> " +
                                exitCode);
                    throw new RMException("Cannot delete job " + jobID + ". qDel command returned != 0 -> " +
                        exitCode);
                } else {
                    logger.debug("Job " + jobID + " deleted.");
                    return;
                }
            } catch (IllegalThreadStateException e) {
                //the thread hasn't exited yet... don't eat exception, trace it...
                logger.trace("waiting for qDel exit code.", e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //the thread was interrupted... don't eat exception, trace it...
                logger.trace("sleep interrupted while waiting for qDel exit.", e);
            }
            if ((System.currentTimeMillis() - timeStamp) >= pbsCmdTimeOut) {
                logger.error("Cannot delete job " + jobID + ". qDel command timed out.");
                throw new RMException("Cannot delete job " + jobID + ". qDel command timed out.");
            }
        }
    }

    /**
     * @return short description of the IM
     */
    public String getDescription() {
        return "Acquires nodes from a PBS Resouce Manager";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PBS Infrastructure";
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
}
