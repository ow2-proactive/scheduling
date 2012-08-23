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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter.OperatingSystem;


/**
 *
 * Represents underlying infrastructure and defines ways to acquire / release nodes.<br>
 *
 * Acquisition requests are supposed to be asynchronous. When new new node is available it
 * should register itself into the resource manager by calling {@link InfrastructureManager#internalRegisterAcquiredNode(Node)}<br>
 *
 * To define a new infrastructure manager
 * - define a way to add information about further node acquisition implementing {@link InfrastructureManager#configure(Object...)}
 * - define a way to acquire a single node from underlying infrastructure in {@link InfrastructureManager#acquireNode()}
 * - define a way to acquire all available nodes from the infrastructure in the method {@link InfrastructureManager#acquireAllNodes()}
 * - register available nodes in the resource manager using {@link InfrastructureManager#internalRegisterAcquiredNode(Node)}, so they till be taken into account.
 * - add the name of new class to the resource manager configuration file (config/rm/nodesource/infrastructures).
 *
 */
public abstract class InfrastructureManager implements Serializable {

    /** class' logger */
    protected static final Logger logger = ProActiveLogger.getLogger(InfrastructureManager.class);

    /** manager's node source */
    protected NodeSource nodeSource;

    /** deploying nodes list */
    private Map<String, RMDeployingNode> deployingNodes = new ConcurrentHashMap<String, RMDeployingNode>();
    private Map<String, RMDeployingNode> lostNodes = new ConcurrentHashMap<String, RMDeployingNode>();

    /** node list, miror of nodesource.getAliveNodes(), to implement random access */
    private Hashtable<String, Node> acquiredNodes = new Hashtable<String, Node>();

    private final ReentrantLock nodeAcquisitionLock = new ReentrantLock();

    //shared fields, needs to be volatile not to cache it
    private volatile boolean usingDeployingNodes = false;
    //shutdown marker
    private volatile boolean shutdown = false;
    //used to timeout the nodes
    private transient Timer timeouter = null;

    /**
     * The resource manager url. Should be provided by user.
     */
    // This url is populated during the call to the internalConfigure method
    // or setNodesource depending on the valued provided by users for the call to
    // internalConfigure.
    // Implementations can also use getRegistrationURL once the nodesource is set.
    @Configurable(description = "The URL of the resource manager")
    protected String rmUrl;
    // if true, we guess the rmurl when the nodesource is set,
    // if false, means that the user supplied a rm url.
    private boolean rmUrlGuess = false;
    //export of the field's name, used by the RMCore
    public static final String RM_URL_FIELD_NAME = "rmUrl";

    public InfrastructureManager() {
    }

    /**
     * Sets an infrastructure node source.
     * Also sets the field rmUrl if it is not provided by users.
     * @param nodeSource policy node source
     */
    public void setNodeSource(NodeSource nodeSource) {
        this.nodeSource = nodeSource;
        if (rmUrlGuess) {
            this.rmUrl = this.nodeSource.getRegistrationURL();
        }
    }

    /**
     * To retrieve nodes whose registration status is deploying or lost.
     * @return nodes whose registration status is deploying or lost.
     */
    public ArrayList<RMDeployingNode> getDeployingNodes() {
        ArrayList<RMDeployingNode> result = new ArrayList<RMDeployingNode>();
        synchronized (deployingNodes) {
            result.addAll(this.deployingNodes.values());
            result.addAll(this.lostNodes.values());
        }
        return result;
    }

    /**
     * To remove a deploying node given its url
     * @param pnUrl the url of the deploying node to remove.
     * @return true if successful, false otherwise
     */
    public final boolean internalRemoveDeployingNode(String pnUrl) {
        RMDeployingNode pn = null;
        boolean isLost = false;
        synchronized (deployingNodes) {
            pn = this.deployingNodes.remove(pnUrl);
            if (pn == null) {
                pn = this.lostNodes.remove(pnUrl);
                isLost = true;
            }
        }
        //if such a deploying or lost node exists
        if (pn != null) {
            String url = pn.getNodeURL();
            RMNodeEvent event = new RMNodeEvent(pn, RMEventType.NODE_REMOVED, pn.getState(), pn.getProvider()
                    .getName());
            emitEvent(event);
            logger.trace("DeployingNode " + url + " removed from IM");
            //one notifies listeners about the deploying node removal
            //if the node is not lost
            if (!isLost) {
                this.notifyDeployingNodeLost(pn.getNodeURL());
            }
            return true;
        } else {
            logger.trace("DeployingNode: " + pnUrl + " no more managed by IM, cannot remove it");
            return false;
        }
    }

