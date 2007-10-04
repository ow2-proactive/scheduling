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

import org.objectweb.proactive.extra.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extra.scheduler.common.job.JobType;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableApplicationTask;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalAppliTask;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalTask;


/**
 * Class ApplicationJob.
 * This is the definition of an application job.
 * The nodes use is under user responsability.
 *
 * @author ProActive Team
 * @version 1.0, Jun 7, 2007
 * @since ProActive 3.2
 */
public class InternalApplicationJob extends InternalJob {

    /** Serial version UID */
    private static final long serialVersionUID = 5793055959400444968L;

    /**
     * ProActive empty constructor.
     */
    public InternalApplicationJob() {
    }

    /**
     * Create a new Application Job with the given parameters. It provides method to get the created task.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param runtimeLimit the maximum execution time for this job given in millisecond.
     * @param cancelOnException true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     */
    public InternalApplicationJob(String name, JobPriority priority,
        long runtimeLimit, boolean cancelOnException, String description) {
        super(name, priority, runtimeLimit, cancelOnException, description);
        InternalAppliTask descriptor = new InternalAppliTask();
        descriptor.setFinalTask(true);
        super.addTask(descriptor);
    }

    /**
     * Create a new Application Job with the given parameters.  It provides method to get the created task.
     * You can here had the number of nodes you want for your application.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param runtimeLimit the maximum execution time for this job given in millisecond.
     * @param cancelOnException true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     * @param numberOfNodesNeeded the number of node needed by the user.
     */
    public InternalApplicationJob(String name, JobPriority priority,
        long runtimeLimit, boolean cancelOnException, String description,
        int numberOfNodesNeeded) {
        this(name, priority, runtimeLimit, cancelOnException, description);
        getTask().setNumberOfNodesNeeded(numberOfNodesNeeded);
    }

    /**
     * Create a new Application Job with the given parameters.  It provides method to get the created task.
     * You can here had the number of nodes you want for your application.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param runtimeLimit the maximum execution time for this job given in millisecond.
     * @param cancelOnException true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     * @param numberOfNodesNeeded the number of node needed by the user.
     * @param taskClass the Class instance of the class to instanciate.
     */
    public InternalApplicationJob(String name, JobPriority priority,
        long runtimeLimit, boolean cancelOnException, String description,
        int numberOfNodesNeeded, Class<ExecutableApplicationTask> taskClass) {
        this(name, priority, runtimeLimit, cancelOnException, description,
            numberOfNodesNeeded);
        getTask().setTaskClass(taskClass);
    }

    /**
     * Create a new Application Job with the given parameters.  It provides method to get the created task.
     * You can here had the number of nodes you want for your application.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param runtimeLimit the maximum execution time for this job given in millisecond.
     * @param cancelOnException true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     * @param numberOfNodesNeeded the number of node needed by the user.
     * @param taskClass the instanciated class task object.
     */
    public InternalApplicationJob(String name, JobPriority priority,
        long runtimeLimit, boolean cancelOnException, String description,
        int numberOfNodesNeeded, ExecutableApplicationTask task) {
        this(name, priority, runtimeLimit, cancelOnException, description,
            numberOfNodesNeeded);
        getTask().setTask(task);
    }

    /**
     * Should never be called !
     */
    @Override
    public boolean addTask(InternalTask task) {
        throw new RuntimeException(
            "This method should have NEVER been called in ApplicationJob.");
    }

    /**
     * Get the application task created while application job creation.
     *
     * @return the application task created while application job creation.
     */
    public InternalAppliTask getTask() {
        return (InternalAppliTask) getTasks().get(0);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.job.JobU#getType()
     */
    @Override
    public JobType getType() {
        return JobType.APPLI;
    }
}
