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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ow2.proactive.scheduler.core.properties.PASchedulerProperties.SCHEDULER_FINISHED_JOBS_LRU_CACHE_SIZE;

import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.objectweb.proactive.core.UniqueID;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.jmx.mbean.RuntimeDataMBeanImpl;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.IdentifiedJob;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.tests.ProActiveTestClean;
import org.python.google.common.collect.Lists;


public class SchedulerFrontendStateTest extends ProActiveTestClean {

    @Test // SCHEDULING-2242
    public void session_removal_should_not_throw_concurrent_modification_exception() throws Exception {
        PASchedulerProperties.SCHEDULER_USER_SESSION_TIME.updateProperty("1");

        SchedulerJMXHelper mockJMX = mock(SchedulerJMXHelper.class);
        when(mockJMX.getSchedulerRuntimeMBean()).thenReturn(new RuntimeDataMBeanImpl(null));

        final SchedulerFrontendState schedulerFrontendState = new SchedulerFrontendState(new SchedulerStateImpl<ClientJobState>(),
                                                                                         mockJMX);

        // create a bunch of active sessions, they will be removed in 1s
        for (int i = 0; i < 100; i++) {
            UserIdentificationImpl identification = new UserIdentificationImpl("john");
            identification.setHostName("localhost");
            schedulerFrontendState.connect(new UniqueID("abc" + i), identification, null);
        }

        // use the FrontendState continuously to query the active sessions
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Object> noException = executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (;;) {
                    schedulerFrontendState.getUsers();
                }
            }
        });

        try {
            noException.get(2, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            fail("Should exit with timeout exception " + e.getMessage());
        } catch (TimeoutException e) {
            // expected timeout exception after two seconds
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void getIdentifiedJobTest() throws Exception {

        SchedulerJMXHelper mockJMX = mock(SchedulerJMXHelper.class);
        when(mockJMX.getSchedulerRuntimeMBean()).thenReturn(new RuntimeDataMBeanImpl(null));

        SchedulerStateImpl<ClientJobState> schedulerStateImpl = new SchedulerStateImpl<>();

        JobIdImpl jobId = new JobIdImpl(1234L, "job name");

        ClientJobState jobState = mock(ClientJobState.class);

        when(jobState.getId()).thenReturn(jobId);

        schedulerStateImpl.setFinishedJobs(new Vector(Lists.newArrayList(jobState)));

        final SchedulerFrontendState schedulerFrontendState = new SchedulerFrontendState(schedulerStateImpl, mockJMX);

        assertEquals(schedulerFrontendState.getIdentifiedJob(jobId).getJobId(), (jobId));

    }

    ClientJobState createClientJobState(long id) {
        JobIdImpl jobId = new JobIdImpl(id, "job name" + id);
        ClientJobState jobState = mock(ClientJobState.class);
        when(jobState.getId()).thenReturn(jobId);
        return jobState;
    }

    @Test
    public void testLRUCache() throws Exception {
        SchedulerJMXHelper mockJMX = mock(SchedulerJMXHelper.class);
        when(mockJMX.getSchedulerRuntimeMBean()).thenReturn(new RuntimeDataMBeanImpl(null));

        SchedulerStateImpl<ClientJobState> schedulerStateImpl = new SchedulerStateImpl<>();

        final ClientJobState clientJobState0 = createClientJobState(10l);
        final ClientJobState clientJobState01 = createClientJobState(11l);
        final ClientJobState clientJobState1 = createClientJobState(1l);
        final ClientJobState clientJobState2 = createClientJobState(2l);

        schedulerStateImpl.setFinishedJobs(new Vector());
        schedulerStateImpl.setRunningJobs(new Vector(Lists.newArrayList(clientJobState1)));
        schedulerStateImpl.setPendingJobs(new Vector(Lists.newArrayList(clientJobState2)));
        SCHEDULER_FINISHED_JOBS_LRU_CACHE_SIZE.updateProperty("1");

        SchedulerDBManager dbManager = mock(SchedulerDBManager.class);
        SchedulerFrontendState schedulerFrontendState = new SchedulerFrontendState(schedulerStateImpl,
                                                                                   mockJMX,
                                                                                   dbManager);

        InternalJob internalJob = spy(new InternalTaskFlowJob());
        JobInfoImpl jobInfo = mock(JobInfoImpl.class);
        doReturn(jobInfo).when(internalJob).getJobInfo();
        doReturn(clientJobState0.getId()).when(jobInfo).getJobId();
        doReturn(clientJobState0.getId()).when(internalJob).getId();
        doReturn(Collections.singletonList(internalJob)).when(dbManager).loadInternalJob(10l);

        InternalJob internalJob1 = spy(new InternalTaskFlowJob());
        JobInfoImpl jobInfo1 = mock(JobInfoImpl.class);
        doReturn(jobInfo1).when(internalJob1).getJobInfo();
        doReturn(clientJobState01.getId()).when(jobInfo1).getJobId();
        doReturn(clientJobState01.getId()).when(internalJob1).getId();
        doReturn(Collections.singletonList(internalJob1)).when(dbManager).loadInternalJob(11l);

        assertEquals(schedulerFrontendState.getIdentifiedJob(clientJobState0.getId()).getJobId(),
                     clientJobState0.getId());
        assertEquals(schedulerFrontendState.getIdentifiedJob(clientJobState0.getId()).getJobId(),
                     clientJobState0.getId());

        assertEquals(schedulerFrontendState.getIdentifiedJob(clientJobState01.getId()).getJobId(),
                     clientJobState01.getId());
        assertEquals(schedulerFrontendState.getIdentifiedJob(clientJobState01.getId()).getJobId(),
                     clientJobState01.getId());
        assertEquals(schedulerFrontendState.getIdentifiedJob(clientJobState01.getId()).getJobId(),
                     clientJobState01.getId());

        assertEquals(schedulerFrontendState.getIdentifiedJob(clientJobState0.getId()).getJobId(),
                     clientJobState0.getId());
        assertEquals(schedulerFrontendState.getIdentifiedJob(clientJobState0.getId()).getJobId(),
                     clientJobState0.getId());

        verify(dbManager, times(3)).loadInternalJob(anyLong());
    }
}
