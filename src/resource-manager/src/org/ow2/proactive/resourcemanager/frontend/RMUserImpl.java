/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.resourcemanager.frontend;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.authentication.RestrictedService;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * Resource Manager User class.
 * Provides a way to perform user operations in Resource manager (RM).
 * We consider the ProActive scheduler as an 'user' of RM.
 * So the user (scheduler) launch tasks on nodes, it asks node to the RM.
 * and give back nodes at the end of the tasks. That the two operations
 * of an user :<BR>
 * - ask nodes or get nodes.<BR>
 * - give back nodes or free nodes.<BR><BR>
 *
 * Scheduler can ask nodes that verify criteria. selections criteria are
 * defined in a test script that provide kind of boolean result :
 * node suitable or not suitable.<BR>
 * This script is executed in the node before selecting it,
 * If the node match criteria, it is selected, otherwise RM tries the selection script
 * on other nodes
 *
 *  @see org.ow2.proactive.scripting.SelectionScript
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public class RMUserImpl extends RestrictedService implements RMUser, InitActive {

    /** Log4J logger name for RMUser */
    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.USER);

    /** RMcore active object Stub of the RM */
    private RMCoreInterface rmcore;

    private RMAuthentication authentication;

    /**
     * ProActive empty constructor
     */
    public RMUserImpl() {
    }

    /**
     * Creates the RM user object
     * @param rmcore stub of the RMCore active object
     */
    public RMUserImpl(RMCoreInterface rmcore, RMAuthentication authentication) {
        if (logger.isDebugEnabled()) {
            logger.debug("RMUser constructor");
        }

        this.rmcore = rmcore;
        this.authentication = authentication;
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            PAActiveObject.registerByName(PAActiveObject.getStubOnThis(),
                    RMConstants.NAME_ACTIVE_OBJECT_RMUSER);

            registerTrustedService(authentication);
            registerTrustedService(rmcore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#isAlive()
     */
    public boolean isAlive() {
        return true;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#getFreeNodesNumber()
     */
    public IntWrapper getFreeNodesNumber() {
        return rmcore.getSizeListFreeRMNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#getTotalNodesNumber()
     */
    public IntWrapper getTotalNodesNumber() {
        return rmcore.getNbAllRMNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#getAtMostNodes(org.objectweb.proactive.core.util.wrapper.IntWrapper, org.ow2.proactive.scripting.SelectionScript)
     */
    public NodeSet getAtMostNodes(IntWrapper nbNodes, SelectionScript selectionScript) {
        return rmcore.getAtMostNodes(nbNodes, selectionScript, null);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#getAtMostNodes(org.objectweb.proactive.core.util.wrapper.IntWrapper, org.ow2.proactive.scripting.SelectionScript, org.ow2.proactive.utils.NodeSet)
     */
    public NodeSet getAtMostNodes(IntWrapper nbNodes, SelectionScript selectionScript, NodeSet exclusion) {
        return rmcore.getAtMostNodes(nbNodes, selectionScript, exclusion);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#getAtMostNodes(org.objectweb.proactive.core.util.wrapper.IntWrapper, java.util.List, org.ow2.proactive.utils.NodeSet)
     */
    public NodeSet getAtMostNodes(IntWrapper nbNodes, List<SelectionScript> selectionScriptsList,
            NodeSet exclusion) {
        return rmcore.getAtMostNodes(nbNodes, selectionScriptsList, exclusion);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#getExactlyNodes(org.objectweb.proactive.core.util.wrapper.IntWrapper, org.ow2.proactive.scripting.SelectionScript)
     */
    public NodeSet getExactlyNodes(IntWrapper nbNodes, SelectionScript selectionScript) {
        if (logger.isInfoEnabled()) {
            logger.info("Nb nodes : " + nbNodes);
        }

        return rmcore.getExactlyNodes(nbNodes, selectionScript);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#freeNode(org.objectweb.proactive.core.node.Node)
     */
    public void freeNode(Node node) {
        if (logger.isDebugEnabled()) {
            logger.debug("FreeNode : " + node.getNodeInformation().getURL());
        }

        rmcore.freeNode(node);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#freeNodes(org.ow2.proactive.utils.NodeSet)
     */
    public void freeNodes(NodeSet nodes) {
        if (logger.isDebugEnabled()) {
            String freeNodes = "";

            for (Node node : nodes) {
                freeNodes += (node.getNodeInformation().getName() + " ");
            }

            logger.debug("FreeNode : " + freeNodes);
        }

        rmcore.freeNodes(nodes);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#shutdown()
     */
    public void shutdown() {
        PAActiveObject.terminateActiveObject(false);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMUser#getRMState()
     */
    public RMState getRMState() {
        RMState state = new RMState();
        state.setNumberOfFreeResources(getFreeNodesNumber());
        state.setNumberOfAllResources(getTotalNodesNumber());
        return state;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean connect(UniqueID id) {
        return registerTrustedService(id);
    }

    public void disconnect() {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        unregisterTrustedService(id);
    }

}