    /**
     * Performs some cleanup ( essentially removal of the cached node ) and call removeNodeImpl
     * @param node the node to be removed
     * @throws RMException
     */
    public final void internalRemoveNode(Node node) throws RMException {
        try {
            this.acquiredNodes.remove(node.getNodeInformation().getName());
        } catch (Exception e) {
            logger.warn("Exception occurred while removing node " + node);
        }
        this.removeNode(node);
    }

    /**
     * This method is called by the RMCore to notify the InfrastructureManager
     * that a new node was added to the core. If the IM throws an exception,
     * the node is not added.
     * Note that this method is in mutual exclusion with {@link #checkNodeIsAcquiredAndDo(String, Runnable, Runnable)}
     * At this point, if a previous call to {@link InfrastructureManager#addDeployingNode(String, String, String, long)} was made (means that
     * implementor uses deploying nodes features), this method ensures that the implementation method (see {@link InfrastructureManager#notifyAcquiredNode(Node)}
     * is only called if no timeout has occurred for the associated deploying node.
     * @param node the newly added node
     * @throws RMException
     */
    public final void internalRegisterAcquiredNode(Node node) throws RMException {
        //if implementation doesn't use deploying nodes, we just execute factory method and return
        if (!usingDeployingNodes) {
            this.notifyAcquiredNode(node);
            return;
        }
        //here we use deploying nodes and timeout
        RMDeployingNode pn = null;
        //we build the url of the associated deploying node
        String deployingNodeURL = this.buildDeployingNodeURL(node.getNodeInformation().getName());
        synchronized (nodeAcquisitionLock) {
            pn = this.deployingNodes.remove(deployingNodeURL);
            //if a deploying node with this name exists, one runs the implementation callback
            if (pn != null) {
                RMNodeEvent event = new RMNodeEvent(pn, RMEventType.NODE_REMOVED, pn.getState(), pn
                        .getProvider().getName());
                emitEvent(event);
                this.notifyAcquiredNode(node);
                //if everything went well with the new node, caching it
                try {
                    this.acquiredNodes.put(node.getNodeInformation().getName(), node);
                } catch (Exception e) {
                    //if an exception occurred, we don't want to discard the node registration
                    logger.warn("Cannot cache the node in the InfrastructureManager after registration: " +
                        node, e);
                }
            } else {
                String url = node.getNodeInformation().getURL();
                logger.warn("Not expected node registered, discarding it: " + url);
                throw new RMException("Not expected node registered, discarding it: " + url);
            }
        }
    }

    /**
     * called by the node source at configuration time. Shifts the parameter array
     * once done to let implementation only care about their own configurable parameters.
     * @param parameters the parameters of the infrastructure manager
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public final void internalConfigure(Object... parameters) {
        Object[] shiftedParams = null;
        if (parameters == null || parameters.length < 1) {
            //no parameter was supplied, guess the rm url
            this.rmUrlGuess = true;
            //cannot call this.nodeSource.getRegistrationURL();
            //as the nodesource is not set yet
        } else {
            String url = parameters[0].toString();
            if (url.equals("")) {
                //we guess the url
                this.rmUrlGuess = true;
            } else {
                this.rmUrl = url;
            }
            //we get the rm's url, know shifting the array for implementations
            shiftedParams = new Object[parameters.length - 1];
            System.arraycopy(parameters, 1, shiftedParams, 0, parameters.length - 1);
        }
        this.configure(shiftedParams);
    }

    /**
     * Called by the node source at shutdown time.
     * First removes every registered Deploying and Lost nodes
     * and delegates the call to the implementation thanks to
     * the method {@link #shutDown()}
     */
    public final void internalShutDown() {
        this.shutdown = true;
        //first removing deploying nodes
        for (String dnUrl : this.deployingNodes.keySet()) {
            this.internalRemoveDeployingNode(dnUrl);
        }
        //no need to remove lost nodes, implementation is notified
        //at the timeout of the deploying node
        this.deployingNodes.clear();
        //delegating the call to the implementation
        this.shutDown();

        if (timeouter != null) {
            timeouter.cancel();
        }
    }

