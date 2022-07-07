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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.utils.NodeSet;


public class CreateExecutionInfo implements Future<Boolean> {

    private Future<Boolean> executionCreated;

    private InternalJob job;

    private InternalTask task;

    private TaskDescriptor taskDescriptor;

    private NodeSet nodes;

    public CreateExecutionInfo(Future<Boolean> executionCreated, InternalJob job, InternalTask task,
            TaskDescriptor taskDescriptor, NodeSet nodes) {
        this.executionCreated = executionCreated;
        this.job = job;
        this.task = task;
        this.taskDescriptor = taskDescriptor;
        this.nodes = nodes;
    }

    public Future<Boolean> getExecutionCreated() {
        return executionCreated;
    }

    public InternalJob getJob() {
        return job;
    }

    public InternalTask getTask() {
        return task;
    }

    public TaskDescriptor getTaskDescriptor() {
        return taskDescriptor;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return executionCreated.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return executionCreated.isCancelled();
    }

    @Override
    public boolean isDone() {
        return executionCreated.isDone();
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        return executionCreated.get();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executionCreated.get(timeout, unit);
    }

    public NodeSet getNodes() {
        return nodes;
    }

}
