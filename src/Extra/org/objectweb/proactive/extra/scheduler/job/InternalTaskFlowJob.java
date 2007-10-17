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
package org.objectweb.proactive.extra.scheduler.job;

import java.util.ArrayList;

import org.objectweb.proactive.extra.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extra.scheduler.common.job.JobType;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalTask;


/**
 * Class TaskFlowJob.
 * This is the definition of a tasks flow job.
 *
 * @author ProActive Team
 * @version 1.0, Jun 7, 2007
 * @since ProActive 3.2
 */
public class InternalTaskFlowJob extends InternalJob {

    /** Serial version UID */
    private static final long serialVersionUID = 5553430029462060936L;

    /**
     * ProActive empty constructor.
     */
    public InternalTaskFlowJob() {
    }

    /**
     * Create a new Tasks Flow Job with the given parameters. It provides methods to add or
     * remove tasks.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param runtimeLimit the maximum execution time for this job given in millisecond.
     * @param cancelOnError true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     */
    public InternalTaskFlowJob(String name, JobPriority priority,
        long runtimeLimit, boolean cancelOnError, String description) {
        super(name, priority, runtimeLimit, cancelOnError, description);
    }

    /**
     * Append a list of tasks to this job.
     *
     * @param tasks the list of tasks to add.
     * @return true if the list of tasks have been correctly added to the job,
     *         false if not.
     */
    public boolean addTasks(ArrayList<InternalTask> tasks) {
        for (InternalTask td : tasks) {
            if (!addTask(td)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.job.JobU#getType()
     */
    @Override
    public JobType getType() {
        return JobType.TASKSFLOW;
    }
}
