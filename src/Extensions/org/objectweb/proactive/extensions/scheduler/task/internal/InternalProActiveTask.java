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
import org.objectweb.proactive.extensions.scheduler.common.task.executable.Executable;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.ProActiveExecutable;
import org.objectweb.proactive.extensions.scheduler.task.ProActiveTaskLauncher;
import org.objectweb.proactive.extensions.scheduler.task.TaskLauncher;


/**
 * Description of an ProActive java task.
 * See also @see AbstractJavaTaskDescriptor
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 16, 2007
 * @since ProActive 3.9
 */
public class InternalProActiveTask extends InternalAbstractJavaTask {

    /** the java task to launch */
    private ProActiveExecutable task;

    /**
     * ProActive empty constructor
     */
    public InternalProActiveTask() {
    }

    /**
     * Create a new Java ProActive task descriptor using instantiated java task.
     *
     * @param task the already instantiated java task.
     */
    public InternalProActiveTask(ProActiveExecutable task) {
        this.task = task;
    }

    /**
     * Create a new Java ProActive task descriptor using a specific Class.
     *
     * @param taskClass the class instance of the class to instantiate.
     */
    public InternalProActiveTask(Class<ProActiveExecutable> taskClass) {
        super(taskClass);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.task.internal.InternalTask#getTask()
     */
    @Override
    public Executable getTask() throws TaskCreationException {
        // create task from taskClass
        if (task == null) {
            try {
                task = (ProActiveExecutable) taskClass.newInstance();
            } catch (InstantiationException e) {
                throw new TaskCreationException("Cannot create ProActive task from task class ",
                    e);
            } catch (IllegalAccessException e) {
                throw new TaskCreationException("Cannot create ProActive task from task class ",
                    e);
            }
        }

        task.setArgs(args);

        return task;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.task.internal.InternalTask#createLauncher(org.objectweb.proactive.core.node.Node)
     */
    @Override
    public TaskLauncher createLauncher(Node node)
        throws ActiveObjectCreationException, NodeException {
        ProActiveTaskLauncher launcher;

        if (getPreScript() == null) {
            launcher = (ProActiveTaskLauncher) PAActiveObject.newActive(ProActiveTaskLauncher.class.getName(),
                    new Object[] { getId() }, node);
        } else {
            launcher = (ProActiveTaskLauncher) PAActiveObject.newActive(ProActiveTaskLauncher.class.getName(),
                    new Object[] { getId(), getPreScript() }, node);
        }

        setExecuterInformations(new ExecuterInformations(launcher, node));

        return launcher;
    }

    /**
     * Set the instantiated java ProActive task.
     *
     * @param task the instantiated java ProActive task.
     */
    public void setTask(ProActiveExecutable task) {
        this.task = task;
    }

    /**
     * @param numberOfNodesNeeded the numberOfNodesNeeded to set
     */
    public void setNumberOfNodesNeeded(int numberOfNodesNeeded) {
        this.numberOfNodesNeeded = numberOfNodesNeeded;
    }
}
