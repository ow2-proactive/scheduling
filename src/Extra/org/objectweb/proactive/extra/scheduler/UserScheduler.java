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
 *        This is the interface to the user
 * @author walzouab
 *
 */
package org.objectweb.proactive.extra.scheduler;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.exception.UserException;


public class UserScheduler extends SchedulerUserAPI {
    private static long taskID = 0;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);
    private boolean stopped;
    Scheduler scheduler;

    /**
     * NoArg constructor for proactive
     *
     */
    public UserScheduler() {
    }

    /**
     * constructor , an instance of the user scheduler must have a link to the scheduler core to function
     * The scheduler satrts always in stopped mode
     * @param s-Scheduler Core
     */
    public UserScheduler(Scheduler s) {
        scheduler = s;
        stopped = true;
    }

    public Vector<UserResult> submit(Vector<ProActiveTask> userTasks,
        String userName) throws UserException {
        if (stopped) {
            throw new UserException(
                "Scheduler is stopped. Please contact the administator");
        }

        //		must wrap in a vector because policies only take vectors
        Vector<InternalTask> tasks = new Vector<InternalTask>();
        Vector<UserResult> userResultVector = new Vector<UserResult>();
        String tempID;
        for (int i = 0; i < userTasks.size(); i++) {
            tempID = "" + (++taskID);
            tasks.add(new InternalTask(userTasks.get(i), tempID, userName));

            userResultVector.add(new UserResult(tempID, userName,
                    (UserScheduler) ProActive.getStubOnThis()));
        }

        scheduler.submit(tasks);
        return userResultVector;
    }

    public UserResult submit(ProActiveTask userTask, String userName)
        throws UserException {
        if (stopped) {
            throw new UserException(
                "Scheduler is stopped. Please contact the administator");
        }

        //must wrap in a vector because policies only take vectors
        Vector<InternalTask> task = new Vector<InternalTask>();

        String ID = "" + (++taskID);

        task.add(new InternalTask(userTask, ID, userName));

        scheduler.submit(task);

        return new UserResult(ID, userName,
            (UserScheduler) ProActive.getStubOnThis());
    }

    public InternalResult getResult(String taskID1, String userName)
        throws UserException {
        if (stopped) {
            throw new UserException(
                "Scheduler is stopped. Please contact the administator");
        }

        return scheduler.getResult(taskID1, userName);
    }

    /**
     * Allows the user to start submitting commands
     * @return true if it is started, return false if it is already started
     */
    public BooleanWrapper start() {
        if (stopped == false) {
            return new BooleanWrapper(false);
        } else {
            stopped = false;
            logger.info("Scheduler Started, users can interact with it");
            return new BooleanWrapper(true);
        }
    }

    /**
     * stops the user from  submitting commands
     * @return true if it is  stopped, return false if it is already stopped
     */
    public BooleanWrapper stop() {
        if (stopped == true) {
            return new BooleanWrapper(false);
        } else {
            stopped = true;
            logger.info("Scheduler Stopped, users cannot interact with it");
            return new BooleanWrapper(true);
        }
    }

    public BooleanWrapper isFinished(String taskID1) throws UserException {
        if (stopped) {
            throw new UserException(
                "Scheduler is stopped. Please contact the administator");
        }
        return scheduler.isFinished(taskID1);
    }

    public void shutdown() {
        try {
            //			FIXME :must make sure that futures have been propagated using automatic continuation
            logger.warn(
                "FIX ME: Bugs #945 in proacive forge Will sleep in user scheduler to allow for automatic continuation to propagate");
            Thread.sleep(1000);

            ProActive.getBodyOnThis().terminate();
        } catch (Exception e) {
            logger.info("error terminating userscheulder " + e.getMessage());
        }
    }

    public BooleanWrapper del(String tID, String userName)
        throws UserException {
        if (stopped) {
            throw new UserException(
                "Scheduler is stopped. Please contact the administator");
        }

        return scheduler.del(tID, userName);
    }

    public Status stat(String tID) throws UserException {
        if (stopped) {
            throw new UserException(
                "Scheduler is stopped. Please contact the administator");
        }

        Vector<String> temp;

        temp = scheduler.getQueuedID();
        if (temp.contains(tID)) {
            return Status.QUEUED;
        }
        temp = scheduler.getRunningID();
        if (temp.contains(tID)) {
            return Status.RUNNNING;
        }

        temp = scheduler.getFinishedID();
        if (temp.contains(tID)) {
            return Status.FINISHED;
        }

        temp = scheduler.getFailedID();
        if (temp.contains(tID)) {
            return Status.FAILED;
        }

        temp = scheduler.getKilledID();
        if (temp.contains(tID)) {
            return Status.KILLED;
        }

        // doesnt exist
        return Status.NEW;
    }
}
