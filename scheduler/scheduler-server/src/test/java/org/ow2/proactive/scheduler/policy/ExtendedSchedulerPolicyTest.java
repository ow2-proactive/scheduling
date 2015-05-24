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
package org.ow2.proactive.scheduler.policy;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;

import java.util.*;

import static org.junit.Assert.*;
import static org.ow2.proactive.scheduler.common.task.CommonAttribute.GENERIC_INFO_START_AT_KEY;


/**
 * Unit tests for ExtendedSchedulerPolicy class.
 */
public class ExtendedSchedulerPolicyTest {

    private ExtendedSchedulerPolicy policy;
    private String now;
    private String later;

    private int jobId = 0;

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
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(null, null, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 2);
    }

    @Test
    public void testJobStartAtNow() throws Exception {
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(now, null, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 2);
    }

    @Test
    public void testJobStartAtLater() throws Exception {
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(later, null, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 0);
    }

    @Test
    public void testTaskStartAtNow() {
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(null, now, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 2);

    }

    @Test
    public void testTaskStartLater() {
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(null, later, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 1);
        assertNull(startAtValue(first(orderedTasks)));
    }

    @Test
    public void testOneTaskStartNowOtherLater() {
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(null, now, later));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        assertEquals(now, startAtValue(first(orderedTasks)));
    }

    @Test
    public void testJobStartNowOneTaskStartLater() {
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(now, later, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        String startAtValue = startAtValue(first(orderedTasks));
        assertNull(startAtValue);
    }

    @Test
    public void testJobStartNowOneTaskStartLater2() {
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(now, later, now));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        String startAtValue = startAtValue(first(orderedTasks));
        assertEquals(now, startAtValue);
    }

    @Test
    public void testJobStartLaterOneTaskStartNow() {
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(later, now, null));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        String startAtValue = startAtValue(first(orderedTasks));
        assertEquals(now, startAtValue);
    }

    @Test
    public void testJobStartLaterOneTaskStartNow2() {
        List<JobDescriptor> jobDescList = asModifiableList(createJobDescWithTwoTasks(later, now, later));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks.size() == 1);
        String startAtValue = startAtValue(first(orderedTasks));
        assertEquals(now, startAtValue);
    }

    @Test
    public void testMalformedTaskStartAt() {
        List<JobDescriptor> jobDescList = asModifiableList(
          createJobDescWithTwoTasks(later, now, "malformed-start-at"));
        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);
        assertTrue(orderedTasks != null && orderedTasks.size() == 2);
    }

    @Test
    public void job_with_same_priorities() throws Exception {
        JobDescriptor job1 = createJobDescWithTwoTasks(null, null, null);
        JobDescriptor job2 = createJobDescWithTwoTasks(null, null, null);
        JobDescriptor job3 = createJobDescWithTwoTasks(null, null, null);

        List<JobDescriptor> jobDescList = asModifiableList(job1, job3, job2);

        Vector<EligibleTaskDescriptor> orderedTasks = policy.getOrderedTasks(jobDescList);

        assertEquals(job1.getJobId(), orderedTasks.get(0).getJobId());
        assertEquals(job2.getJobId(), orderedTasks.get(2).getJobId());
        assertEquals(job3.getJobId(), orderedTasks.get(4).getJobId());
    }

    private List<JobDescriptor> asModifiableList(JobDescriptor... jobDesc) {
        return Arrays.asList(jobDesc);
    }

    private String startAtValue(EligibleTaskDescriptor taskDesc) {
        return taskDesc.getInternal().getGenericInformations().get(GENERIC_INFO_START_AT_KEY);
    }

    private EligibleTaskDescriptor first(List<EligibleTaskDescriptor> taskDescList) {
        return taskDescList.get(0);
    }

    private JobDescriptor createJobDescWithTwoTasks(String jobStartAt, String oneTaskStartAt,
            String otherTaskStartAt) {

        InternalTaskFlowJob taskFlowJob = new InternalTaskFlowJob("test", JobPriority.NORMAL, true, "");
        taskFlowJob.setId(JobIdImpl.makeJobId(Integer.toString(jobId++)));

        ArrayList<InternalTask> tasks = new ArrayList<InternalTask>();
        tasks.add(createTask(oneTaskStartAt));
        tasks.add(createTask(otherTaskStartAt));
        taskFlowJob.addTasks(tasks);

        if (jobStartAt != null) {
            taskFlowJob.addGenericInformation("START_AT", jobStartAt);
        }

        return new JobDescriptorImpl(taskFlowJob);
    }

    private InternalScriptTask createTask(String taskStartAt) {
        InternalScriptTask task1 = new InternalScriptTask();
        if (taskStartAt != null) {
            task1.addGenericInformation("START_AT", taskStartAt);
        }
        return task1;
    }

}
