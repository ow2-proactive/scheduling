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
package org.ow2.proactive.scheduler.common.job;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.ProActiveTask;


/**
 * Use this class to create your job if you want to define a ProActive application job.<br>
 * A ProActive job is a job that contains one task that has a list of nodes in its argument list.
 * To make this type of job, just use the default no argument constructor,
 * and set the properties you want to set.<br>
 * Then add your ProActive task using {@link #setTask(ProActiveTask)} with the given method in order to fill the job with your own task.<br>
 * <b>You must set the number of nodes you want in the task,
 * and also the task as a .class or instance.</b>
 *
 *
 * @author The ProActive Team
 * @version 3.9, Sept 14, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public class ProActiveJob extends Job {
    private ProActiveTask task = null;

    /** ProActive Empty Constructor */
    public ProActiveJob() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getType()
     */
    @Override
    public JobType getType() {
        return JobType.PROACTIVE;
    }

    /**
     * To get the unique task of this job.
     *
     * @return the unique task of this job.
     */
    public ProActiveTask getTask() {
        return task;
    }

    /**
     * To set the unique task of this job.
     *
     * @param task the task to set
     */
    public void setTask(ProActiveTask task) {
        this.task = task;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getId()
     */
    @Override
    public JobId getId() {
        // Not yet assigned
        return null;
    }
}
