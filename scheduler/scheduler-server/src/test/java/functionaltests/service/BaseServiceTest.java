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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.core.SchedulingMethod;
import org.ow2.proactive.scheduler.core.SchedulingService;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.policy.DefaultPolicy;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.tests.ProActiveTest;

import functionaltests.utils.Jobs;


public class BaseServiceTest extends ProActiveTest {

    protected SchedulerDBManager dbManager;

    protected MockSchedulingInfrastructure infrastructure;

    protected MockSchedulingListener listener;

    protected SchedulingService service;

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    @Before
    public void init() throws Exception {
        dbManager = SchedulerDBManager.createInMemorySchedulerDBManager();
        infrastructure = new MockSchedulingInfrastructure(dbManager);
        listener = new MockSchedulingListener();
        service = new SchedulingService(infrastructure,
                                        listener,
                                        null,
                                        DefaultPolicy.class.getName(),
                                        Mockito.mock(SchedulingMethod.class));
    }

    @After
    public void cleanup() {
        service.kill();
    }

    void taskStarted(JobDescriptor jobDesc, EligibleTaskDescriptor taskDesc) throws Exception {
        InternalTask task = taskDesc.getInternal();
        TaskLauncher launcher = Mockito.mock(TaskLauncher.class);
        task.setExecuterInformation(new ExecuterInformation(launcher, NodeFactory.getDefaultNode()));
        service.taskStarted(jobDesc.getInternal(), task, launcher);
    }

    public static InternalJob createJob(TaskFlowJob job) throws Exception {
        return Jobs.createJob(job);
    }

    interface TestRunnable {
        void run() throws Exception;
    }

    static void runInAnotherThread(final TestRunnable runnable) throws Exception {
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    Assert.fail("Unexpected exception: " + t);
                    t.printStackTrace(System.out);
                }
            }
        }).get();
    }

    static void runInAnotherThreadNoWait(final TestRunnable runnable) throws Exception {
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    Assert.fail("Unexpected exception: " + t);
                    t.printStackTrace(System.out);
                }
            }
        });
    }
}
