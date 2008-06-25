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
package org.ow2.proactive.scheduler.task;

import org.ow2.proactive.resourcemanager.common.scripting.GenerationScript;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * This class is a container for Native executable. The actual executable is instanciated on the worker node.
 * @author The ProActive Team
 */
public class NativeExecutableContainer implements ExecutableContainer {

    // actual executable data
    private String command;
    private GenerationScript generated;

    /**
     * Create a new container for a native executable.
     * @param command the command to be executed.
     * @param generated the script that generates the command (can be null).
     */
    public NativeExecutableContainer(String command, GenerationScript generated) {
        this.command = command;
        this.generated = generated;
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#getExecutable()
     */
    public Executable getExecutable() throws ExecutableCreationException {
        return new NativeExecutable(command, generated);
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#init()
     */
    public void init(InternalJob job, InternalTask task) {
        // Nothing to do for now...
    }

}
