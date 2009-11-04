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
 *  Initial developer(s): ActiveEon Team - www.activeeon.com
 *
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;


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
public class PBSInfrastructure extends AbstractSSHInfrastructure {

    /**
     * maximum number of nodes this infrastructure can ask simultaneously to the pbs scheduler
     */
    @Configurable
    protected int maxNodes = 1;
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
     * dummy class to make up for the lack of a builtin Pair<A,B> template
     */
    private static class NodeInfo {
        /**
         * request id: returned by qsub upon job submission,
         * used as parameter by qdel to kill the job
         */
        public String id;
        /**
         * name of a node as in protocol://host:port/name
         */
        public String name;

        public NodeInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    /**
     * Credentials used by remote nodes to register to the NS
     */
    private Credentials credentials = null;

    /**
     * nodes currently up and running
     */
    private List<NodeInfo> currentNodes = new ArrayList<NodeInfo>();
    /**
     * nodes that spontaneously registered to the IM
     */
    private List<String> registeredNodes = new ArrayList<String>();

    /**
     * Acquires as much nodes as possible, making one distinct reservation per node
     */
    @Override
    public void acquireAllNodes() {
        int cur = currentNodes.size();
        for (int i = 0; i < (maxNodes - cur); i++) {
            acquireNode();
        }
    }

    /**
     * Acquires a single node through pbs
     */
    @Override
    public void acquireNode() {
        int cur = currentNodes.size();
        if (cur >= maxNodes) {
            logger.warn("Attempting to acquire nodes while maximum reached");
            return;
        }

        // new thread: call will block until registration of the node to the RM
        nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    currentNodes.add(startNode());
                } catch (Exception e) {
                    logger.error("Could not acquire node ", e);
                    return;
                }
            }
        });
    }

    private NodeInfo startNode() throws RMException {

        InetAddress host = null;
        try {
            host = InetAddress.getByName(this.PBSServer);
        } catch (UnknownHostException e) {
            throw new RMException(e);
        }

        // generate the node name
        Random rand = new Random();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String nn = "";
        for (int i = 0; i < 16; i++) {
            nn += alpha.charAt(rand.nextInt(alpha.length()));
        }
        String nodeName = "PBS-" + nodeSource.getName() + "-" + nn;

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
        cmd += " -Dproactive.communication.protocol=" + this.protocol + " ";
        if (this.protocol.equals(Constants.RMI_PROTOCOL_IDENTIFIER) ||
            this.protocol.equals(Constants.RMISSH_PROTOCOL_IDENTIFIER)) {
            cmd += PAProperties.PA_RMI_PORT.getCmdLine() + this.port + " ";
        } else if (this.protocol.equals(Constants.XMLHTTP_PROTOCOL_IDENTIFIER) ||
            this.protocol.equals(Constants.HTTPSSH_PROTOCOL_IDENTIFIER)) {
            cmd += PAProperties.PA_RMI_PORT.getCmdLine() + this.port + " ";
        }
        cmd += " -Dproactive.useIPaddress=true ";
        cmd += this.javaOptions + " ";
        cmd += "\\\"";
        cmd += "| qsub " + this.qsubOptions;

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
        final long timeout = 1000 * 60 * 5; // 5mn
        long t1 = System.currentTimeMillis();
        while (true) {
            try {
                int exitCode = p.exitValue();
                if (exitCode != 0) {
                    p.destroy();
                    throw new RMException("SSH subprocess at " + host.getHostName() + " exited abnormally (" +
                        exitCode + ").");
                }
            } catch (IllegalThreadStateException e) {
                // process has not returned yet
            }

            long t2 = System.currentTimeMillis();
            if (t2 - t1 > timeout || shutdown) {
                p.destroy();
                throw new RMException("Request timed out");
            }
            boolean done = false;
            synchronized (registeredNodes) {
                for (String reg : registeredNodes) {
                    if (nodeName.equals(reg)) {
                        registeredNodes.remove(nodeName);
                        done = true;
                        break;
                    }
                }
            }
            if (done) {
                break;
            }
            try {
                Thread.sleep(4000);
            } catch (Exception e) {
                break;
            }

        }
        NodeInfo i = new NodeInfo(id, nodeName);

        return i;
    }

    @Override
    public void configure(Object... parameters) throws RMException {
        super.configure(parameters);

        if (parameters != null && parameters.length >= 10) {
            try {
                this.maxNodes = Integer.parseInt(parameters[5].toString());
            } catch (Exception e) {
                this.maxNodes = 1;
            }
            this.PBSServer = parameters[6].toString();
            this.RMUrl = parameters[7].toString();
            if (parameters[8] == null) {
                throw new RMException("Credentials must be specified");
            }
            try {
                this.credentials = Credentials.getCredentialsBase64((byte[]) parameters[8]);
            } catch (KeyException e) {
                throw new RMException("Could not retrieve base64 credentials", e);
            }
            this.qsubOptions = parameters[9].toString().replaceAll("\"", "\\\"");
        } else {
            throw new RMException("Invalid parameters for IM creation");
        }
    }

    @Override
    public void registerAcquiredNode(Node node) throws RMException {
        synchronized (registeredNodes) {
            registeredNodes.add(node.getNodeInformation().getName());
        }
    }

    @Override
    public void removeNode(Node node) throws RMException {
        synchronized (currentNodes) {
            for (NodeInfo ni : currentNodes) {
                if (node.getNodeInformation().getName().equals(ni.name)) {
                    String cmd = "qdel " + ni.id;
                    runSSHCommand(node.getVMInformation().getInetAddress(), cmd);
                }
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

}
