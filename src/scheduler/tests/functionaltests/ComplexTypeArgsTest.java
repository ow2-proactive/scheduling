/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

import functionalTests.FunctionalTest;
import functionaltests.executables.ComplexParamsExecutable;


public class ComplexTypeArgsTest extends FunctionalTest {

    @org.junit.Test
    public void run() throws Throwable {

        SchedulerTHelper.startScheduler();

        //creating job
        TaskFlowJob submittedJob = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("t1");
        task.setExecutableClassName(ComplexParamsExecutable.class.getName());
        task.addArgument("param1", new UserTypeA(3));
        submittedJob.addTask(task);

        JobEnvironment env = new JobEnvironment();
        env.setJobClasspath(new String[] { System.getProperty("pa.scheduler.home") +
            "/classes/schedulerTests/" });
        submittedJob.setEnvironment(env);

        //test submission and event reception
        JobId id = SchedulerTHelper.testJobSubmission(submittedJob);

        //check job results
        JobResult res = SchedulerTHelper.getJobResult(id);

        Thread.sleep(5000);

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
