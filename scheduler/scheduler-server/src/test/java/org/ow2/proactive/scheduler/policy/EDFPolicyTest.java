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
package org.ow2.proactive.scheduler.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.edf.EDFPolicy;
import org.ow2.tests.ProActiveTestClean;


public class EDFPolicyTest extends ProActiveTestClean {

    private JobDescriptorImpl jobPlus2HoursLowest;

    private JobDescriptorImpl jobPlus2HoursHighest;

    private JobDescriptorImpl jobPlus5HoursLowest;

    private JobDescriptorImpl jobPlus5HoursHighest;

    private JobDescriptorImpl jobPlus10HoursLowest;

    private JobDescriptorImpl jobPlus10HoursHighest;

    private JobDescriptorImpl jobEmptyDeadlineLowest;

    private JobDescriptorImpl jobEmptyDeadlineHighest;

    private JobDescriptorImpl jobMinus2HoursLowest;

    private JobDescriptorImpl jobMinus2HoursHighest;

    @Before
    public void setUp() {

        jobPlus2HoursLowest = createJob(JobPriority.LOWEST, Optional.of(new DateTime().plusHours(2).toDate()));

        jobPlus2HoursHighest = createJob(JobPriority.HIGHEST, Optional.of(new DateTime().plusHours(2).toDate()));

        jobPlus5HoursLowest = createJob(JobPriority.LOWEST, Optional.of(new DateTime().plusHours(5).toDate()));

        jobPlus5HoursHighest = createJob(JobPriority.HIGHEST, Optional.of(new DateTime().plusHours(5).toDate()));

        jobPlus10HoursLowest = createJob(JobPriority.LOWEST, Optional.of(new DateTime().plusHours(10).toDate()));

        jobPlus10HoursHighest = createJob(JobPriority.HIGHEST, Optional.of(new DateTime().plusHours(10).toDate()));

        jobEmptyDeadlineLowest = createJob(JobPriority.LOWEST, Optional.empty());

        jobEmptyDeadlineHighest = createJob(JobPriority.HIGHEST, Optional.empty());

        jobMinus2HoursLowest = createJob(JobPriority.LOWEST, Optional.of(new DateTime().minusHours(2).toDate()));

        jobMinus2HoursHighest = createJob(JobPriority.HIGHEST, Optional.of(new DateTime().minusHours(2).toDate()));

    }

    private JobDescriptorImpl createJob(JobPriority jobPriority, Optional<Date> deadline) {
        final JobDescriptorImpl job = mock(JobDescriptorImpl.class);

        final JobId jobId = mock(JobId.class);
        when(jobId.value()).thenReturn("id " + jobPriority + deadline.map(Date::toString).orElse(" no deadline"));
        when(job.getJobId()).thenReturn(jobId);
        final InternalJob internalJob = mock(InternalJob.class);
        when(job.getInternal()).thenReturn(internalJob);
        when(internalJob.getJobDeadline()).thenReturn(deadline);
        when(internalJob.getPriority()).thenReturn(jobPriority);

        final EligibleTaskDescriptor taskDescriptor0 = mock(EligibleTaskDescriptor.class);
        when(taskDescriptor0.getJobId()).thenReturn(jobId);

        final EligibleTaskDescriptor taskDescriptor1 = mock(EligibleTaskDescriptor.class);
        when(taskDescriptor1.getJobId()).thenReturn(jobId);
        when(job.getEligibleTasks()).thenReturn(Arrays.asList(taskDescriptor0, taskDescriptor1));
        return job;
    }

    @Test
    public void emptyListOfJobsTest() {
        final LinkedList<EligibleTaskDescriptor> orderedTasks = new EDFPolicy().getOrderedTasks(Collections.emptyList());
        assertTrue(orderedTasks.isEmpty());
    }

    @Test
    public void jobsHaveSamePriorityTest() {
        final LinkedList<EligibleTaskDescriptor> orderedTasks = new EDFPolicy().getOrderedTasks(Arrays.asList(jobPlus10HoursHighest,
                                                                                                              jobPlus2HoursHighest,
                                                                                                              jobPlus5HoursHighest,
                                                                                                              jobEmptyDeadlineHighest,
                                                                                                              jobMinus2HoursHighest));
        assertEquals(jobMinus2HoursHighest.getJobId().value(), orderedTasks.get(0).getJobId().value());
        assertEquals(jobMinus2HoursHighest.getJobId().value(), orderedTasks.get(1).getJobId().value());
        assertEquals(jobPlus2HoursHighest.getJobId().value(), orderedTasks.get(2).getJobId().value());
        assertEquals(jobPlus2HoursHighest.getJobId().value(), orderedTasks.get(3).getJobId().value());
        assertEquals(jobPlus5HoursHighest.getJobId().value(), orderedTasks.get(4).getJobId().value());
        assertEquals(jobPlus5HoursHighest.getJobId().value(), orderedTasks.get(5).getJobId().value());
        assertEquals(jobPlus10HoursHighest.getJobId().value(), orderedTasks.get(6).getJobId().value());
        assertEquals(jobPlus10HoursHighest.getJobId().value(), orderedTasks.get(7).getJobId().value());
        assertEquals(jobEmptyDeadlineHighest.getJobId().value(), orderedTasks.get(8).getJobId().value());
        assertEquals(jobEmptyDeadlineHighest.getJobId().value(), orderedTasks.get(9).getJobId().value());
    }

    @Test
    public void jobsHaveDifferentPrioritiesTest() {
        final LinkedList<EligibleTaskDescriptor> orderedTasks = new EDFPolicy().getOrderedTasks(Arrays.asList(jobPlus10HoursHighest,
                                                                                                              jobPlus2HoursLowest,
                                                                                                              jobPlus5HoursHighest,
                                                                                                              jobEmptyDeadlineLowest,
                                                                                                              jobMinus2HoursHighest));
        assertEquals(jobMinus2HoursHighest.getJobId().value(), orderedTasks.get(0).getJobId().value());
        assertEquals(jobMinus2HoursHighest.getJobId().value(), orderedTasks.get(1).getJobId().value());
        assertEquals(jobPlus5HoursHighest.getJobId().value(), orderedTasks.get(2).getJobId().value());
        assertEquals(jobPlus5HoursHighest.getJobId().value(), orderedTasks.get(3).getJobId().value());
        assertEquals(jobPlus10HoursHighest.getJobId().value(), orderedTasks.get(4).getJobId().value());
        assertEquals(jobPlus10HoursHighest.getJobId().value(), orderedTasks.get(5).getJobId().value());
        assertEquals(jobPlus2HoursLowest.getJobId().value(), orderedTasks.get(6).getJobId().value());
        assertEquals(jobPlus2HoursLowest.getJobId().value(), orderedTasks.get(7).getJobId().value());
        assertEquals(jobEmptyDeadlineLowest.getJobId().value(), orderedTasks.get(8).getJobId().value());
        assertEquals(jobEmptyDeadlineLowest.getJobId().value(), orderedTasks.get(9).getJobId().value());
    }

}
