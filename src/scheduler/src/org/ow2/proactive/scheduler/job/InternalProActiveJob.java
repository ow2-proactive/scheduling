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
package org.ow2.proactive.scheduler.job;

import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalProActiveTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * Class ProActiveJob.
 * This is the definition of an ProActive job.
 * The nodes use is under user responsibility.
 *
 * @author The ProActive Team
 * @version 3.9, Jun 7, 2007
 * @since ProActive 3.9
 */
public class InternalProActiveJob extends InternalJob {

    /**
     * ProActive empty constructor.
     */
    public InternalProActiveJob() {
    }

    private void createTask(String executableClassName, Map<String, String> args) {
        InternalProActiveTask descriptor = new InternalProActiveTask(new JavaExecutableContainer(
            executableClassName, args));
        descriptor.setPreciousResult(true);
        super.addTask(descriptor);
    }

    /**
     * Create a new ProActive Job with the given parameters. It provides method to get the created task.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param cancelOnError true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     */

    /**
     * Create a new instance of InternalProActiveJob.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param cancelOnError true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     * @param executableClassName the proActive task to execute
     * @param args the arguments attach to this job.
     * @param numberOfNodesNeeded The number of nodes needed for the application.
     */
    public InternalProActiveJob(String name, JobPriority priority, boolean cancelOnError, String description,
            String executableClassName, Map<String, String> args, int numberOfNodesNeeded) {
        super(name, priority, cancelOnError, description);
        createTask(executableClassName, args);
        getTask().setNumberOfNodesNeeded(numberOfNodesNeeded);
    }

    //    /**
    //     * Create a new ProActive Job with the given parameters.  It provides method to get the created task.
    //     * You can here had the number of nodes you want for your ProActive job.
    //     *
    //     * @param name the current job name.
    //     * @param priority the priority of this job between 1 and 5.
    //     * @param cancelOnError true if the job has to run until its end or an user intervention.
    //     * @param description a short description of the job and what it will do.
    //     * @param numberOfNodesNeeded the number of node needed by the user.
    //     */
    //
    //    //   * @param runtimeLimit the maximum execution time for this job given in millisecond.
    //    public InternalProActiveJob(String name, JobPriority priority, boolean cancelOnError, String description,
    //            int numberOfNodesNeeded) {
    //        this(name, priority, cancelOnError, description);
    //        getTask().setNumberOfNodesNeeded(numberOfNodesNeeded);
    //    }

    //    /**
    //     * Create a new ProActive Job with the given parameters.  It provides method to get the created task.
    //     * You can here had the number of nodes you want for your ProActive job.
    //     *
    //     * @param name the current job name.
    //     * @param priority the priority of this job between 1 and 5.
    //     * @param runtimeLimit the maximum execution time for this job given in millisecond.
    //     * @param cancelOnError true if the job has to run until its end or an user intervention.
    //     * @param description a short description of the job and what it will do.
    //     * @param numberOfNodesNeeded the number of node needed by the user.
    //     * @param taskClass the Class instance of the class to instantiate.
    //     */
    //    public InternalProActiveJob(String name, JobPriority priority, boolean cancelOnError, String description,
    //            int numberOfNodesNeeded, Class<ProActiveExecutable> taskClass) {
    //        this(name, priority, cancelOnError, description, numberOfNodesNeeded);
    //        getTask().setExecutableClassName(taskClass);
    //    }

    //    /**
    //     * Create a new ProActive Job with the given parameters.  It provides method to get the created task.
    //     * You can here had the number of nodes you want for your ProActive job.
    //     *
    //     * @param name the current job name.
    //     * @param priority the priority of this job between 1 and 5.
    //     * @param runtimeLimit the maximum execution time for this job given in millisecond.
    //     * @param cancelOnError true if the job has to run until its end or an user intervention.
    //     * @param description a short description of the job and what it will do.
    //     * @param numberOfNodesNeeded the number of node needed by the user.
    //     * @param task the instantiated class task object.
    //     */
    //    public InternalProActiveJob(String name, JobPriority priority, boolean cancelOnError, String description,
    //            int numberOfNodesNeeded, ProActiveExecutable task) {
    //        this(name, priority, cancelOnError, description, numberOfNodesNeeded);
    //        getTask().setTask(task);
    //    }

    /**
     * Create a new ProActive Job with the given parameters.  It provides method to get the created task.
     * You can here had the number of nodes you want for your ProActive job.
     *
     * @param numberOfNodesNeeded
     * @param executableClassName
     * @param args the arguments attach to this job.
     */
    public InternalProActiveJob(int numberOfNodesNeeded, String executableClassName, Map<String, String> args) {
        createTask(executableClassName, args);
        getTask().setNumberOfNodesNeeded(numberOfNodesNeeded);
    }

    //    /**
    //     * Create a new ProActive Job with the given parameters.  It provides method to get the created task.
    //     * You can here had the number of nodes you want for your ProActive job.
    //     *
    //     * @param numberOfNodesNeeded the number of node needed by the user.
    //     * @param task the instantiated class task object.
    //     */
    //    public InternalProActiveJob(int numberOfNodesNeeded, ProActiveExecutable task) {
    //        createTask();
    //        getTask().setNumberOfNodesNeeded(numberOfNodesNeeded);
    //        getTask().setTask(task);
    //    }

    /**
     * Should never be called !
     */
    @Override
    public boolean addTask(InternalTask task) {
        throw new RuntimeException("This method should have NEVER been called in ProActiveJob.");
    }

    /**
     * Get the ProActive task created while ProActive job creation.
     *
     * @return the ProActive task created while ProActive job creation.
     */
    public InternalProActiveTask getTask() {
        return (InternalProActiveTask) getTasks().get(0);
    }

    /**
     * @see org.ow2.proactive.scheduler.job.InternalJob#getType()
     */
    @Override
    public JobType getType() {
        return JobType.PROACTIVE;
    }
}
