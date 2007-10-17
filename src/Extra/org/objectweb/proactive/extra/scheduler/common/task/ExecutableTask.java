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
package org.objectweb.proactive.extra.scheduler.common.task;

import java.io.Serializable;
import java.util.Map;


/**
 * This is the main entry point of a task execution.
 * Each executable java and native task may have to implements this method.
 * Only the application job will implement its own execute.
 * In this last case, the execute(TaskResult...) method will be forget.
 *
 * @author ProActive Team
 * @version 1.0, Aug 24, 2007
 * @since ProActive 3.2
 */
public abstract class ExecutableTask implements Serializable {

    /** Arguments of the task as a map */
    private Map<String, Object> args = null;

    /**
     * The content of this method will be executed once or more if asked.
     * This may generate an Object result. It can be whatever you want.
     *
     * @param results the results (as a taskResult) from parent tasks.
     * @throws any exception thrown by the user's code
     * @return any object from the user.
     */
    public abstract Object execute(TaskResult... results)
        throws Throwable;

    /**
     * Initialize the task with the arguments given to the constructor.
     * If args is not null, then the {@link #init(Map)} method is called.
     * If args is null, the {@link #init(Map)} method is not called.
     *
     * @throws Exception
     */
    public final void init() throws Exception {
        if (args != null) {
            init(args);
        }
    }

    /**
     * Initialize the task with the given arguments.
     * By default this method do nothing.
     * You can override this method to make your own initialization.
     *
     * @param args the arguments as a map.
     * @throws Exception an exception that the user can throw if something goes wrong.
     */
    public abstract void init(Map<String, Object> args)
        throws Exception;

    /**
     * Set the arguments map to this Executable object.
     *
     * @param args the arguments to set.
     */
    public final void setArgs(Map<String, Object> args) {
        if ((args != null) && (args.size() > 0)) {
            this.args = args;
        }
    }

    /**
     * Get the arguments map of this Executable object.
     *
     * @return the arguments map of this Executable object.
     */
    public final Map<String, Object> getArgs() {
        return args;
    }
}
