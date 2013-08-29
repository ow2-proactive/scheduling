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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.jmeter.scheduler;

import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.NativeTask;


/**
 * Test scenario 'Submit simple native task' (it executes basic 'submit job'
 * scenario, see BaseJobSubmitClient for details).
 * <p/>
 * Scenario submits job which contains single native task, native task's script
 * just sleep 5 seconds and finishes.
 * 
 * @author ProActive team
 * 
 */
public class SimpleNativeJobSubmitClient extends BaseJobSubmitClient {

    @Override
    protected TaskFlowJob createJob(String jobName) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setCancelJobOnError(true);
        job.setDescription("Job with one native task (task exits immediately)");

        NativeTask task = new NativeTask();
        task.setCommandLine(new String[] { testsSourcePath +
                "/org/ow2/proactive/tests/performance/jmeter/scheduler/nativeTask.sh"});
        task.setName("Simple native task");
        task.setDescription("Test native task, exits immediately");
        task.setMaxNumberOfExecution(1);
        task.setCancelJobOnError(true);

        job.addTask(task);

        return job;
    }

}
