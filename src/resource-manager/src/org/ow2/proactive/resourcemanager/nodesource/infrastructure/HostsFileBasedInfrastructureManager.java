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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.utils.FileToBytesConverter;


/** Abstract infrastructure Manager implementation based on hosts list file. */
public abstract class HostsFileBasedInfrastructureManager extends InfrastructureManager {

    public static final int DEFAULT_NODE_TIMEOUT = 60 * 1000;
    public static final int DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD = 5;

    @Configurable(fileBrowser = true, description = "Absolute path of the file containing\nthe list of remote hosts")
    protected File hostsList;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    protected int nodeTimeOut = HostsFileBasedInfrastructureManager.DEFAULT_NODE_TIMEOUT;

    @Configurable(description = "Maximum number of failed attempt to deploy on \na host before discarding it")
    protected int maxDeploymentFailure = HostsFileBasedInfrastructureManager.DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD;

    /**
     * list of free hosts; if host AA has a capacity of 2 runtimes, the initial state
     * of this list will contain twice host AA.
     */
    private List<InetAddress> freeHosts = Collections.synchronizedList(new ArrayList<InetAddress>());
    /** Maintains tresholds per hosts to be able to know if the deployment fails and to retry a given number of time */
    private Hashtable<InetAddress, Integer> hostsThresholds = new Hashtable<InetAddress, Integer>();

    /**
     * The set of nodes for which one the registerAcquiredNode has been run.
     */
    private Hashtable<String, InetAddress> registeredNodes = new Hashtable<String, InetAddress>();

    /**
     * To notify the control loop of the deploying node timeout
     */
    protected ConcurrentHashMap<String, Boolean> pnTimeout = new ConcurrentHashMap<String, Boolean>();

    /**
     * Acquire one node per available host
     */
    @Override
    public void acquireAllNodes() {
        synchronized (freeHosts) {
            while (freeHosts.size() > 0) {
                acquireNode();
            }
        }
    }

