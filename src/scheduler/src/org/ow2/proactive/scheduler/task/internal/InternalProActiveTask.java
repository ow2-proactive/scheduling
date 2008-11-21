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
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.ProActiveTaskLauncher;
import org.ow2.proactive.scheduler.task.TaskLauncher;


/**
 * Description of an ProActive java task.
 * See also @see AbstractJavaTaskDescriptor
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class InternalProActiveTask extends InternalTask {

    /**
     * ProActive empty constructor
     */
    public InternalProActiveTask() {
    }

    /**
     * Create a new Java ProActive task descriptor using instantiated java task.
     *
     * @param execContainer contains the task to execute
     */
    public InternalProActiveTask(JavaExecutableContainer execContainer) {
        this.executableContainer = new ExecutableContainerDataBaseProxy(execContainer, this);
    }

    /**
     * @see org.ow2.proactive.scheduler.task.internal.InternalTask#createLauncher(org.objectweb.proactive.core.node.Node)
     */
    @Override
    public TaskLauncher createLauncher(Node node) throws ActiveObjectCreationException, NodeException {
        ProActiveTaskLauncher launcher;

        if (getPreScript() == null && getPostScript() == null) {
            launcher = (ProActiveTaskLauncher) PAActiveObject.newActive(
                    ProActiveTaskLauncher.class.getName(), new Object[] { getId() }, node);
        } else {
            launcher = (ProActiveTaskLauncher) PAActiveObject.newActive(
                    ProActiveTaskLauncher.class.getName(), new Object[] { getId(), getPreScript(),
                            getPostScript() }, node);
        }

        setExecuterInformations(new ExecuterInformations(launcher, node));
        setKillTaskTimer(launcher);

        return launcher;
    }

    /**
     * @param numberOfNodesNeeded the numberOfNodesNeeded to set
     */
    public void setNumberOfNodesNeeded(int numberOfNodesNeeded) {
        this.numberOfNodesNeeded = numberOfNodesNeeded;
    }

}
