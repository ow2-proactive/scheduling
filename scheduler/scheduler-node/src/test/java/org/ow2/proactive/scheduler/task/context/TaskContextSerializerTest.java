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
package org.ow2.proactive.scheduler.task.context;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scripting.ForkEnvironmentScript;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


public class TaskContextSerializerTest {

    @Test
    public void testSerializeContextToFile() throws Exception {
        TaskContext taskContext = createTaskContext();
        File serializedContextFile = new TaskContextSerializer().serializeContext(taskContext,
                                                                                  new File(System.getProperty("java.io.tmpdir")));
        serializedContextFile.deleteOnExit();

        assertThat("File is not written to disk, but it should.", serializedContextFile.exists(), is(true));
        assertThat("File must be readable", serializedContextFile.canRead(), is(true));
    }

    @Test(expected = java.io.IOException.class)
    public void testSerializeContextThrowsIoExceptionIfDirectoryIsInvalid()
            throws InvalidScriptException, NodeException, IOException {
        TaskContext taskContext = createTaskContext();
        File invalidDirectory = new File("/-->invalid!!/66/4/32/4/234/");

        new TaskContextSerializer().serializeContext(taskContext, invalidDirectory);
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
