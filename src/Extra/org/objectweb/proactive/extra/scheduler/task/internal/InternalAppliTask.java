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
package org.objectweb.proactive.extra.scheduler.task.internal;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.scheduler.common.exception.TaskCreationException;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableApplicationTask;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableTask;
import org.objectweb.proactive.extra.scheduler.task.AppliTaskLauncher;
import org.objectweb.proactive.extra.scheduler.task.TaskLauncher;


/**
 * Description of an application java task.
 * See also @see AbstractJavaTaskDescriptor
 *
 * @author ProActive Team
 * @version 1.0, Jul 16, 2007
 * @since ProActive 3.2
 */
public class InternalAppliTask extends InternalAbstractJavaTask {

    /** Serial Version UID */
    private static final long serialVersionUID = -6946803819032140410L;

    /** the java task to launch */
    private ExecutableApplicationTask task;

    /**
     * ProActive empty constructor
     */
    public InternalAppliTask() {
    }

    /**
     * Create a new Java application task descriptor using instantiated java task.
     *
     * @param task the already instantiated java task.
     */
    public InternalAppliTask(ExecutableApplicationTask task) {
        this.task = task;
    }

    /**
     * Create a new Java application task descriptor using a specific Class.
     *
     * @param taskClass the class instance of the class to instantiate.
     */
    public InternalAppliTask(Class<ExecutableApplicationTask> taskClass) {
        super(taskClass);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.task.internal.InternalTask#getTask()
     */
    @Override
    public ExecutableTask getTask() throws TaskCreationException {
        // create task from taskClass
        if (task == null) {
            try {
                task = (ExecutableApplicationTask) taskClass.newInstance();
            } catch (InstantiationException e) {
                throw new TaskCreationException("Cannot create applitask from task class ",
                    e);
            } catch (IllegalAccessException e) {
                throw new TaskCreationException("Cannot create applitask from task class ",
                    e);
            }
        }

        // init task
        try {
            task.init(args);
        } catch (Exception e) {
            throw new TaskCreationException("Cannot initialize task ", e);
        }
        return task;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.task.internal.InternalTask#createLauncher(java.lang.String, int, org.objectweb.proactive.core.node.Node)
     */
    @Override
    public TaskLauncher createLauncher(String host, int port, Node node)
        throws ActiveObjectCreationException, NodeException {
        AppliTaskLauncher launcher;
        if (getPreTask() == null) {
            launcher = (AppliTaskLauncher) ProActiveObject.newActive(AppliTaskLauncher.class.getName(),
                    new Object[] { getId(), getJobId(), host, port }, node);
        } else {
            launcher = (AppliTaskLauncher) ProActiveObject.newActive(AppliTaskLauncher.class.getName(),
                    new Object[] { getId(), getJobId(), getPreTask(), host, port },
                    node);
        }
        setExecuterInformations(new ExecuterInformations(launcher, node));
        return launcher;
    }

    /**
     * Set the instantiated java application task.
     *
     * @param task the instantiated java application task.
     */
    public void setTask(ExecutableApplicationTask task) {
        this.task = task;
    }

    /**
     * @param numberOfNodesNeeded the numberOfNodesNeeded to set
     */
    public void setNumberOfNodesNeeded(int numberOfNodesNeeded) {
        this.numberOfNodesNeeded = numberOfNodesNeeded;
    }
}
