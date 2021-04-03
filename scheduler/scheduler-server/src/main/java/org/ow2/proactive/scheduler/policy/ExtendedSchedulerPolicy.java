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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.util.ISO8601DateUtil;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;


/**
 * ExtendedSchedulerPolicy class provides:
 * 
 * - Support for startAt generic info. The users can annotate their jobs/tasks
 * with 'startAt' generic info. Those jobs/tasks will not be scheduled for
 * execution unless current date-time of the scheduler succeeds date-time
 * specified by startAt generic info.
 * 
 * The value of 'startAt' generic info should be a ISO-8601 complaint string. If
 * the 'startAt' is specified at the task level, it overrides the value
 * specified at job level.
 * 
 */
public class ExtendedSchedulerPolicy extends DefaultPolicy {

    private static final Logger logger = Logger.getLogger(ExtendedSchedulerPolicy.class);

    public static final String GENERIC_INFORMATION_KEY_START_AT = "START_AT";

    /*
     * Utilize 'startAt' generic info and filter any tasks that should not be scheduled for current
     * execution cycle.
     */
    @Override
    public LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobDescList) {
        Date now = new Date();
        LinkedList<EligibleTaskDescriptor> executionCycleTasks = new LinkedList<>();
        Collections.sort(jobDescList, FIFO_BY_PRIORITY_COMPARATOR);

        List<JobDescriptor> filteredJobs = filterJobs(jobDescList);

        for (JobDescriptor jobDesc : filteredJobs) {
            Collection<TaskDescriptor> tasks = jobDesc.getEligibleTasks();
            Collection<EligibleTaskDescriptor> eligibleTasks = (Collection) tasks;
            for (EligibleTaskDescriptor candidate : eligibleTasks) {
                String startAt = getStartAtValue(jobDesc, candidate);
                if (startAt == null) {
                    executionCycleTasks.add(candidate);
                } else {
                    try {
                        if (now.after(ISO8601DateUtil.toDate(startAt))) {
                            executionCycleTasks.add(candidate);

                        } else {
                            if (logger.isTraceEnabled()) {
                                logger.trace(String.format("Task [jobId:\"%s\", taskId:\"%s\"] is scheduled to be executed at %s." +
                                                           " It will not be scheduled for this execution cycle at %s.",
                                                           jobDesc.getJobId(),
                                                           candidate.getTaskId(),
                                                           startAt,
                                                           ISO8601DateUtil.parse(now)));
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        logger.warn(String.format("An error occurred while processing 'startAt' generic info.%n" +
                                                  "Task ([job-id:\"%s\", task-id:\"%s\"]) will be scheduled immediately for execution.",
                                                  jobDesc.getJobId().toString(),
                                                  candidate.getTaskId().toString()),
                                    e);
                        executionCycleTasks.add(candidate);
                    }

                }
            }
        }
        return executionCycleTasks;
    }

    // To consider only non delayed jobs
    protected List<JobDescriptor> filterJobs(List<JobDescriptor> jobDescList) {
        Date now = new Date();
        LinkedList<JobDescriptor> executionCycleJobs = new LinkedList<>();
        Collections.sort(jobDescList, FIFO_BY_PRIORITY_COMPARATOR);
        for (JobDescriptor candidate : jobDescList) {
            String startAt = getStartAtValue(candidate);
            if (startAt == null) {
                executionCycleJobs.add(candidate);
            } else {
                try {
                    if (now.after(ISO8601DateUtil.toDate(startAt))) {
                        executionCycleJobs.add(candidate);

                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace(String.format("Job [jobId:\"%s\"] is scheduled to be executed at %s." +
                                                       " It will not be scheduled for this execution cycle at %s.",
                                                       candidate.getJobId().toString(),
                                                       startAt,
                                                       ISO8601DateUtil.parse(now)));
                        }
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn(String.format("An error occurred while processing 'startAt' generic info.%n" +
                                              "Job ([job-id:\"%s\"]) will be scheduled immediately for execution.",
                                              candidate.getJobId().toString(),
                                              e));
                    executionCycleJobs.add(candidate);
                }

            }
        }
        return executionCycleJobs;
    }

    private String getStartAtValue(JobDescriptor jobDesc) {
        return ((JobDescriptorImpl) jobDesc).getInternal()
                                            .getRuntimeGenericInformation()
                                            .get(GENERIC_INFORMATION_KEY_START_AT);
    }

    private String getStartAtValue(JobDescriptor jobDesc, EligibleTaskDescriptor taskDesc) {
        String startAt = ((EligibleTaskDescriptorImpl) taskDesc).getInternal()
                                                                .getRuntimeGenericInformation()
                                                                .get(GENERIC_INFORMATION_KEY_START_AT);
        if (startAt == null) {
            startAt = ((JobDescriptorImpl) jobDesc).getInternal()
                                                   .getRuntimeGenericInformation()
                                                   .get(GENERIC_INFORMATION_KEY_START_AT);
        }
        return startAt;
    }
}
