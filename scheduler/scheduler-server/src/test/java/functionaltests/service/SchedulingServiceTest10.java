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
package functionaltests.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.core.SchedulingMethod;
import org.ow2.proactive.scheduler.core.SchedulingService;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.policy.DefaultPolicy;
import org.ow2.tests.ProActiveTest;


/**
 * Test trying to catch SCHEDULING-1775.
 *
 */
public class SchedulingServiceTest10 extends ProActiveTest {

    private TaskFlowJob createTestJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        JavaTask task1 = new JavaTask();
        task1.setExecutableClassName("class");
        task1.setName("task1");
        job.addTask(task1);
        return job;
    }

    private SchedulingService service;

    @Before
    public void init() throws Exception {
        // default test infrastructure executes all requests in the same thread, for this test real thread pool is needed
        SchedulerDBManager dbManager = SchedulerDBManager.createInMemorySchedulerDBManager();
        MockSchedulingInfrastructure infrastructure = new MockSchedulingInfrastructure(dbManager,
                                                                                       Executors.newFixedThreadPool(1));
        MockSchedulingListener listener = new MockSchedulingListener();
        service = new SchedulingService(infrastructure,
                                        listener,
                                        null,
                                        DefaultPolicy.class.getName(),
                                        Mockito.mock(SchedulingMethod.class));
    }

    @After
    public void clean() throws Exception {
        service.shutdown();
    }

    @Test
    public void testLockJobsToSchedule() throws Exception {
        Map<JobId, JobDescriptor> jobsMap;

        service.submitJob(BaseServiceTest.createJob(createTestJob()));
        service.submitJob(BaseServiceTest.createJob(createTestJob()));
        jobsMap = service.lockJobsToSchedule();
        Assert.assertEquals(2, jobsMap.size());
        List<EligibleTaskDescriptor> tasks = new ArrayList<>(jobsMap.entrySet()
                                                                    .iterator()
                                                                    .next()
                                                                    .getValue()
                                                                    .getEligibleTasks());
        service.simulateJobStartAndCancelIt(tasks, "");
        service.unlockJobsToSchedule(jobsMap.values());

        // wait when request started by the 'simulateJobStartAndCancelIt' finishes
        service.getInfrastructure().getInternalOperationsThreadPool().shutdown();
        boolean terminated = service.getInfrastructure()
                                    .getInternalOperationsThreadPool()
                                    .awaitTermination(1, TimeUnit.MINUTES);
        Assert.assertTrue(terminated);

        jobsMap = service.lockJobsToSchedule();
        Assert.assertEquals(1, jobsMap.size());
    }

}
