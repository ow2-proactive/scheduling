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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.jmx.mbean.RuntimeDataMBeanImpl;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.tests.ProActiveTest;
import org.python.google.common.collect.Lists;


public class SchedulerFrontendStateTest extends ProActiveTest {

    @Test // SCHEDULING-2242
    public void session_removal_should_not_throw_concurrent_modification_exception() throws Exception {
        PASchedulerProperties.SCHEDULER_USER_SESSION_TIME.updateProperty("1");

        SchedulerJMXHelper mockJMX = mock(SchedulerJMXHelper.class);
        when(mockJMX.getSchedulerRuntimeMBean()).thenReturn(new RuntimeDataMBeanImpl(null));

        final SchedulerFrontendState schedulerFrontendState = new SchedulerFrontendState(new SchedulerStateImpl(),
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
        }
    }

    @Test
    public void getIdentifiedJobTest() throws Exception {

        SchedulerJMXHelper mockJMX = mock(SchedulerJMXHelper.class);
        when(mockJMX.getSchedulerRuntimeMBean()).thenReturn(new RuntimeDataMBeanImpl(null));

        SchedulerStateImpl schedulerStateImpl = new SchedulerStateImpl();

        JobIdImpl jobId = new JobIdImpl(1234L, "job name");

        JobState jobState = mock(JobState.class);

        when(jobState.getId()).thenReturn(jobId);

        schedulerStateImpl.setFinishedJobs(new Vector(Lists.newArrayList(jobState)));

        final SchedulerFrontendState schedulerFrontendState = new SchedulerFrontendState(schedulerStateImpl, mockJMX);

        assertEquals(schedulerFrontendState.getIdentifiedJob(jobId).getJobId(), (jobId));

    }
}
