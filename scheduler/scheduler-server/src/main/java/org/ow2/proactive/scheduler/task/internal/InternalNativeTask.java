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

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.nativ.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.nativ.NativeTaskLauncher;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scripting.Script;


/**
 * Description of a native task.
 * This task include the process
 * see also {@link InternalTask}
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlRootElement(name = "task")
@XmlAccessorType(XmlAccessType.FIELD)
public class InternalNativeTask extends InternalTask {
    public static final TaskLogger logger = TaskLogger.getInstance();

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
     * {@inheritDoc}
     */
    @Override
    public TaskLauncher createLauncher(InternalJob job, Node node) throws ActiveObjectCreationException,
            NodeException {
        NativeTaskLauncher launcher;

        logger.info(getTaskInfo().getTaskId(), "creating native task launcher");
        TaskLauncherInitializer tli = getDefaultTaskLauncherInitializer(job);
        launcher = (NativeTaskLauncher) PAActiveObject.newActive(NativeTaskLauncher.class.getName(),
                new Object[] { tli }, node);

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
        Script gscript = ((NativeExecutableContainer) executableContainer).getGenerationScript();
        return answer + nl + "\tCommandLine = " +
            Arrays.toString(((NativeExecutableContainer) executableContainer).getCommand()) + nl +
            "\tGenerationScript = " + ((gscript != null) ? gscript.display() : null) + nl +
            "\tWorkingDir = '" + ((NativeExecutableContainer) executableContainer).getWorkingDir() + '\'';
    }

}
