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
package org.ow2.proactive.scheduler.task.internal;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeTaskLauncher;
import org.ow2.proactive.scheduler.task.TaskLauncher;


/**
 * Description of a native task.
 * This task include the process
 * see also {@link InternalTask}
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 */
public class InternalNativeTask extends InternalTask {

    /**
     * ProActive empty constructor.
     */
    public InternalNativeTask() {
    }

    /**
     * Create a new native task descriptor with the given command line.
     *
     * @param execContainer the Native Executable Container
     */
    public InternalNativeTask(NativeExecutableContainer execContainer) {
        this.executableContainer = execContainer;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.internal.InternalTask#createLauncher(org.objectweb.proactive.core.node.Node)
     */
    @Override
    public TaskLauncher createLauncher(Node node) throws ActiveObjectCreationException, NodeException {
        NativeTaskLauncher launcher;
        if (getPreScript() == null) {
            launcher = (NativeTaskLauncher) PAActiveObject.newActive(NativeTaskLauncher.class.getName(),
                    new Object[] { getId() }, node);
        } else {
            launcher = (NativeTaskLauncher) PAActiveObject.newActive(NativeTaskLauncher.class.getName(),
                    new Object[] { getId(), getPreScript() }, node);
        }

        setExecuterInformations(new ExecuterInformations(launcher, node));
        setKillTaskTimer(launcher);

        return launcher;
    }

}
