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
package org.ow2.proactive.scheduler.common.task.executable;

import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * Extends this abstract class if you want to create your own ProActive application task.<br>
 * You may override the {@link #execute(ArrayList)} method.
 * The content of this method will be executed. It provides nodes on which you can run activeObjects.<br>
 * <i>Note</i> : the {@link #execute(TaskResult...)} method is not used anymore from this class.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class ProActiveExecutable extends JavaExecutable {

    /**
     *
     */
    private static final long serialVersionUID = 10L;

    /**
     * <font color="red">Not used anymore in this context</font>
     * This method should never be called.<br>
     * It is the last point for this method implementation.
     * That's why it is final. User cannot override/implement this one anymore.<br>
     * <b>Instead, implement the {@link #execute(ArrayList)} method.</b>
     */
    @Override
    public final Serializable execute(TaskResult... results) {
        throw new RuntimeException("This method should have NEVER been called in this context !!");
    }

    /**
     * The content of this method will be execute by the scheduler.
     * Make your own ProActive implementation using the given nodes.<br>
     * <i>Note</i> : if you asked for 10 nodes, one will be used to start the task
     * and the other will be sent to you as parameters. So you will have 9 executions nodes.<br>
     * Ask for 11 if you need 10.
     *
     * @param nodes the nodes you asked for.
     * @throws Throwable any exception thrown by the user's code
     * @return any Serializable object from the user.
     */
    public abstract Serializable execute(ArrayList<Node> nodes) throws Throwable;
}
