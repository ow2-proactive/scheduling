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

import java.util.Map;

import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.core.SchedulerCore;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.classloading.TaskClassLoader;
import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;


/**
 * This class is a container for Java executable. The actual executable is instanciated on the worker node
 * in a dedicated classloader, which can download classes from the associated classServer.
 * @see TaskClassServer
 * @author The ProActive Team
 */
public class JavaExecutableContainer implements ExecutableContainer {

    private String userExecutableClassName;
    private Map<String, String> args;

    // lazy instanciated
    private JavaExecutable userExecutable;
    // can be null
    private TaskClassServer classServer;

    /**
     * Create a new container for JavaExecutable
     * @param userExecutableClassName the classname of the user defined executable
     * @param args the arguments for Executable.init() method.
     */
    public JavaExecutableContainer(String userExecutableClassName, Map<String, String> args) {
        this.userExecutableClassName = userExecutableClassName;
        this.args = args;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#getExecutable()
     */
    public Executable getExecutable() throws ExecutableCreationException {
        if (this.userExecutable == null) {
            // Instanciate the actual executable
            try {
                TaskClassLoader tcl = new TaskClassLoader(ClassLoader.getSystemClassLoader(),
                    this.classServer);
                Class userExecutableClass = tcl.loadClass(this.userExecutableClassName);
                userExecutable = (JavaExecutable) userExecutableClass.newInstance();
                userExecutable.setArgs(this.args);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new ExecutableCreationException("Unable to instanciate JavaExecutable : " + e);
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new ExecutableCreationException("Unable to instanciate JavaExecutable : " + e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new ExecutableCreationException("Unable to instanciate JavaExecutable : " + e);
            }
        }
        return userExecutable;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#init(org.ow2.proactive.scheduler.job.InternalJob, org.ow2.proactive.scheduler.task.internal.InternalTask)
     */
    public void init(InternalJob job, InternalTask task) {
        // get the classserver if any (can be null)
        this.classServer = SchedulerCore.getTaskClassServer(job.getId());

    }

}