    //**********************************************************************************************\\
    //**************************************** SPI methods *****************************************\\
    //**********************************************************************************************\\
    /**
     * Adds information required to deploy nodes in the future.
     * Do not initiate a real nodes deployment/acquisition as it's up to the
     * policy.
     * @param the parameters of the infrastructure manager
     * @throws IllegalArgumentException if the parameters are invalid
     */
    protected abstract void configure(Object... parameters);

    /**
     * Asynchronous node acquisition request.
     * Proactive node should be registered by calling {@link InfrastructureManager#internalRegisterAcquiredNode(Node)}
     */
    public abstract void acquireNode();

    /**
     * Asynchronous request of all nodes acquisition.
     * Proactive nodes should be registered by calling {@link InfrastructureManager#internalRegisterAcquiredNode(Node)}
     */
    public abstract void acquireAllNodes();

    /**
     * Removes the node from the resource manager.
     * @param node node to release
     * @throws RMException if any problems occurred
     */
    public abstract void removeNode(Node node) throws RMException;

    /**
     * Notifies the user that the deploying node was lost or removed (because of a timeout, user interaction...)
     * Default empty implementation is provided because implementors don't
     * necessary use this feature. Anyway, if they decide to do so, they can
     * override this method, for instance, to change a flag that would
     * get a control loop to exit...
     * @param pnURL the deploying node's URL for which one the timeout occurred.
     */
    protected void notifyDeployingNodeLost(String pnURL) {
    }

    /**
     * Notifies the implementation of the infrastructure manager that a new node has been registered.
     * If this method throws an exception, the node registration will be discarded.
     * This method is always called if implementor doesn't use deploying nodes (no call to {@link InfrastructureManager#addDeployingNode(String, String, String, long)}
     * was made), and is called only for deploying nodes for which one no timeout occurred.
     * @param node the newly registered node
     * @throws RMException if the implementation does not approve the node acquisition request
     */
    protected abstract void notifyAcquiredNode(Node node) throws RMException;

    /**
     * Notify this infrastructure it is going to be shut down along with
     * its nodesource. All necessary cleanup should be done here.
     * @param initiator
     * @return mainly to be able to put a checkpoint and to be sure that
     */
    protected void shutDown() {
    }

    //**********************************************************************************************\\
    //**************************************** API methods *****************************************\\
    //**********************************************************************************************\\

    /**
     * This method returns a {@link RMNodeStarter.CommandLineBuilder} filled in with "default" settings.
     * That means that the returned CommandLineBuilder is useable as such.
     * <ul><li>
     * It tries to set the Java Path to use, either JAVA_HOME retrieved from your environment or java.home
     * set by Java itself.
     * </li><li>
     * The target operating system is set to {@link OperatingSystem#UNIX}
     * </li><li>
     * If a ProActive configuration file is provided, it is used as such.
     * </li><li>
     * Finally, it tries to set the nodesource's name, the rm's URL and the node's name.
     * </li></ul>
     * @param targetOS the operating system on which one the node will be deployed
     */
    protected final RMNodeStarter.CommandLineBuilder getDefaultCommandLineBuilder(OperatingSystem targetOS) {
        RMNodeStarter.CommandLineBuilder result = new RMNodeStarter.CommandLineBuilder();
        String javaPath = (System.getenv("JAVA_HOME") != null ? System.getenv("JAVA_HOME") : System
                .getProperty("java.home")) +
            targetOS.fs + "bin" + targetOS.fs + "java";
        result.setJavaPath(javaPath);
        result.setTargetOS(targetOS);
        if (CentralPAPropertyRepository.PA_CONFIGURATION_FILE.isSet()) {
            try {
                result
                        .setPaProperties(new File(CentralPAPropertyRepository.PA_CONFIGURATION_FILE
                                .getValue()));
            } catch (IOException e) {
                logger.debug("Cannot set default pa configuration file for " +
                    RMNodeStarter.CommandLineBuilder.class.getSimpleName(), e);
            }
        }
        result.setRmURL(this.rmUrl);
        if (this.nodeSource != null) {
            String nsName = this.nodeSource.getName();
            result.setSourceName(nsName);
            result.setNodeName(nsName + "_DefaultNodeName");
        }
        return result;
    }

    /**
     * Returns an empty {@link RMNodeStarter.CommandLineBuilder}
     * @return Returns an empty {@link RMNodeStarter.CommandLineBuilder}
     */
    protected final RMNodeStarter.CommandLineBuilder getEmptyCommandLineBuilder() {
        return new RMNodeStarter.CommandLineBuilder();
    }

