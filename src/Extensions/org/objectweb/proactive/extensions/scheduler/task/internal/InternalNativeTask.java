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
package org.objectweb.proactive.extensions.scheduler.task.internal;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.scheduler.common.exception.TaskCreationException;
import org.objectweb.proactive.extensions.scheduler.common.scripting.GenerationScript;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.Executable;
import org.objectweb.proactive.extensions.scheduler.task.NativeExecutable;
import org.objectweb.proactive.extensions.scheduler.task.NativeTaskLauncher;
import org.objectweb.proactive.extensions.scheduler.task.TaskLauncher;


/**
 * Description of a native task.
 * This task include the process
 * see also {@link InternalTask}
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jun 29, 2007
 * @since ProActive 3.9
 */
public class InternalNativeTask extends InternalTask {

    /** Serial Version UID */
    private static final long serialVersionUID = 2587936204570926300L;

    /** Command line to execute */
    private String cmd;

    /** Generation Script */
    private GenerationScript gscript;

    /**
     * ProActive empty constructor.
     */
    public InternalNativeTask() {
    }

    /**
     * Create a new native task descriptor with the given command line.
     *
     * @param cmd the command line to execute
     */
    public InternalNativeTask(String cmd, GenerationScript gscript) {
        this.cmd = cmd;
        this.gscript = gscript;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.task.internal.InternalTask#getTask()
     */
    @Override
    public Executable getTask() throws TaskCreationException {
        //create the new task that will launch the command on execute.
        NativeExecutable executableNativeTask = null;

        try {
            executableNativeTask = new NativeExecutable(this.cmd, this.gscript);
        } catch (Exception e) {
            throw new TaskCreationException("Cannot create native task !!", e);
        }

        return executableNativeTask;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.task.internal.InternalTask#createLauncher(java.lang.String, int, org.objectweb.proactive.core.node.Node)
     */
    @Override
    public TaskLauncher createLauncher(String host, int port, Node node)
        throws ActiveObjectCreationException, NodeException {
        NativeTaskLauncher launcher;
        if (getPreScript() == null) {
            launcher = (NativeTaskLauncher) PAActiveObject.newActive(NativeTaskLauncher.class.getName(),
                    new Object[] { getId(), host, port }, node);
        } else {
            launcher = (NativeTaskLauncher) PAActiveObject.newActive(NativeTaskLauncher.class.getName(),
                    new Object[] { getId(), host, port, getPreScript() }, node);
        }

        setExecuterInformations(new ExecuterInformations(launcher, node));

        return launcher;
    }
}
