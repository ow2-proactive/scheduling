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
package org.ow2.proactive.scheduler.task;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * An executable container allows to instanciate the actual executable in a lazy manner, i.e. 
 * on the worker node that will execute the actual executable.
 * @author The ProActive Team
 */
public interface ExecutableContainer extends Serializable {

    /**
     * Create and return the contained executable
     * @return the contained executable
     * @throws ExecutableCreationException if the executable cannot be created
     */
    public Executable getExecutable() throws ExecutableCreationException;

    /**
     * Generic init method for executable containers.
     * This method is called on SchedulerCore just before sending the container
     * on the node that will execute the contained executable.
     * @param job the job owning the contained executable
     * @param task the task owning the contained executable
     */
    public void init(InternalJob job, InternalTask task);
}
