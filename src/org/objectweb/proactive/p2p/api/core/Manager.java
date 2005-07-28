/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.p2p.api.core;

import org.apache.log4j.Logger;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import java.io.Serializable;

import java.util.Vector;


/**
 * @author Alexandre di Costanzo
 *
 * Created on May 31, 2005
 */
public class Manager implements Serializable, InitActive {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_MANAGER);
    private Task rootTask = null;
    private Node[] nodes = null;
    private Worker workerGroup;
    private Vector tasks;

    /**
     * The no args constructor for ProActive.
     */
    public Manager() {
        // nothing to do
    }

    /**
     * @param root the root task.
     * @param nodes the array of nodes for the computation.
     */
    public Manager(Task root, Node[] nodes) {
        try {
            this.rootTask = (Task) ProActive.turnActive(root);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with the turn active of the root task", e);
            throw new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("Problem with the node of the root task", e);
            throw new RuntimeException(e);
        }
        this.nodes = nodes;
    }

    public void initActivity(Body body) {
        // Group of Worker
        Object[][] args = new Object[this.nodes.length][1];
        for (int i = 0; i < args.length; i++) {
            args[i][0] = ProActive.getStubOnThis();
        }
        try {
            ProActiveGroup.newGroup(Worker.class.getName(), args, this.nodes);
        } catch (ClassNotReifiableException e) {
            logger.fatal("The Worker is not reifiable", e);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Problem with active objects creation", e);
        } catch (NodeException e) {
            logger.fatal("Problem with a node", e);
        } catch (ClassNotFoundException e) {
            logger.fatal("The class for worker was not found", e);
        }

        // Creating tasks
        int split = (this.rootTask.shouldISplit()).intValue();
        if ((split >= Task.SPLIT_CODE_MIN) && (split <= Task.SPLIT_CODE_MAX)) {
            switch (split) {
            case 1:
                this.tasks = this.rootTask.split();
                logger.info("split called");
                break;
            case 2:
                this.tasks = this.rootTask.splitInN(this.rootTask.getNForSplit()
                                                                 .intValue());
                logger.info("split in n called");
                break;
            case 3:
                this.tasks = this.rootTask.splitAtMost(this.rootTask.getNForSplit()
                                                                    .intValue());
                logger.info("split at most called");
                break;
            case 4:
                this.tasks = this.rootTask.splitAtLeast(this.rootTask.getNForSplit()
                                                                     .intValue());
                logger.info("split at least called");
                break;
            default:
                logger.fatal("A split method not handled by the manager: " +
                    split);
                throw new RuntimeException(
                    "A split method not handled by the manager");
            }
        } else {
            logger.info("No first split to do: not yet implemented :(");
            // TODO What can we do?
        }
    }
    
    public void start() {
    	// TODO if is active
    }
    
    public BooleanWrapper isFinish() {
    	// TODO check is computation start
    	return null;
    }
    
    public Result getFinalResult() {
    	// TODO check is finish before
    	return null;
    }
}
