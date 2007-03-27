/*
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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

/**
 *
 *
 * @author walzouab
 *
 */
package org.objectweb.proactive.extra.scheduler;

import java.util.Vector;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.exception.UserException;


/**
 * An API that provides functions needed by the user
 * @author walzouab
 *
 */
public abstract class SchedulerUserAPI {

    /**
     * A non-blocking method that submits a vector of tasks to the scheduler
     * @param userTasks
     * @param userName
     * @return
     * @throws UserException
     */
    public abstract Vector<UserResult> submit(Vector<ProActiveTask> userTasks,
        String userName) throws UserException;

    /**
     * A non-blocking method that submits a single task to the scheduler
     * @param userTask
     * @param userName
     * @return
     * @throws UserException
     */
    public abstract UserResult submit(ProActiveTask userTask, String userName)
        throws UserException;

    /**
     * A blocking method that gets the user result when it is available
     * @param taskID
     * @return
     * @throws UserException
     */
    public abstract InternalResult getResult(String taskID, String userName)
        throws UserException;

    /**
     * A method that checks if the task has finsihed and returns immediately
     * @param taskID
     * @return
     * @throws UserException
     */
    public abstract BooleanWrapper isFinished(String taskID)
        throws UserException;

    /**
     * deletes a certain task
     * @param tID
     * @return true if sucessfulll, false if itsnt queued or running anymore
     * @throws UserException
     */
    public abstract BooleanWrapper del(String tID, String userName)
        throws UserException;

    /**
     * gets the status of a certain task
     * @param tID
     * @return Status--see enum staus for details
     * @throws UserException
     */
    public abstract Status status(String tID) throws UserException;

    /**
     *
     * Connects to an already exisitng scheduler and returns a reference to the user API
     * @param schedulerURL
     * @return SchedulerUserAPI
     * @throws UserException
     */
    public static SchedulerUserAPI connectTo(String schedulerURL)
        throws UserException {
        Object[] ao;
        try {
            Node node = NodeFactory.getNode(schedulerURL);

            //look up must be made on the user scheudler rather than the api because it is the real object that was instansiated
            ao = node.getActiveObjects(UserScheduler.class.getName());
        } catch (Exception e) {
            throw new UserException("Couldn't Connect to the scheduler");
        }

        if (ao.length == 1) {
            return ((SchedulerUserAPI) ao[0]);
        }

        throw new UserException(
            "Scheduler object doesnt Exist please make sure its running");
    }
}