    /**
     * Acquire one node on an available host
     */
    @Override
    public void acquireNode() {
        InetAddress tmpHost = null;
        synchronized (freeHosts) {
            if (freeHosts.size() == 0) {
                logger.warn("Attempting to acquire nodes while all hosts are already deployed.");
                return;
            }
            tmpHost = freeHosts.remove(0);
            logger.debug("Acquiring a new node. #freeHosts:" + freeHosts.size() + " #registered: " +
                registeredNodes.size());
        }
        final InetAddress host = tmpHost;
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    startNodeImpl(host);
                    logger.debug("Node acquisition ended. #freeHosts:" + freeHosts.size() + " #registered: " +
                        registeredNodes.size());
                    //node acquisition went well for host so we update the threshold
                    synchronized (freeHosts) {
                        hostsThresholds.put(host, maxDeploymentFailure);
                    }
                } catch (Exception e) {
                    synchronized (freeHosts) {
                        Integer tries = hostsThresholds.get(host);
                        tries--;
                        if (tries > 0) {
                            hostsThresholds.put(host, tries);
                            freeHosts.add(host);
                        } else {
                            logger.debug("Tries threshold reached for host " + host +
                                ". This host is not part of the deployment process anymore.");
                        }
                    }
                    String description = "Could not acquire node on host " + host.toString() +
                        ". NS's state refreshed regarding last checked excpetion: #freeHosts:" +
                        freeHosts.size() + " #registered: " + registeredNodes.size();
                    logger.error(description, e);
                    return;
                }
            }
        });
    }

    /**
     * Configures the infrastructre.
     * 	parameters[0] = hosts list file content
     * 	parameters[1] = timeout of the node deployment
     * 	parameters[2] = max deployment failure
     */
    @Override
    protected void configure(Object... parameters) {
        if (parameters == null || parameters.length < 3) {
            throw new IllegalArgumentException("Not enough parameter provided to the infrastructure.");
        }
        int index = 0;
        try {
            byte[] bytes = (byte[]) parameters[index++];
            this.hostsList = File.createTempFile("hosts", "list");
            FileToBytesConverter.convertByteArrayToFile(bytes, this.hostsList);
            readHosts(this.hostsList);
            this.hostsList.delete();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read hosts file", e);
        }
        try {
            this.nodeTimeOut = Integer.parseInt(parameters[index++].toString());
        } catch (NumberFormatException e) {
            logger
                    .warn("Number format exception occurred at ns configuration, default acq timeout value set: " +
                        HostsFileBasedInfrastructureManager.DEFAULT_NODE_TIMEOUT + "ms");
            this.nodeTimeOut = HostsFileBasedInfrastructureManager.DEFAULT_NODE_TIMEOUT;
        }

        try {
            this.maxDeploymentFailure = Integer.parseInt(parameters[index++].toString());
        } catch (NumberFormatException e) {
            logger.warn("Number format exception occurred at ns configuration, default attemp value set: " +
                HostsFileBasedInfrastructureManager.DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD);
            this.maxDeploymentFailure = HostsFileBasedInfrastructureManager.DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD;
        }
    }

    /**
     * Internal host file parser
     * <p>
     * File format:
     * one host per line, optionally followed by a space and an integer describing the maximum
     * number of runtimes (1 if not specified). Example:
     * <pre>
     * example.com
     * example.org 5
     * example.net 3
     * </pre>
     * @param f the file from which hosts names are to be extracted
     * @throws IOException parsing failed
     */
    protected void readHosts(File f) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(f));
        String line = "";

        while ((line = in.readLine()) != null) {
            if (line == "" || line.trim().length() == 0)
                continue;

            String[] elts = line.split(" ");
            int num = 1;
            if (elts.length > 1) {
                try {
                    num = Integer.parseInt(elts[1]);
                    if (num < 1) {
                        throw new IllegalArgumentException("Cannot launch less than one runtime per host.");
                    }
                } catch (Exception e) {
                    logger.warn("Error while parsing hosts file: " + e.getMessage());
                    num = 1;
                }
            }
            String host = elts[0];
            try {
                InetAddress addr = InetAddress.getByName(host);
                synchronized (this.freeHosts) {
                    for (int i = 0; i < num; i++) {
                        this.freeHosts.add(addr);
                    }
                }
                hostsThresholds.put(addr, maxDeploymentFailure);
            } catch (UnknownHostException ex) {
                throw new RuntimeException("Unknown host: " + host, ex);
            }
        }
    }

    /**
     * This method is called by Infrastructure Manager in case of a deploying node removal.
     * We take advantage of it to specify to the remote process control loop of the removal.
     * This one will then exit.
     */
    @Override
    protected void notifyDeployingNodeLost(String pnURL) {
        this.pnTimeout.put(pnURL, new Boolean(true));
    }

    /**
     * Parent IM notifies about a new node registration
     */
    @Override
    protected void notifyAcquiredNode(Node node) throws RMException {
        String nodeName = node.getNodeInformation().getName();
        this.registeredNodes.put(nodeName, node.getVMInformation().getInetAddress());
        if (logger.isDebugEnabled()) {
            logger.debug("New expected node registered: #freeHosts:" + freeHosts.size() + " #registered: " +
                registeredNodes.size());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(Node node) {
        InetAddress host = null;
        String nodeName = node.getNodeInformation().getName();
        if ((host = registeredNodes.remove(nodeName)) != null) {
            freeHosts.add(host);
            logger.debug("Node " + nodeName + " removed. #freeHosts:" + freeHosts.size() + " #registered: " +
                registeredNodes.size());
            try {
                this.killNodeImpl(node, host);
            } catch (Exception e) {
                logger.trace("An exception occurred during node removal", e);
            }
        } else {
            logger.error("Node " + nodeName +
                " is not known as a node belonging to this infrastructure manager");
        }
    }

    /**
     * Launch the node on the host passed as parameter
     * @param host The host on which one the node will be started
     * @throws RMException If the node hasn't been started. Very important to take care of that
     * in implementations to keep the infrastructure in a coherent state.
     */
    protected abstract void startNodeImpl(InetAddress host) throws RMException;

    /**
     * Kills the node passed as parameter
     * @param node The node to kill
     * @param host
     * @throws RMException if a problem occurred while removing
     */
    protected abstract void killNodeImpl(Node node, InetAddress host) throws RMException;
}
