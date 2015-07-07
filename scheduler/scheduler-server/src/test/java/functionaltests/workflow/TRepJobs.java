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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.workflow;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import functionaltests.SchedulerConsecutive;
import functionaltests.SchedulerTHelper;
import org.junit.Assert;


/**
 * Test the correctness of workflow jobs containing replicate actions
 * 
 * 
 * @author mschnoor
 *
 */
public class TRepJobs extends SchedulerConsecutive {

    static class TRepCase {
        String jobPath;

        // total number of tasks
        public int total;

        // for each task name in the job descriptor, number
        // of actual tasks sharing the same base name when the job finishes
        public Map<String, Long> tasks;

        // for each task name in the job descriptor, sum
        // of the results of all tasks sharing the same base
        // name when the job finishes
        public Map<String, Long> results;

        /**
         * @param jobPath path to the XML job file to submit
         * @param total total number of tasks 
         * @param t for all task at submission, number of actual tasks sharing the same
         * 	base name when finished, and sum of all results
         *  ie: "T1,1,3 T2,2,5 T3,1,2 ..."
         */
        TRepCase(String jobPath, int total, String t) {
            this.jobPath = jobPath;
            this.total = total;
            readArg(t);
        }

        private void readArg(String param) {
            String[] arr = param.split(" ");

            this.tasks = new HashMap<>(arr.length);
            this.results = new HashMap<>(arr.length);

            for (String str : arr) {
                String[] split = str.split(",");
                String key = split[0];
                long val = Long.parseLong(split[1]);
                long val2 = Long.parseLong(split[2]);
                tasks.put(key, val);
                results.put(key, val2);
            }
        }
    }

    void testJobs(TRepCase... testCases) throws Throwable {
        SchedulerTHelper.startScheduler(new File(SchedulerTHelper.class.getResource(
          "config/scheduler-nonforkedscripttasks.ini").toURI()).getAbsolutePath());
        for (TRepCase tcase : testCases) {
            String path = new File(TWorkflowJobs.class.getResource(tcase.jobPath).toURI()).getAbsolutePath();
            Job job = JobFactory.getFactory().createJob(path);
            JobId id = SchedulerTHelper.submitJob(job);
            SchedulerTHelper.log("Job submitted, id " + id.toString());

            JobState receivedstate = SchedulerTHelper.waitForEventJobSubmitted(id);
            Assert.assertEquals(id, receivedstate.getId());
            JobInfo jInfo = SchedulerTHelper.waitForEventJobRunning(id);
            Assert.assertEquals(jInfo.getJobId(), id);
            Assert.assertEquals(JobStatus.RUNNING, jInfo.getStatus());
            jInfo = SchedulerTHelper.waitForEventJobFinished(id);
            Assert.assertEquals(JobStatus.FINISHED, jInfo.getStatus());
            SchedulerTHelper.log("Job finished");

            JobResult res = SchedulerTHelper.getJobResult(id);
            Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());

            JobState js = SchedulerTHelper.getSchedulerInterface().getJobState(id);

            // final number of tasks
            Assert.assertEquals(tcase.total, js.getTasks().size());

            // to be checked against this.tasks
            HashMap<String, Long> finalTaskCount = new HashMap<>();
            // to be checked against this.results
            HashMap<String, Long> finalResSum = new HashMap<>();
            for (TaskState ts : js.getTasks()) {
                String baseName = InternalTask.getInitialName(ts.getName());
                long count = 0;
                long sum = 0;
                if (finalTaskCount.containsKey(baseName)) {
                    count = finalTaskCount.get(baseName);
                    sum = finalResSum.get(baseName);
                }
                finalTaskCount.put(baseName, count + 1);

                long tr = 0;
                if (ts.getStatus().equals(TaskStatus.SKIPPED)) {
                    tr = -1;
                } else {
                    Serializable sr = res.getAllResults().get(ts.getName()).value();
                    if (sr instanceof Long) {
                        tr = ((Long) sr).longValue();
                    }
                }
                finalResSum.put(baseName, sum + tr);
            }

            Assert.assertEquals(tcase.tasks.size(), finalTaskCount.size());
            Assert.assertEquals(tcase.results.size(), finalResSum.size());
            Assert.assertEquals(finalTaskCount.size(), finalResSum.size());

            for (Entry<String, Long> entry : finalTaskCount.entrySet()) {
                Assert.assertTrue(tcase.tasks.containsKey(entry.getKey()));
                long val = tcase.tasks.get(entry.getKey());
                Assert.assertEquals(val, entry.getValue().longValue());
            }

            for (Entry<String, Long> entry : finalResSum.entrySet()) {
                Assert.assertTrue(tcase.results.containsKey(entry.getKey()));
                long val = tcase.results.get(entry.getKey());
                Assert.assertEquals(val, entry.getValue().longValue());
            }

            SchedulerTHelper.removeJob(id);
            SchedulerTHelper.waitForEventJobRemoved(id);
        }

    }
}
