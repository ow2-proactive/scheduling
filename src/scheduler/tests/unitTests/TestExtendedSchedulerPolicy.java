/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package unitTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;
import static org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy.GENERIC_INFORMATION_KEY_START_AT;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;


/**
 * Unit tests for ExtendedSchedulerPolicy class.
 */
public class TestExtendedSchedulerPolicy {

    private ExtendedSchedulerPolicy policy;
    private String now;
    private String later;

    @Before
    public void setUp() {
        policy = new ExtendedSchedulerPolicy();
        // Thu Jan 01 01:00:00 CET 1970
        now = ISO8601DateUtil.parse(new Date(0));
        // Sun Aug 17 08:12:55 CET 292278994
        later = ISO8601DateUtil.parse(new Date(Long.MAX_VALUE));
    }

    @Test
    public void testWithoutStartAt() throws Exception {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(null, null, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 2);
    }

    @Test
    public void testJobStartAtNow() throws Exception {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(now, null, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 2);
    }

    @Test
    public void testJobStartAtLater() throws Exception {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(later, null, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 0);
    }

    @Test
    public void testTaskStartAtNow() {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(null, now, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 2);

    }

    @Test
    public void testTaskStartLater() {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(null, later, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 1);
        assertNull(startAtValue(first(orderedTasks)));
    }

    @Test
    public void testOneTaskStartNowOtherLater() {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(null, now, later));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        assertEquals(now, startAtValue(first(orderedTasks)));
    }

    @Test
    public void testJobStartNowOneTaskStartLater() {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(now, later, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        String startAtValue = startAtValue(first(orderedTasks));
        assertNull(startAtValue);
    }

    @Test
    public void testJobStartNowOneTaskStartLater2() {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(now, later, now));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        String startAtValue = startAtValue(first(orderedTasks));
        assertEquals(now, startAtValue);
    }

    @Test
    public void testJobStartLaterOneTaskStartNow() {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(later, now, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        String startAtValue = startAtValue(first(orderedTasks));
        assertEquals(now, startAtValue);
    }

    @Test
    public void testJobStartLaterOneTaskStartNow2() {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(later, now, later));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        String startAtValue = startAtValue(first(orderedTasks));
        assertEquals(now, startAtValue);
    }

    @Test
    public void testMalformedTaskStartAt() {
        List<JobDescriptor> jobDescList = asList(createJobDescWithTwoTasks(later, now, "malformed-start-at"));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 2);
    }

    private List<JobDescriptor> asList(JobDescriptor... jobDesc) {
        return Arrays.asList(jobDesc);
    }

    private String startAtValue(EligibleTaskDescriptor taskDesc) {
        return taskDesc.getInternal().getGenericInformations().get(GENERIC_INFORMATION_KEY_START_AT);
    }

    private EligibleTaskDescriptor first(List<EligibleTaskDescriptor> taskDescList) {
        return taskDescList.get(0);
    }

    private JobDescriptor createJobDescWithTwoTasks(String jobStartAt, String oneTaskStartAt,
            String otherTaskStartAt) {
        JobDescriptor jobDesc = mock(JobDescriptor.class);
        JobId jobId = mock(JobId.class);
        stub(jobId.toString()).toReturn("unit-test-job-id");
        stub(jobDesc.getJobId()).toReturn(jobId);
        InternalJob internalJob = mock(InternalJob.class);
        stub(jobDesc.getInternal()).toReturn(internalJob);

        Map<String, String> genericInfo = new HashMap<String, String>();
        genericInfo.put("START_AT", jobStartAt);
        stub(jobDesc.getInternal().getGenericInformations()).toReturn(genericInfo);

        Collection<EligibleTaskDescriptor> taskDescList = createTaskDescList(oneTaskStartAt, otherTaskStartAt);
        stub(jobDesc.getEligibleTasks()).toReturn(taskDescList);

        return jobDesc;
    }

    private Collection<EligibleTaskDescriptor> createTaskDescList(String oneStartAt, String otherStartAt) {
        EligibleTaskDescriptor eligibleTaskDescOne = createTaskDesc(oneStartAt);
        EligibleTaskDescriptor eligibleTaskDescTwo = createTaskDesc(otherStartAt);
        @SuppressWarnings("unchecked")
        Iterator<EligibleTaskDescriptor> iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, true, false);
        when(iterator.next()).thenReturn(eligibleTaskDescOne).thenReturn(eligibleTaskDescTwo);
        @SuppressWarnings("unchecked")
        Collection<EligibleTaskDescriptor> eligibleTaskDescList = mock(Collection.class);
        stub(eligibleTaskDescList.iterator()).toReturn(iterator);
        return eligibleTaskDescList;
    }

    private EligibleTaskDescriptor createTaskDesc(String startAt) {
        EligibleTaskDescriptor eligibleTaskDesc = mock(EligibleTaskDescriptor.class);
        TaskId taskId = mock(TaskId.class);
        stub(taskId.toString()).toReturn("unit-test-task-id");
        stub(eligibleTaskDesc.getTaskId()).toReturn(taskId);
        Map<String, String> genericInfo = new HashMap<String, String>();
        genericInfo.put("START_AT", startAt);
        InternalTask internalTask = mock(InternalTask.class);
        stub(internalTask.getGenericInformations()).toReturn(genericInfo);
        stub(eligibleTaskDesc.getInternal()).toReturn(internalTask);
        return eligibleTaskDesc;
    }

    public static void main(String[] args) {
        TestExtendedSchedulerPolicy test = new TestExtendedSchedulerPolicy();
        test.setUp();
        test.testMalformedTaskStartAt();
    }
}
