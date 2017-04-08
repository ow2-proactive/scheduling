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

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.core.db.RecoveredSchedulerState;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.policy.DefaultPolicy;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class SchedulingServiceTest {

    private SchedulingService schedulingService;

    @Mock
    private SchedulingInfrastructure infrastructure;

    @Mock
    private SchedulerStateUpdate listener;

    @Mock
    private RecoveredSchedulerState recoveredState;

    @Mock
    private SchedulingMethod schedulingMethod;

    @Mock
    private RMProxiesManager rmProxiesManager;

    private final String policyClassName = DefaultPolicy.class.getName();

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(recoveredState.getPendingJobs()).thenReturn(new Vector<InternalJob>());
        Mockito.when(recoveredState.getRunningJobs()).thenReturn(new Vector<InternalJob>());
        Mockito.when(recoveredState.getFinishedJobs()).thenReturn(new Vector<InternalJob>());

        Mockito.when(infrastructure.getRMProxiesManager()).thenReturn(rmProxiesManager);
        Mockito.when(rmProxiesManager.getRmUrl()).thenReturn(null);

        schedulingService = new SchedulingService(infrastructure,
                                                  listener,
                                                  recoveredState,
                                                  policyClassName,
                                                  schedulingMethod);
    }

    @Test
    public void testConstructorAndStart() {
        assertThat(schedulingService.status, is(schedulingService.status.STARTED));
    }

    @Test
    public void testGetPolicy() {
        assertThat(schedulingService.getPolicy().getClass().getName(), is(policyClassName));
    }

    @Test
    public void testIsSubmitPossible() {
        assertThat(schedulingService.isSubmitPossible(), is(true));
    }

    @Test
    public void testIsNotSubmitPossible() {
        schedulingService.status = schedulingService.status.KILLED;
        assertThat(schedulingService.isSubmitPossible(), is(false));
        schedulingService.status = schedulingService.status.STOPPED;
        assertThat(schedulingService.isSubmitPossible(), is(false));
        schedulingService.status = schedulingService.status.SHUTTING_DOWN;
        assertThat(schedulingService.isSubmitPossible(), is(false));
        schedulingService.status = schedulingService.status.DB_DOWN;
        assertThat(schedulingService.isSubmitPossible(), is(false));
    }

    @Test
    public void testStop() {
        assertThat(schedulingService.stop(), is(true));
        assertThat(schedulingService.status, is(schedulingService.status.STOPPED));
    }

    @Test
    public void testCannotStop() {
        schedulingService.status = schedulingService.status.STOPPED;
        assertThat(schedulingService.stop(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.STOPPED));

        schedulingService.status = schedulingService.status.UNLINKED;
        assertThat(schedulingService.stop(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.UNLINKED));

        schedulingService.status = schedulingService.status.SHUTTING_DOWN;
        assertThat(schedulingService.stop(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.SHUTTING_DOWN));

        schedulingService.status = schedulingService.status.KILLED;
        assertThat(schedulingService.stop(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.KILLED));

        schedulingService.status = schedulingService.status.DB_DOWN;
        assertThat(schedulingService.stop(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.DB_DOWN));
    }

    @Test
    public void testPause() {
        assertThat(schedulingService.pause(), is(true));
        assertThat(schedulingService.status, is(schedulingService.status.PAUSED));

        schedulingService.status = schedulingService.status.FROZEN;
        assertThat(schedulingService.pause(), is(true));
        assertThat(schedulingService.status, is(schedulingService.status.PAUSED));
    }

    @Test
    public void testCannotPause() {
        schedulingService.status = schedulingService.status.UNLINKED;
        assertThat(schedulingService.pause(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.UNLINKED));

        schedulingService.status = schedulingService.status.SHUTTING_DOWN;
        assertThat(schedulingService.pause(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.SHUTTING_DOWN));

        schedulingService.status = schedulingService.status.KILLED;
        assertThat(schedulingService.pause(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.KILLED));

        schedulingService.status = schedulingService.status.DB_DOWN;
        assertThat(schedulingService.pause(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.DB_DOWN));
    }

    @Test
    public void testFreeze() {
        assertThat(schedulingService.freeze(), is(true));
        assertThat(schedulingService.status, is(schedulingService.status.FROZEN));

        schedulingService.status = schedulingService.status.PAUSED;
        assertThat(schedulingService.freeze(), is(true));
        assertThat(schedulingService.status, is(schedulingService.status.FROZEN));
    }

    @Test
    public void testCannotFreeze() {
        schedulingService.status = schedulingService.status.UNLINKED;
        assertThat(schedulingService.freeze(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.UNLINKED));

        schedulingService.status = schedulingService.status.SHUTTING_DOWN;
        assertThat(schedulingService.freeze(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.SHUTTING_DOWN));

        schedulingService.status = schedulingService.status.KILLED;
        assertThat(schedulingService.freeze(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.KILLED));

        schedulingService.status = schedulingService.status.DB_DOWN;
        assertThat(schedulingService.freeze(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.DB_DOWN));
    }

    @Test
    public void testResume() {
        schedulingService.status = schedulingService.status.FROZEN;
        assertThat(schedulingService.resume(), is(true));
        assertThat(schedulingService.status, is(schedulingService.status.STARTED));

        schedulingService.status = schedulingService.status.PAUSED;
        assertThat(schedulingService.resume(), is(true));
        assertThat(schedulingService.status, is(schedulingService.status.STARTED));
    }

    @Test
    public void testCannotResume() {
        schedulingService.status = schedulingService.status.STARTED;
        assertThat(schedulingService.resume(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.STARTED));

        schedulingService.status = schedulingService.status.SHUTTING_DOWN;
        assertThat(schedulingService.resume(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.SHUTTING_DOWN));

        schedulingService.status = schedulingService.status.KILLED;
        assertThat(schedulingService.resume(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.KILLED));

        schedulingService.status = schedulingService.status.DB_DOWN;
        assertThat(schedulingService.resume(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.DB_DOWN));
    }

    @Test
    public void testShutdown() {
        schedulingService.status = schedulingService.status.STARTED;
        assertThat(schedulingService.shutdown(), is(true));
        assertThat(schedulingService.status, is(schedulingService.status.SHUTTING_DOWN));
        Mockito.verify(infrastructure, Mockito.times(1)).schedule(org.mockito.Matchers.any(Runnable.class),
                                                                  org.mockito.Matchers.anyLong());
    }

    @Test
    public void testCannotShutdown() {
        schedulingService.status = schedulingService.status.UNLINKED;
        assertThat(schedulingService.shutdown(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.UNLINKED));

        schedulingService.status = schedulingService.status.SHUTTING_DOWN;
        assertThat(schedulingService.shutdown(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.SHUTTING_DOWN));

        schedulingService.status = schedulingService.status.KILLED;
        assertThat(schedulingService.shutdown(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.KILLED));

        schedulingService.status = schedulingService.status.DB_DOWN;
        assertThat(schedulingService.shutdown(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.DB_DOWN));
    }

    @Test
    public void testKill() {
        schedulingService.status = schedulingService.status.STARTED;
        assertThat(schedulingService.kill(), is(true));
        assertThat(schedulingService.status, is(schedulingService.status.KILLED));
        Mockito.verify(infrastructure, Mockito.times(1)).shutdown();
        Mockito.verify(listener, Mockito.times(1)).schedulerStateUpdated(SchedulerEvent.KILLED);
    }

    @Test
    public void testCannotKill() {
        schedulingService.status = schedulingService.status.KILLED;
        assertThat(schedulingService.kill(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.KILLED));

        schedulingService.status = schedulingService.status.DB_DOWN;
        assertThat(schedulingService.kill(), is(false));
        assertThat(schedulingService.status, is(schedulingService.status.DB_DOWN));
    }

    @Test
    public void testCannotRestartTaskOnNodeFailure() {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        schedulingService.status = schedulingService.status.KILLED;
        InternalTask task = new InternalScriptTask(job);
        schedulingService.restartTaskOnNodeFailure(task);
        Mockito.verify(infrastructure, Mockito.times(0)).getInternalOperationsThreadPool();
    }

    @Test
    public void testRestartTaskOnNodeFailure() {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        InternalTask task = new InternalScriptTask(job);
        ExecutorService executorService = Mockito.mock(ExecutorService.class);
        Mockito.when(infrastructure.getInternalOperationsThreadPool()).thenReturn(executorService);
        schedulingService.restartTaskOnNodeFailure(task);
        Mockito.verify(executorService, Mockito.times(1)).submit(org.mockito.Matchers.any(Runnable.class));
    }

    @Test
    public void testScheduleJobRemoveShouldUseHousekeepingThreadPool() {
        schedulingService.scheduleJobRemove(JobIdImpl.makeJobId("42"), 42);

        Mockito.verify(infrastructure).scheduleHousekeeping(Mockito.any(JobRemoveHandler.class), Mockito.anyLong());
    }

    @Test
    public void testRemoveJobShouldUseClientOperationsThreadPool() {
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        Mockito.when(infrastructure.getClientOperationsThreadPool()).thenReturn(threadPool);
        Mockito.when(infrastructure.getDBManager()).thenReturn(Mockito.mock(SchedulerDBManager.class));

        schedulingService.removeJob(JobIdImpl.makeJobId("43"));
        Mockito.verify(infrastructure).getClientOperationsThreadPool();
    }

}
