/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.internal;

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.newimpl.*;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scripting.Script;


@XmlAccessorType(XmlAccessType.FIELD)
public class InternalScriptTask extends InternalTask {
    public static final TaskLogger logger = TaskLogger.getInstance();

    /**
     * ProActive empty constructor.
     */
    public InternalScriptTask() {
    }

    /**
     * Create a new script task descriptor with the given command line.
     *
     * @param execContainer the Native Executable Container
     */
    public InternalScriptTask(ExecutableContainer execContainer) {
        this.executableContainer = execContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskLauncher createLauncher(InternalJob job, Node node) throws ActiveObjectCreationException,
            NodeException {
        logger.info(getTaskInfo().getTaskId(), "creating script task launcher");
        TaskLauncher launcher = (TaskLauncher) PAActiveObject.newActive(TaskLauncher.class.getName(),
                new Object[] { getDefaultTaskLauncherInitializer(job), new TaskLauncherFactory() {
                    @Override
                    public TaskExecutor createTaskExecutor(TaskContext context, File workingDir) {
                        return new NonForkedTaskExecutor();
                    }
                } }, node);
        setExecuterInformations(new ExecuterInformations(launcher, node));

        return launcher;
    }

    /**
     * {@inheritDoc}
     */
    public boolean handleResultsArguments() {
        return true; // Needed for exported properties
    }

    @Override
    public String display() {
        String nl = System.getProperty("line.separator");
        String answer = super.display();
        Script tscript = ((ForkedScriptExecutableContainer) executableContainer).getScript();
        return answer + nl + "\tScript = " + ((tscript != null) ? tscript.display() : null);
    }

}
