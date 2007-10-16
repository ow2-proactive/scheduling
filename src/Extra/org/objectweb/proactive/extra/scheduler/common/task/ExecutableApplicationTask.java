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
package org.objectweb.proactive.extra.scheduler.common.task;

import java.util.ArrayList;

import org.objectweb.proactive.core.node.Node;


/**
 * This is the execution entry point for the application task.
 * User may override the execute(ArrayList<Node>) method.
 * The content of this method will be executed. This method provides nodes to run activeObject on them.
 * Note : the execute(TaskResult...) method is not used anymore from this class.
 *
 * @author ProActive Team
 * @version 1.0, Aug 21, 2007
 * @since ProActive 3.2
 */
public abstract class ExecutableApplicationTask extends ExecutableJavaTask {

    /**
     * <font color="red">Not used anymore in this context</font>
     * This method should never be called.
     * It is the last point for this method implementation.
     * That's why it is final. User cannot override/implement this one anymore.
     * Instead, implement the execute(ArrayList<Node>) method.
     */
    public final Object execute(TaskResult... results) {
        throw new RuntimeException(
            "This method should have NEVER been called in this context !!");
    }

    /**
     * The content of this method will be execute by the scheduler.
     * Make your own Proactive implementation using the given nodes.
     * Note : if you asked for 10 nodes, one will be used to start the task
     * and the other will be sent to you as parameters.
     *
     * @param nodes the nodes you asked for.
     * @throws any exception thrown by the user's code
     * @return any object from the user.
     */
    public abstract Object execute(ArrayList<Node> nodes)
        throws Throwable;
}
