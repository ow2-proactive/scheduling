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
package org.ow2.proactive.scheduler.descriptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class EligibleTaskDescriptorImplTest {

    /**
     * Test checks that task which has 100K deep kids structure
     * can be successfully serialized.
     * (Hint: before EligibleTaskDescriptorImpl did not have
     * transient keywords for parents/children, which caused Stackoverflow
     * exception during serializaion - because structure was too deep.
     */
    @Test
    public void serializaion() throws IOException {

        EligibleTaskDescriptorImpl root = new EligibleTaskDescriptorImpl(createInternalTask(0));

        EligibleTaskDescriptorImpl latest = root;
        for (int i = 1; i < 100000; ++i) {
            final EligibleTaskDescriptorImpl newTask = new EligibleTaskDescriptorImpl(createInternalTask(i));
            latest.addChild(newTask);
            latest = newTask;
        }

        File tempFile = File.createTempFile("hello", ".tmp");

        try (FileOutputStream file = new FileOutputStream(tempFile);
                ObjectOutputStream out = new ObjectOutputStream(file)) {

            out.writeObject(root);

        }
    }

    private InternalTask createInternalTask(long id) {
        InternalTask task = mock(InternalTask.class);

        JobId jobId = new JobIdImpl(1, "1");
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "1", id);
        when(task.getId()).thenReturn(taskId);
        when(task.getNumberOfNodesNeeded()).thenReturn(5);

        return task;
    }

}