    /** Creates a new RMDeployingNode's, stores it in a local ArrayList and notify the
     * owning NodeSource of the RMDeployingNode creation
     * @param name The RMDeployingNode's name.
     * @param description The RMDeployingNode's description
     * @param the timeout after which one the deploying node will be declared lost. ( node acquisition after this timeout is discarded )
     * @return The newly created RMDeployingNode's URL.
     * @throws UnsupportedOperationException if the infrastructure manager is shuting down
     */
    protected final String addDeployingNode(String name, String command, String description,
            final long timeout) {
        checkName(name);
        checkTimeout(timeout);
        if (shutdown) {
            throw new UnsupportedOperationException("The infrastructure manager is shuting down.");
        }
        //if the user calls this method, we use the require nodes/timeout mecanism
        usingDeployingNodes = true;
        NodeSource nsStub = this.nodeSource.getStub();
        RMDeployingNode result = RMDeployingNodeAccessor.getDefault().newRMDeployingNode(name, nsStub,
                command, nsStub.getAdministrator(), description);
        final String resultURL = result.getNodeURL();
        this.deployingNodes.put(result.getNodeURL(), result);
        logger.trace("New DeployingNode " + name + " instanciated in IM");
        RMNodeEvent event = new RMNodeEvent(result, RMEventType.NODE_ADDED, null, result.getProvider()
                .getName());
        emitEvent(event);
        this.sched(new TimerTask() {
            @Override
            public void run() {
                InfrastructureManager.this.timeout(resultURL, timeout);
            }
        }, timeout);
        return result.getNodeURL();
    }

    /**
     * To update the description of a deploying node. If a timeout has occurred for this node, the update is discarded.
     * @param toUpdateURL The RMDeployingNode's URL whose description will be updated.
     * @param newDescription The new description
     * @return true in case of success, false if the deploying node is not managed by the IM anymore.
     */
    protected final boolean updateDeployingNodeDescription(String toUpdateURL, String newDescription) {
        RMDeployingNode pn = this.deployingNodes.get(toUpdateURL);
        if (pn != null) {
            NodeState previousState = pn.getState();
            RMDeployingNodeAccessor.getDefault().setDescription(pn, newDescription);
            RMNodeEvent event = new RMNodeEvent(pn, RMEventType.NODE_STATE_CHANGED, previousState, pn
                    .getProvider().getName());
            emitEvent(event);
            logger.trace("DeployingNode " + toUpdateURL + " updated in IM");
            return true;
        } else {
            logger.trace("DeployingNode " + toUpdateURL + " no more managed by the IM, cannot update it");
            return false;
        }
    }

