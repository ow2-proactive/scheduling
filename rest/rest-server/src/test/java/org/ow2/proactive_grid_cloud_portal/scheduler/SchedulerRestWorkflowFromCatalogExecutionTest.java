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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;


public class SchedulerRestWorkflowFromCatalogExecutionTest extends RestTestServer {

    private SchedulerStateRest schedulerRest;

    private SchedulerProxyUserInterface scheduler;

    private String sessionId;

    private long currentJobId;

    @BeforeClass
    public static void setUpWorkflowsServer() throws Exception {
        // This resource will expose workflow contents so that they can
        // be downloaded and used for the tests.
        addResource(new WorkflowServerHelper());
    }

    @Before
    public void setUp() throws Exception {
        schedulerRest = new SchedulerStateRest();
        scheduler = mock(SchedulerProxyUserInterface.class);
        sessionId = SharedSessionStoreTestUtils.createValidSession(scheduler);
        currentJobId = 55L;
    }

    @Test
    public void testWhenSubmittingUsingAValidWfContentUrlThenAJobIdMustBeRetrieved() throws Exception {
        when(scheduler.submit(Matchers.<Job> any())).thenReturn(new JobIdImpl(77L, "job"));

        String workflowUrl = getBaseUriTestWorkflowsServer() + "/workflow";
        JobIdData jobId = schedulerRest.submitFromUrl(sessionId, workflowUrl, getEmptyPathSegment());

        Assert.assertNotNull(jobId);
        Assert.assertEquals(77L, jobId.getId());
        Assert.assertEquals("job", jobId.getReadableName());
    }

    @Test
    public void testWhenSubmittingConcurrentlyUsingAValidWfContentUrlAllValidJobIdsMustBeRetrieved() throws Exception {
        Integer NRO_THREADS = 100;
        doAnswer(invocation -> new JobIdImpl(currentJobId++, "job")).when(scheduler).submit(Matchers.any());

        final AtomicInteger successfullySubmitted = new AtomicInteger(0);
        Runnable runnable = () -> {
            try {
                String workflowUrl = getBaseUriTestWorkflowsServer() + "/workflow";
                schedulerRest.submitFromUrl(sessionId, workflowUrl, getEmptyPathSegment());
                successfullySubmitted.incrementAndGet();
            } catch (Exception e) {
                fail(e.getMessage());
            }
        };
        List<Thread> threads = new ArrayList<>(NRO_THREADS);
        for (int i = 0; i < NRO_THREADS; i++) {
            Thread t = new Thread(runnable, "submitter-" + i);
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) {
            t.join();
        }

        Assert.assertEquals(NRO_THREADS, (Integer) successfullySubmitted.get());
    }

    @Test
    public void testWhenSubmittingAValidTemplateWithoutVariablesThenTheDefaultJobVariableIsUsed() throws Exception {
        when(scheduler.submit(Matchers.<Job> any())).thenReturn(new JobIdImpl(88L, "job"));
        ArgumentCaptor<Job> argumentCaptor = ArgumentCaptor.forClass(Job.class);

        String workflowUrl = getBaseUriTestWorkflowsServer() + "/workflow";

        JobIdData response = schedulerRest.submitFromUrl(sessionId, workflowUrl, getEmptyPathSegment());

        verify(scheduler).submit(argumentCaptor.capture());
        Job interceptedJob = argumentCaptor.getValue();
        Assert.assertEquals(1, interceptedJob.getVariables().size());
        Assert.assertEquals("defaultvalue", interceptedJob.getVariables().get("var1").getValue());

        Assert.assertEquals(88L, response.getId());
        Assert.assertEquals("job", response.getReadableName());
    }

    @Test
    public void testWhenValidatingAValidTemplateWithoutVariablesThenTheDefaultJobVariableIsUsed() throws Exception {
        String workflowUrl = getBaseUriTestWorkflowsServer() + "/workflow";

        JobValidationData response = schedulerRest.validateFromUrl(sessionId, workflowUrl, getEmptyPathSegment());

        Assert.assertEquals(1, response.getUpdatedVariables().size());
        Assert.assertEquals("defaultvalue", response.getUpdatedVariables().get("var1"));
    }

