/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.task.internal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.ProActiveForkedTaskLauncherFactory;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;


@XmlAccessorType(XmlAccessType.FIELD)
public class InternalForkedScriptTask extends InternalScriptTask {

    /**
     * ProActive empty constructor.
     */
    public InternalForkedScriptTask(InternalJob internalJob) {
        super(internalJob);
    }

    /**
     * Create a new forked script task descriptor with the given command line.
     *
     * @param execContainer the Native Executable Container
     */
    public InternalForkedScriptTask(ExecutableContainer execContainer, InternalJob internalJob) {
        super(internalJob);
        this.executableContainer = execContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskLauncher createLauncher(Node node) throws ActiveObjectCreationException, NodeException {
        logger.info(getTaskInfo().getTaskId(), "creating forked task launcher");
        TaskLauncher launcher = (TaskLauncher) PAActiveObject.newActive(TaskLauncher.class.getName(),
                                                                        new Object[] { getDefaultTaskLauncherInitializer(),
                                                                                       new ProActiveForkedTaskLauncherFactory() },
                                                                        node);
        // wait until the task launcher is active
        launcher.isActivated();
        setExecuterInformation(new ExecuterInformation(launcher, node));

        return launcher;
    }

}
