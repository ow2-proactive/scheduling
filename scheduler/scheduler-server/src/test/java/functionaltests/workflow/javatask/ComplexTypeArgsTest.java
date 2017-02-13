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
package functionaltests.workflow.javatask;

import static org.junit.Assert.assertFalse;

import java.io.Serializable;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

import functionaltests.executables.ComplexParamsExecutable;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;


public class ComplexTypeArgsTest extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testComplexTypeArgs() throws Throwable {
        TaskFlowJob submittedJob = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("t1");
        task.setExecutableClassName(ComplexParamsExecutable.class.getName());
        task.addArgument("param1", new UserTypeA(3));
        submittedJob.addTask(task);

        //test submission and event reception
        JobId id = schedulerHelper.testJobSubmission(submittedJob);

        //check job results
        JobResult res = schedulerHelper.getJobResult(id);

        assertFalse(res.hadException());
    }

    public static class UserTypeA implements Serializable {
        private int value;

        public UserTypeA(int v) {
            this.value = v;
        }

        public int getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return "[UserTypeA : " + this.value + "]";
        }
    }

}
