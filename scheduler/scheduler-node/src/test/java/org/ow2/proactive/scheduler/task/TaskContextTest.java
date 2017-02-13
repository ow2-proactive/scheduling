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
package org.ow2.proactive.scheduler.task;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.NodeDataSpacesURIs;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scripting.ForkEnvironmentScript;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


public class TaskContextTest {

    @Test
    public void nodesURLsAndHostsCanBeRepeated() throws Exception {
        TaskContext context = createTaskContext();

        context.getOtherNodesURLs().add("host1");
        context.getOtherNodesURLs().add("host1");
        assertEquals(2, context.getOtherNodesURLs().size());

        context.getNodesHosts().add("host1");
        context.getNodesHosts().add("host1");
        assertEquals(2, context.getNodesHosts().size());
    }

    private TaskContext createTaskContext() throws NodeException, InvalidScriptException {
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1L, "testSerializeContextToFile"),
                                                                  "testSerializeContextToFile",
                                                                  1L));
        // Mock TaskContext except of the tested method
        // Invoke method to test it
        return new TaskContext(new ScriptExecutableContainer(new TaskScript(new ForkEnvironmentScript(new SimpleScript("",
                                                                                                                       "python")))),
                               taskLauncherInitializer,
                               null,
                               new NodeDataSpacesURIs(null, null, null, null, null, null),
                               null,
                               null);
    }
}
