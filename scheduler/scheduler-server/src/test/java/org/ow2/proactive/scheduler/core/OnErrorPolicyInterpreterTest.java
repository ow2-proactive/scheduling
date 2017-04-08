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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.Mock;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class OnErrorPolicyInterpreterTest {

    @Test
    public void testRequiresPauseTaskOnError() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_TASK);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(true));

    }

    @Test
    public void testRequiresPauseTaskOnErrorNull() {

        Task task = createTask();

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseTaskOnErrorNotset() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.NONE);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseTaskOnErrorCancelJob() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.CANCEL_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseTaskOnErrorPauseJob() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseJobOnError() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(true));

    }

    @Test
    public void testRequiresPauseJobOnErrorNull() {

        Task task = createTask();

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseJobOnErrorNotset() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.NONE);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseJobOnErrorCancelJob() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.CANCEL_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseJobOnErrorPauseTask() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_TASK);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresCancelJobOnError() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.CANCEL_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(true));

    }

    @Test
    public void testRequiresCancelJobOnErrorNull() {

        Task task = createTask();

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresCancelJobOnErrorNotset() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.NONE);

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresCancelJobOnErrorPauseJob() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresCancelJobOnErrorPauseTask() {

        Task task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_TASK);

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testnotSetOrNone() {

        Task task = createTask();

        boolean actual = new OnErrorPolicyInterpreter().notSetOrNone(task);

        assertThat(actual, is(true));

        task.setOnTaskError(OnTaskError.NONE);

        actual = new OnErrorPolicyInterpreter().notSetOrNone(task);

        assertThat(actual, is(true));

    }

    private InternalTask createTask() {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        InternalTask task = new InternalTask(job) {

            @Override
            public boolean handleResultsArguments() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public TaskLauncher createLauncher(Node node) throws ActiveObjectCreationException, NodeException {
                // TODO Auto-generated method stub
                return null;
            }
        };
        return task;
    }

}