    /**
     * Declares a deploying node lost. Future attempts to modify the deploying node will be ignored.
     * @param toUpdateURL The RMDeployingNode's URL which is to be declared as lost
     * @param description the new rmdeployingnode's description, can be null.
     * @return true if the method ran successfully, false otherwise.
     */
    protected final boolean declareDeployingNodeLost(String toUpdateURL, String description) {
        RMDeployingNode pn = null;
        //we need to atomically move the node from the deploying collection to the lost one.
        synchronized (deployingNodes) {
            pn = this.deployingNodes.remove(toUpdateURL);
            if (pn != null) {
                this.lostNodes.put(toUpdateURL, pn);
            }
        }
        if (pn != null) {
            NodeState previousState = pn.getState();
            RMDeployingNodeAccessor.getDefault().setLost(pn);
            if (description != null) {
                RMDeployingNodeAccessor.getDefault().setDescription(pn, description);
            }
            RMNodeEvent event = new RMNodeEvent(pn, RMEventType.NODE_STATE_CHANGED, previousState, pn
                    .getProvider().getName());
            emitEvent(event);
            if (logger.isTraceEnabled()) {
                logger.trace(RMDeployingNode.class.getSimpleName() + " " + toUpdateURL +
                    " declared lost in IM");
            }
            return true;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace(RMDeployingNode.class.getSimpleName() + " " + toUpdateURL +
                    " no more managed by IM, cannot declare it as lost");
            }
            return false;
        }
    }

    /**
     * Check if the Node with the given name is already acquired ( usefull when the launcher of the process which is supposed to launch a RMNode
     * waits for its registration ). At the same time, this methode executes the first runnable is the node has been found amoung acquired nodes and
     * executes the second runnable if the node has not been found. Note that this method is in mutual exclusion with
     * {@link InfrastructureManager#internalRegisterAcquiredNode(Node)} to avoid races.
     * @param nodeName the node's name
     * @param toRunWhenOK the Runnable that will be launched if the node is already acquired (can be null)
     * @param toRunWhenKO the Runnable that will be launched if the node has not been found among nodesource.getAliveNodes(). (can be null)
     * @return true if the node with such name is already acquired false otherwise.
     */
    protected final boolean checkNodeIsAcquiredAndDo(String nodeName, Runnable toRunWhenOK,
            Runnable toRunWhenKO) {
        synchronized (nodeAcquisitionLock) {
            if (this.acquiredNodes.containsKey(nodeName)) {
                if (toRunWhenOK != null) {
                    try {
                        toRunWhenOK.run();
                    } catch (Exception e) {
                        logger
                                .warn(
                                        "An exception occurred while running implementation's code whereas the node " +
                                            nodeName + " was found.", e);
                    }
                }
                return true;
            } else {
                if (toRunWhenKO != null) {
                    try {
                        toRunWhenKO.run();
                    } catch (Exception e) {
                        logger
                                .warn(
                                        "An exception occurred while running implementation's code whereas the node " +
                                            nodeName + " was not found.", e);
                    }
                }
                return false;
            }
        }
    }

    //**********************************************************************************************//
    //*********************** Package private accessors & Helpers **********************************//
    //**********************************************************************************************//

    /**
     * To emit an event and register it in the database
     */
    private void emitEvent(final RMNodeEvent event) {
        NodeSource nsStub = this.nodeSource.getStub();
        nsStub.internalEmitDeployingNodeEvent(event);
    }

    private void timeout(String pnURL, long timeout) {
        if (this.declareDeployingNodeLost(pnURL, "Timeout occurred after " + timeout + " ms.")) {
            this.notifyDeployingNodeLost(pnURL);
        }
    }

    private synchronized void sched(TimerTask task, long delay) {
        if (this.timeouter == null) {
            this.timeouter = new Timer("InfrastructureManager Timer");
        }
        this.timeouter.schedule(task, delay);
    }

    private void checkTimeout(long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }

    }

    private void checkName(String name) {
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Deploying node name cannot contain white spaces: \"" + name +
                "\" is forbidden");
        }
        String pnURL = this.buildDeployingNodeURL(name);
        if (this.deployingNodes.containsKey(pnURL) || this.lostNodes.containsKey(pnURL)) {
            throw new IllegalArgumentException(RMDeployingNode.class.getSimpleName() +
                " with the same name (\"" + name + "\") has already been created");
        }
    }

    /**
     * Builds the name of the deploying node given its name
     * @param pnName The name of the deploying node
     * @return the URL of the deploying node
     */
    private String buildDeployingNodeURL(String pnName) {
        return RMDeployingNode.PROTOCOL_ID + "://" + this.nodeSource.getName() + "/" + pnName;
    }

    /**
     * Helper nested class. Used not to expose methods that should be package private of the
     * {@link RMDeployingNode} object.
     */
    public static abstract class RMDeployingNodeAccessor implements Serializable {
        private static volatile RMDeployingNodeAccessor DEFAULT;

        public static void setDefault(RMDeployingNodeAccessor d) {
            RMDeployingNodeAccessor.DEFAULT = d;
        }

        private static RMDeployingNodeAccessor getDefault() {
            if (RMDeployingNodeAccessor.DEFAULT != null) {
                return RMDeployingNodeAccessor.DEFAULT;
            }
            try {
                Class.forName(RMDeployingNode.class.getName(), true, RMDeployingNode.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return RMDeployingNodeAccessor.DEFAULT;
        }

        /**
         * Instantiate a new {@link RMDeployingNode} with the given parameters
         * @param name The deploying node's name
         * @param ns The node source owning the deploying node
         * @param command The command that has been used to launch the {@link RMNode}
         * @param provider The node's provider
         * @param description A first description of the node
         * @return The newly created deploying node
         */
        protected abstract RMDeployingNode newRMDeployingNode(String name, NodeSource ns, String command,
                Client provider, String description);

        /**
         * To set the Description of the {@link RMDeployingNode}
         * @param pn the deploying node to update
         * @param newDescription the new description
         */
        protected abstract void setDescription(RMDeployingNode pn, String newDescription);

        /**
         * To update the lost field of the deploying node
         * @param pn The deploying node to update
         * @param lost the value of lost
         */
        protected abstract void setLost(RMDeployingNode pn);
    }
}
