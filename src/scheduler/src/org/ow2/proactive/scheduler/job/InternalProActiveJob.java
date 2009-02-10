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
package org.ow2.proactive.scheduler.job;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.util.BigString;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalProActiveTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * Class ProActiveJob.
 * This is the definition of an ProActive job.
 * The nodes use is under user responsibility.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@Entity
@Table(name = "Internal_PA_Job")
@AccessType("field")
@Proxy(lazy = false)
public class InternalProActiveJob extends InternalJob {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    /**
     * ProActive empty constructor.
     */
    public InternalProActiveJob() {
    }

    private void createTask(String executableClassName, Map<String, BigString> args) {
        InternalProActiveTask descriptor = new InternalProActiveTask(new JavaExecutableContainer(
            executableClassName, args));
        descriptor.setPreciousResult(true);
        super.addTask(descriptor);
    }

    /**
     * Create a new ProActive Job with the given parameters.  It provides method to get the created task.
     * You can here had the number of nodes you want for your ProActive job.
     *
     * @param numberOfNodesNeeded
     * @param executableClassName
     * @param args the arguments attach to this job.
     */
    public InternalProActiveJob(int numberOfNodesNeeded, String executableClassName,
            Map<String, BigString> args) {
        createTask(executableClassName, args);
        getTask().setNumberOfNodesNeeded(numberOfNodesNeeded);
    }

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
