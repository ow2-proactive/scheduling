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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.policy.DefaultPolicy;


public class JobRemoveHandlerTest {

    private JobRemoveHandler jobRemoveHandler;

    private SchedulingService service;

    @Mock
    private SchedulingInfrastructure infrastructure;

    @Mock
    private SchedulerStateUpdate listener;

    @Mock
    private SchedulingMethod schedulingMethod;

    @Mock
    private SchedulerDBManager dbManager;

    @Mock
    private RMProxiesManager rmProxiesManager;

    @Mock
    private InternalJob job;

    @Mock
    private JobInfoImpl jobInfo;

    private JobId jobId;

    private String policyClassName = DefaultPolicy.class.getName();

    private long id = 666L;

    private String jobName = this.getClass().getName();

    @Before
    public void init() throws Exception {
        jobId = new JobIdImpl(id, jobName);
        MockitoAnnotations.initMocks(this);
        Mockito.when(infrastructure.getDBManager()).thenReturn(dbManager);
        Mockito.when(infrastructure.getRMProxiesManager()).thenReturn(rmProxiesManager);
        Mockito.when(rmProxiesManager.getRmUrl()).thenReturn(null);
        Mockito.when(dbManager.loadJobWithTasksIfNotRemoved(jobId)).thenReturn(job);
        Mockito.when(job.getJobInfo()).thenReturn(jobInfo);
        service = new SchedulingService(infrastructure, listener, null, policyClassName, schedulingMethod);
        jobRemoveHandler = new JobRemoveHandler(service, jobId);
    }

    @Test
    public void testJobRemovedAndStatusUpdated() {
        boolean removed = jobRemoveHandler.call();
        assertThat(removed, is(true));
        Mockito.verify(dbManager, Mockito.times(1)).loadJobWithTasksIfNotRemoved(jobId);
        Mockito.verify(dbManager, Mockito.times(1)).removeJob(org.mockito.Matchers.any(JobId.class),
                                                              org.mockito.Matchers.anyLong(),
                                                              org.mockito.Matchers.anyBoolean());
        Mockito.verify(listener, Mockito.times(1)).jobStateUpdated(org.mockito.Matchers.anyString(),
                                                                   org.mockito.Matchers.any(NotificationData.class));
    }

    @Test
    public void testJobAlreadyRemoved() {
        Mockito.when(dbManager.loadJobWithTasksIfNotRemoved(jobId)).thenReturn(null);
        boolean removed = jobRemoveHandler.call();
        assertThat(removed, is(false));
    }

}
