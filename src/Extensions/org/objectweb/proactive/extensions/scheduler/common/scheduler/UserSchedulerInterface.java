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
package org.objectweb.proactive.extensions.scheduler.common.scheduler;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.job.Job;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;


/**
 * Scheduler interface for someone connected to the scheduler as user.<br>
 * This interface provides methods to managed the user task and jobs on the scheduler.<br>
 * A user will only be able to managed his jobs and tasks, and also see the entire scheduling process.
 *
 * @author The ProActive Team
 * @version 3.9, Jun 7, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public interface UserSchedulerInterface extends UserDeepInterface {

    /**
     * Submit a new job to the scheduler.
     * A user can only managed their jobs.
     * <p>
     * It will execute the tasks of the jobs as soon as resources are available.
     * The job will be considered as finished once every tasks have finished.
     * </p>
     * Thus, user could get the job result according to the precious result.
     *
     * @param job the new job to submit.
     * @return the generated new job ID.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public JobId submit(Job job) throws SchedulerException;

    /**
     * Add a scheduler event Listener. this listener provides method to notice of
     * new coming job, started task, finished task, running job, finished job, etc...<br>
     * You may use this method once by thread or active object.<br>
     * Every call to this method will remove your previous listening settings.<br>
     * For example, if you want to get 2 events, add the 2 events you want at the end of this method.
     *
     * @param sel a SchedulerEventListener on which the scheduler will talk.
     * @return the scheduler current state containing the different lists of jobs.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public SchedulerInitialState<? extends Job> addSchedulerEventListener(
            SchedulerEventListener<? extends Job> sel, SchedulerEvent... events) throws SchedulerException;

    /**
     * Return the scheduler statistics.<br>
     * It will be possible to get an HashMap of all properties set in the stats class.
     *
     * @return the scheduler statistics.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public Stats getStats() throws SchedulerException;

    /**
     * Disconnect properly the user from the scheduler.
     *
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public void disconnect() throws SchedulerException;

}
