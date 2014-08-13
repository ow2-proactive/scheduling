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

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.tests.performance.scheduler.JobWaitContition;
import org.ow2.proactive.tests.performance.scheduler.SchedulerEventsMonitor;
import org.ow2.proactive.tests.performance.scheduler.SchedulerTestListener;
import org.ow2.proactive.tests.performance.scheduler.SchedulerWaitCondition;


/**
 * Test scenario 'Submit sample jobs'.
 * <p/>
 * This scenario submits multiple jobs using jobs descriptors from the scheduler 
 * samples and waits when jobs finish. It measures total time required to submit all jobs. 
 * Submitted jobs are Job_8_tasks.xml, Job_Aborted.xml, Job_fork.xml, _nativ.xml, 
 * Job_PI.xml, Job_pre_post.xml, Job_with_dep.xml, Job_with_select_script.xml
 * 
 * @author ProActive team
 *
 */
public class SampleJobsSubmitClient extends BaseJMeterSchedulerClient {

    static final long JOB_WAIT_TIMEOUT = 5 * 60000;

    private SchedulerEventsMonitor eventsMonitor;

    private final JobFactory jobFactory = JobFactory.getFactory();

    static final String[] sampleJobDescriptors = { "Job_8_tasks.xml", "Job_Aborted.xml", "Job_fork.xml",
            "Job_nativ.xml", "Job_PI.xml", "Job_pre_post.xml", "Job_with_dep.xml",
            "Job_with_select_script.xml" };

    private List<Job> jobs;

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        eventsMonitor = new SchedulerEventsMonitor();
        SchedulerTestListener listener = SchedulerTestListener.createListener(eventsMonitor);
        getScheduler().addEventListener(listener, true);

        System.setProperty("pa.scheduler.home", schedulingPath);
        synchronized (SampleJobsSubmitClient.class) {
            jobs = new ArrayList<Job>();
            for (String descriptor : sampleJobDescriptors) {
                Job job = jobFactory.createJob(schedulingPath + "/samples/workflows/more/" + descriptor);
                job.setDescription(descriptor);
                jobs.add(job);
            }
        }
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        String baseJobName = generateUniqueJobName();

        List<SchedulerWaitCondition> jobCompleteConditions = new ArrayList<SchedulerWaitCondition>();

        for (Job job : jobs) {
            job.setName(job.getDescription() + "-" + baseJobName);

            JobStatus expectedStatus;
            if (job.getDescription().equals("Job_Aborted.xml")) {
                expectedStatus = JobStatus.CANCELED;
            } else {
                expectedStatus = JobStatus.FINISHED;
            }

            SchedulerWaitCondition jobCompleteCondition = eventsMonitor
                    .addWaitCondition(new JobWaitContition(job.getName(), expectedStatus));
            jobCompleteConditions.add(jobCompleteCondition);
        }

        List<JobId> jobsIds = new ArrayList<JobId>();

        SampleResult result = new SampleResult();
        result.sampleStart();
        for (Job job : jobs) {
            JobId jobId = getScheduler().submit(job);
            logInfo("Submitted job " + jobId + " " + job.getName() + "(" + Thread.currentThread() + ")");

            jobsIds.add(jobId);
        }
        result.sampleEnd();

        boolean ok = true;

        for (int i = 0; i < jobCompleteConditions.size(); i++) {
            SchedulerWaitCondition jobCompleteCondition = jobCompleteConditions.get(i);

            if (!eventsMonitor.waitFor(jobCompleteCondition, JOB_WAIT_TIMEOUT, getLogger())) {
                ok = false;

                JobId jobId = jobsIds.get(i);
                logError("Job execution failed (" + jobId + "), trying to get job result:");
                JobResult jobResult = getScheduler().getJobResult(jobId);
                if (jobResult != null) {
                    logError("Job execution failed (" + jobId + "), job result:");
                    logJobResult(jobResult);
                } else {
                    logError("Job execution failed and job result isn't available, job: " + jobId);
                }
            }
        }
        result.setSuccessful(ok);
        if (!ok) {
            result.setResponseMessage("Some job(s) didn't finish as expected");
        }

        return result;
    }

    @Override
    protected void doTeardownTest(JavaSamplerContext context) throws Exception {
        getScheduler().removeEventListener();

        super.doTeardownTest(context);
    }

}
