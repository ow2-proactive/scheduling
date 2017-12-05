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

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * @author ActiveEon Team
 * @since 25/09/17
 */
public class TaskLauncherTestAbstract {

    @InjectMocks
    protected TaskLauncher taskLauncher;

    @Mock
    protected TaskLauncherRebinder taskLauncherRebinder;

    protected TaskTerminateNotificationVerifier taskResult;

    @Before
    public void setup() {
        taskResult = new TaskTerminateNotificationVerifier();
    }

    /**
     * Creates a task launcher for testing purposes.
     * <p>
     * The task launcher that is created is not an active object
     * but it is initialized by using the defined initActive method.
     *
     * @param initializer the task initializer to use.
     * @param factory the task launcher factory to use.
     *
     * @return a non-active task launcher instance ready to be used with tests.
     */
    protected TaskLauncher createLauncherWithInjectedMocks(TaskLauncherInitializer initializer,
            TaskLauncherFactory factory) {
        taskLauncher = new TaskLauncher(initializer, factory);
        taskLauncher.initActivity(null);
        injectTaskTerminateNotificationMock();
        return taskLauncher;
    }

    private void injectTaskTerminateNotificationMock() {
        MockitoAnnotations.initMocks(this);
        when(taskLauncherRebinder.makeSureSchedulerIsConnected(taskResult)).thenReturn(taskResult);
        when(taskLauncherRebinder.getRebindedTaskTerminateNotificationHandler()).thenReturn(taskResult);
    }

    protected static class TaskTerminateNotificationVerifier implements TaskTerminateNotification {
        protected TaskResult result;

        @Override
        public void terminate(TaskId taskId, TaskResult taskResult) {
            this.result = taskResult;
        }
    }

}