    @Test
    public void testWhenSubmittingAValidTemplateWithVariablesThenTheProvidedJobVariableIsUsed() throws Exception {
        when(scheduler.submit(Matchers.<Job> any())).thenReturn(new JobIdImpl(99L, "job"));
        ArgumentCaptor<Job> argumentCaptor = ArgumentCaptor.forClass(Job.class);

        String workflowUrl = getBaseUriTestWorkflowsServer() + "/workflow";

        JobIdData response = schedulerRest.submitFromUrl(sessionId,
                                                         workflowUrl,
                                                         getOneVariablePathSegment("var1", "value1"));

        verify(scheduler).submit(argumentCaptor.capture());
        Job interceptedJob = argumentCaptor.getValue();

        Assert.assertEquals(1, interceptedJob.getVariables().size());
        Assert.assertEquals("value1", interceptedJob.getVariables().get("var1").getValue());

        Assert.assertEquals(99L, response.getId());
        Assert.assertEquals("job", response.getReadableName());
    }

    @Test
    public void testWhenValidatingAValidTemplateWithVariablesThenTheProvidedJobVariableIsUsed() throws Exception {

        String workflowUrl = getBaseUriTestWorkflowsServer() + "/workflow";

        JobValidationData response = schedulerRest.validateFromUrl(sessionId,
                                                                   workflowUrl,
                                                                   getOneVariablePathSegment("var1", "value1"));

        Assert.assertEquals(1, response.getUpdatedVariables().size());
        Assert.assertEquals("value1", response.getUpdatedVariables().get("var1"));
    }

    @Test(expected = JobCreationRestException.class)
    public void testWhenSubmittingACorruptWorkflowThenThrowException() throws Exception {
        String workflowUrl = getBaseUriTestWorkflowsServer() + "/corrupt";
        schedulerRest.submitFromUrl(sessionId, workflowUrl, getEmptyPathSegment());
    }

    @Test
    public void testWhenValidatingACorruptWorkflowThenValidationContainsException() throws Exception {
        String workflowUrl = getBaseUriTestWorkflowsServer() + "/corrupt";
        JobValidationData response = schedulerRest.validateFromUrl(sessionId, workflowUrl, getEmptyPathSegment());
        checkInvalidResponse(response);
    }

    @Test(expected = IOException.class)
    public void testWhenSubmittingUsingAnInvalidWorkflowUrlThenThrowException() throws Exception {
        String workflowUrl = getBaseUriTestWorkflowsServer() + "/nonexistent";
        schedulerRest.submitFromUrl(sessionId, workflowUrl, getEmptyPathSegment());
    }

    @Test
    public void testWhenValidatingUsingAnInvalidWorkflowUrlThenValidationContainsException() throws Exception {
        String workflowUrl = getBaseUriTestWorkflowsServer() + "/nonexistent";
        JobValidationData response = schedulerRest.validateFromUrl(sessionId, workflowUrl, getEmptyPathSegment());
        checkInvalidResponse(response);
    }

    private void checkInvalidResponse(JobValidationData response) {
        Assert.assertFalse(response.isValid());
        Assert.assertNotNull(response.getErrorMessage());
        Assert.assertNotNull(response.getStackTrace());
    }

    @Test(expected = JobCreationRestException.class)
    public void testWhenSubmittingUsingANullWorkflowUrlThenThrowException() throws Exception {
        String workflowUrl = null;
        schedulerRest.submitFromUrl(sessionId, workflowUrl, getEmptyPathSegment());
    }

    @Test
    public void testWhenValidatingUsingANullWorkflowUrlThenValidationContainsException() throws Exception {
        String workflowUrl = null;
        JobValidationData response = schedulerRest.validateFromUrl(sessionId, workflowUrl, getEmptyPathSegment());
        checkInvalidResponse(response);
    }

    @Test(expected = NotConnectedRestException.class)
    public void testWhenExceptionSubmittingATemplateWithoutValidSessionIdThenThrowException() throws Exception {
        String sessionId = "invalidSessionId";
        schedulerRest.submitFromUrl(sessionId, null, getEmptyPathSegment());
    }

    @Test(expected = NotConnectedRestException.class)
    public void testWhenValidatingATemplateWithoutValidSessionIdThenThrowException() throws Exception {
        String sessionId = "invalidSessionId";
        schedulerRest.validateFromUrl(sessionId, null, getEmptyPathSegment());
    }

    private String getBaseUriTestWorkflowsServer() {
        return "http://localhost:" + port + "/wsh";
    }

    private PathSegment getEmptyPathSegment() {
        return getOneVariablePathSegment(null, null);
    }

    private PathSegment getOneVariablePathSegment(String key, String value) {
        PathSegment pathSegment = mock(PathSegment.class);
        MultivaluedMap<String, String> matrix = new MultivaluedHashMap<>();
        if (key != null) {
            matrix.put(key, Arrays.asList(value));
        }
        when(pathSegment.getMatrixParameters()).thenReturn(matrix);
        return pathSegment;
    }

}
