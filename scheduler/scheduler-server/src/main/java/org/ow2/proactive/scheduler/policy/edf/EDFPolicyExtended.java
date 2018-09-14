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
package org.ow2.proactive.scheduler.policy.edf;

import java.time.Duration;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.core.JobEmailNotificationException;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;


/**
 * Early Deadline First Policy sorts jobs based on:
 * - job priorities (from highest to lowest)
 * - job deadline (those that have deadline overtake those without deadline)
 *
 * Among the job with the dealine (and having same priority) we distinguish
 * them by the fact if some task of the job is already scheduled (i.g. job has
 * a startingTime) or not. Those that started has a priority to the not yet started jobs.
 * Among the jobs that are already started we sort them by startTime.
 * If job is not started, we sort them by `effectiveDeadline - (now() + expectedTime`.
 * If deadline is absolute then effectiveDeadline equals to deadline.
 * If deadline is relative then effectiveDeadline is calculated as (now() + deadline)
 *
 * Among jobs without deadline (and the same priority), we sort them by submission date.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 8.3
 */
public class EDFPolicyExtended extends ExtendedSchedulerPolicy {

    private static final Date MAXIMUM_DATE = new Date(Long.MAX_VALUE);

    private static final Logger LOGGER = Logger.getLogger(EDFPolicyExtended.class);

    @Override
    public LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs) {
        final Date now = new Date();

        fireEventsIfSomeJobsWillNotMeetTheirDeadlines(jobs);

        final Comparator<JobDescriptor> jobDescriptorComparator = (job1, job2) -> {

            final InternalJob internalJob1 = ((JobDescriptorImpl) job1).getInternal();
            final InternalJob internalJob2 = ((JobDescriptorImpl) job2).getInternal();
            JobPriority job1Priority = internalJob1.getPriority();
            JobPriority job2Priority = internalJob2.getPriority();
            if (!job1Priority.equals(job2Priority)) {
                // if priorities are different compare by them
                return job2Priority.compareTo(job1Priority);
            } else { // priorities are the same
                if (internalJob1.getJobDeadline().isPresent() & !internalJob2.getJobDeadline().isPresent()) {
                    // job with deadline has an advanrage to the job without deadline
                    return -1;
                } else if (!internalJob1.getJobDeadline().isPresent() & internalJob2.getJobDeadline().isPresent()) {
                    // job with deadline has an advanrage to the job without deadline
                    return 1;
                } else if (noDeadlines(internalJob1, internalJob2)) {
                    // if two jobs do not have deadlines - we compare by the submitted time
                    return Long.compare(internalJob1.getJobInfo().getSubmittedTime(),
                                        internalJob2.getJobInfo().getSubmittedTime());
                } else { // both dead line are present
                    if (bothStarted(internalJob1, internalJob2)) {
                        // both jobs are started
                        // then compare their startTime
                        return Long.compare(internalJob1.getJobInfo().getStartTime(),
                                            internalJob2.getJobInfo().getStartTime());
                    } else if (internalJob1.getJobInfo().getStartTime() >= 0 &&
                               internalJob2.getJobInfo().getStartTime() < 0) {
                        // priority to already started - internalJob1
                        return -1;
                    } else if (internalJob1.getJobInfo().getStartTime() < 0 &&
                               internalJob2.getJobInfo().getStartTime() >= 0) {
                        // priority to already started - internalJob2
                        return 1;
                    } else { // non of the jobs are started
                        // give a priority with the smaller interval between possible end of the job
                        // and job deadline
                        final Duration gap1 = durationBetweenFinishAndDeadline(internalJob1, now);
                        final Duration gap2 = durationBetweenFinishAndDeadline(internalJob2, now);
                        return gap1.compareTo(gap2);
                    }

                }
            }
        };

        return jobs.stream()
                   .sorted(jobDescriptorComparator)
                   .flatMap(jobDescriptor -> jobDescriptor.getEligibleTasks().stream())
                   .map(taskDescriptors -> (EligibleTaskDescriptor) taskDescriptors)
                   .collect(Collectors.toCollection(LinkedList::new));
    }

    private boolean bothStarted(InternalJob internalJob1, InternalJob internalJob2) {
        return internalJob1.getJobInfo().getStartTime() >= 0 && internalJob2.getJobInfo().getStartTime() >= 0;
    }

    private boolean noDeadlines(InternalJob internalJob1, InternalJob internalJob2) {
        return !internalJob1.getJobDeadline().isPresent() & !internalJob2.getJobDeadline().isPresent();
    }

    private static Duration durationBetweenFinishAndDeadline(InternalJob internalJob, Date now) {
        final Date effectiveDeadline = getEffectiveDeadline(internalJob, now);
        final Date effectiveExpectedExecutionTime = getEffectiveExpectedExecutionTime(internalJob, now);
        final long gapInMillis = effectiveDeadline.getTime() - effectiveExpectedExecutionTime.getTime();
        return Duration.ofMillis(gapInMillis);
    }

    /**
     * @return deadline of the job if existed, otherwise returns biggest date possible
     * (this is how this policy treats absence of deadline)
     */
    public static Date getEffectiveDeadline(InternalJob internalJob, Date now) {
        if (internalJob.getJobDeadline().isPresent()) {
            if (internalJob.getJobDeadline().get().isAbsolute()) {
                return internalJob.getJobDeadline().get().getAbsoluteDeadline();
            } else {
                final Duration relativeDeadline = internalJob.getJobDeadline().get().getRelativeDeadline();
                Calendar cal = Calendar.getInstance(); // creates calendar
                cal.setTime(now); // sets calendar time/date
                cal.add(Calendar.SECOND, (int) relativeDeadline.getSeconds()); // adds one hour
                return cal.getTime();
            }
        } else {
            return MAXIMUM_DATE;
        }
    }

    public static Date getEffectiveExpectedExecutionTime(InternalJob internalJob, Date now) {
        if (internalJob.getJobExpectedExecutionTime().isPresent()) {
            final Duration expectedTime = internalJob.getJobExpectedExecutionTime().get();
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(now); // sets calendar time/date
            cal.add(Calendar.SECOND, (int) expectedTime.getSeconds()); // adds one hour
            return cal.getTime();
        } else {
            return now;
        }
    }

    private void fireEventsIfSomeJobsWillNotMeetTheirDeadlines(List<JobDescriptor> jobs) {
        Date now = new Date();
        final List<InternalJob> jobsWhichWillMissDeadlines = jobs.stream()
                                                                 .map(jobDescriptor -> ((JobDescriptorImpl) jobDescriptor).getInternal())
                                                                 .filter(job -> getEffectiveDeadline(job,
                                                                                                     now).compareTo(getEffectiveExpectedExecutionTime(job,
                                                                                                                                                      now)) < 0)
                                                                 .collect(Collectors.toList());

        jobsWhichWillMissDeadlines.forEach(job -> {
            LOGGER.warn(String.format("Job[id=%s] might miss its deadline (expected finish: %s after deadline: %s)",
                                      job.getId().value(),
                                      getEffectiveExpectedExecutionTime(job, now),
                                      getEffectiveDeadline(job, now)));
            try {
                new JobDeadlineEmailNotification(job).doSend();
            } catch (JobEmailNotificationException e) {
                LOGGER.error(e);
            }
        });
    }
}
