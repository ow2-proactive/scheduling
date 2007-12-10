/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMConstants;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreInterface;
import org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript;


/**
 * Infrastructure Manager User class.
 * Provides a way to perform user operations in infrastructure manager (IM).
 * We consider the ProActive scheduler as an 'user' of IM.
 * So the user (scheduler) launch tasks on nodes, it asks node to the IM.
 * and give back nodes at the end of the tasks. That the two operations
 * of an user :<BR>
 * - ask nodes or get nodes.<BR>
 * - give back nodes or free nodes.<BR><BR>
 *
 * Scheduler can ask nodes that verify criteria. selections criteria are
 * defined in a test script that provide kind of boolean result :
 * node suitable or not suitable.<BR>
 * This script is executed in the node before selecting it,
 * If the node match criteria, it is selected, otherwise IM tries the selection script
 * on other nodes
 *
 *  @see org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript
 *
 *  @author ProActive team.
 *
 */
public class IMUserImpl implements IMUser, InitActive {

    /** serial version UID */
    private static final long serialVersionUID = 1L;

    /** Log4J logger name for IMUser */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_USER);

    /** IMcore active object Stub of the IM */
    private IMCoreInterface imcore;

    /**
     * ProActive empty constructor
     */
    public IMUserImpl() {
    }

    /**
     * Creates the IM user object
     * @param imcore stub of the IMCore active object
     */
    public IMUserImpl(IMCoreInterface imcore) {
        if (logger.isDebugEnabled()) {
            logger.debug("IMUser constructor");
        }

        this.imcore = imcore;
    }

    /**
     * Initialization part of the IMUser active object.
     * Register in RMI register the active object.
     */
    public void initActivity(Body body) {
        try {
            PAActiveObject.register((IMUser) PAActiveObject.getStubOnThis(),
                "//localhost/" + IMConstants.NAME_ACTIVE_OBJECT_IMUSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** echo function */
    public StringWrapper echo() {
        return new StringWrapper("I am IMUser");
    }

    /**
     * Provides nbNodes nodes verifying a selection script.
     * If the infrastructure manager (IM) don't have nb free nodes
     * it returns the max of valid free nodes
     * @param nbNodes the number of nodes.
     * @param selectionScript : script to be verified by the returned nodes.
     * @return an array list of nodes.
     */
    public NodeSet getAtMostNodes(IntWrapper nbNodes,
        SelectionScript selectionScript) {
        return imcore.getAtMostNodes(nbNodes, selectionScript);
    }

    /**
     * provides exactly nbNodes nodes verifying the selection script.
     * If the infrastructure manager (IM) don't have nb free nodes
     * it returns an empty node set.
     * @param nbNodes the number of nodes.
     * @param selectionScript : script to be verified by the returned nodes.
     * @return an array list of nodes.
     */
    public NodeSet getExactlyNodes(IntWrapper nbNodes,
        SelectionScript selectionScript) {
        if (logger.isInfoEnabled()) {
            logger.info("getExactlyNodes, nb nodes : " + nbNodes);
        }

        return imcore.getExactlyNodes(nbNodes, selectionScript);
    }

    /**
     * Release the node got by the user previously.
     * @param node : the node to release.
     */
    public void freeNode(Node node) {
        if (logger.isInfoEnabled()) {
            logger.info("freeNode : " + node.getNodeInformation().getURL());
        }

        imcore.freeNode(node);
    }

    /**
     * Release nodes got by the user previously.
     * @param nodes : a table of nodes to release.
     */
    public void freeNodes(NodeSet nodes) {
        if (logger.isInfoEnabled()) {
            String freeNodes = "";

            for (Node node : nodes) {
                freeNodes += (node.getNodeInformation().getName() + " ");
            }

            logger.info("freeNode : " + freeNodes);
        }

        imcore.freeNodes(nodes);
    }

    public void shutdown() {
        PAActiveObject.terminateActiveObject(false);
    }
}
