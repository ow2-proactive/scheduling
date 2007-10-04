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
package org.objectweb.proactive.extra.scheduler.common.job;

import org.objectweb.proactive.extra.scheduler.common.task.ApplicationTask;


/**
 * Definition of an application job for the user.
 * An application job is a job that contains a task that have a list of node in its parameters list.
 * To make this type of job, just use the default no params constructor,
 * and set the properties you want to set.
 * Then add your application task with the given method in order to fill the job with your own tasks.
 * You must set the number of nodes you want in the task,
 * and also the task as a .class or instance.
 *
 * @author ProActive Team
 * @version 1.0, Sept 14, 2007
 * @since ProActive 3.2
 */
public class ApplicationJob extends Job {

    /** Serial Version UID */
    private static final long serialVersionUID = 1623955669459590983L;
    private ApplicationTask task = null;

    /** Proactive Empty Constructor */
    public ApplicationJob() {
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.job.Job#getType()
     */
    @Override
    public JobType getType() {
        return JobType.APPLI;
    }

    /**
     * To get the unique task of this job.
     *
     * @return the unique task of this job.
     */
    public ApplicationTask getTask() {
        return task;
    }

    /**
     * To set the unique task of this job.
     *
     * @param task the task to set
     */
    public void setTask(ApplicationTask task) {
        this.task = task;
    }
}
