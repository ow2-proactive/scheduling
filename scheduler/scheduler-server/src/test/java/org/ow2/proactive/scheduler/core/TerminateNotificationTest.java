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
package org.ow2.proactive.scheduler.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification.TerminateTaskException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;


public class TerminateNotificationTest {

    private TerminateNotification terminateNotification;

    @Mock
    private SchedulingService schedulingService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        terminateNotification = new TerminateNotification(schedulingService);
    }

    @Test
    public void testTerminate() throws TerminateTaskException {
        TaskId taskId = TaskIdImpl.createTaskId(new JobIdImpl(666, "readableName"), "task-name", 777L);
        TaskResult taskResult = new TaskResultImpl(taskId, new Throwable());
        terminateNotification.terminate(taskId, taskResult);
        Mockito.verify(schedulingService, Mockito.times(1)).taskTerminatedWithResult(taskId, taskResult);
    }

}
