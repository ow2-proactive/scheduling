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
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.util.ISO8601DateUtil;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.util.MultipleTimingLogger;

import com.google.common.annotations.VisibleForTesting;


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

    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                                                                                          new NamedThreadFactory("ExtendedSchedulerPolicyExecutor",
                                                                                                                 true,
                                                                                                                 2));

    // cache used to wake up the scheduler for delayed jobs
    // the key is the delayed datetime in milliseconds
    private static final Map<Long, Boolean> delayedJobsWakeUpCache = Collections.synchronizedMap(new LRUMap<>(PASchedulerProperties.SCHEDULER_STARTAT_CACHE.getValueAsInt()));

    // cache used to store the start at value of a given job or task
    private static final Map<String, String> startAtCache = Collections.synchronizedMap(new LRUMap<>(PASchedulerProperties.SCHEDULER_STARTAT_VALUE_CACHE.getValueAsInt()));

    private static final Map<String, Date> startAtConversionCache = Collections.synchronizedMap(new LRUMap<>(PASchedulerProperties.SCHEDULER_STARTAT_CACHE.getValueAsInt()));

    protected MultipleTimingLogger schedulingPolicyTimingLogger = null;

    private void initialize() {
        if (schedulingPolicyTimingLogger == null) {
            schedulingPolicyTimingLogger = new MultipleTimingLogger("SchedulingPolicyTiming", logger);
        }
    }

    /*
     * Utilize 'startAt' generic info and filter any tasks that should not be scheduled for current
     * execution cycle.
     */
    @Override
    public LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobDescList) {
        initialize();
        schedulingPolicyTimingLogger.start("ESP.getOrderedTasks");
        Date now = new Date();
        LinkedList<EligibleTaskDescriptor> executionCycleTasks = new LinkedList<>();

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
                        Date startAtDate;
                        if (startAtConversionCache.containsKey(startAt)) {
                            startAtDate = startAtConversionCache.get(startAt);
                        } else {
                            startAtDate = ISO8601DateUtil.toDate(startAt);
                            startAtConversionCache.put(startAt, startAtDate);
                        }
                        if (now.after(startAtDate)) {
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
                            scheduleWakeUp(now, startAtDate);

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
        schedulingPolicyTimingLogger.end("ESP.getOrderedTasks");
        if (!schedulingPolicyTimingLogger.isHierarchical()) {
            schedulingPolicyTimingLogger.printTimings(Level.DEBUG);
            schedulingPolicyTimingLogger.clear();
        }
        return executionCycleTasks;
    }

    private void scheduleWakeUp(Date now, Date startAtDate) {
        final long scheduledTime = startAtDate.getTime();
        if (!delayedJobsWakeUpCache.containsKey(scheduledTime)) {
            executor.schedule(() -> {
                delayedJobsWakeUpCache.remove(scheduledTime);
                schedulingService.wakeUpSchedulingThread();
            }, scheduledTime - now.getTime(), TimeUnit.MILLISECONDS);
            delayedJobsWakeUpCache.put(scheduledTime, true);
        }
    }

    // To consider only non delayed jobs
    protected List<JobDescriptor> filterJobs(List<JobDescriptor> jobDescList) {
        initialize();
        schedulingPolicyTimingLogger.start("ESP.filterJobs");
        Date now = new Date();
        LinkedList<JobDescriptor> executionCycleJobs = new LinkedList<>();
        for (JobDescriptor candidate : jobDescList) {
            String startAt = getStartAtValue(candidate);
            if (startAt == null) {
                executionCycleJobs.add(candidate);
            } else {
                try {
                    Date startAtDate;
                    if (startAtConversionCache.containsKey(startAt)) {
                        startAtDate = startAtConversionCache.get(startAt);
                    } else {
                        startAtDate = ISO8601DateUtil.toDate(startAt);
                        startAtConversionCache.put(startAt, startAtDate);
                    }
                    if (now.after(startAtDate)) {
                        executionCycleJobs.add(candidate);
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace(String.format("Job [jobId:\"%s\"] is scheduled to be executed at %s." +
                                                       " It will not be scheduled for this execution cycle at %s.",
                                                       candidate.getJobId().toString(),
                                                       startAt,
                                                       ISO8601DateUtil.parse(now)));
                        }
                        scheduleWakeUp(now, startAtDate);
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
        Collections.sort(executionCycleJobs, FIFO_BY_PRIORITY_COMPARATOR);
        schedulingPolicyTimingLogger.end("ESP.filterJobs");
        return executionCycleJobs;
    }

    public void updateStartAt(String key, String startAt) {
        if (startAtCache.containsKey(key)) {
            startAtCache.put(key, startAt);
        }
    }

    private String getStartAtValue(JobDescriptor jobDesc) {
        String key = jobDesc.getJobId().value();
        if (startAtCache.containsKey(key)) {
            return startAtCache.get(key);
        }
        String startAt = ((JobDescriptorImpl) jobDesc).getInternal()
                                                      .getRuntimeGenericInformation()
                                                      .get(GENERIC_INFORMATION_KEY_START_AT);
        startAtCache.put(key, startAt);
        return startAt;
    }

    private String getStartAtValue(JobDescriptor jobDesc, EligibleTaskDescriptor taskDesc) {
        String key = taskDesc.getTaskId().toString();
        if (startAtCache.containsKey(key)) {
            return startAtCache.get(key);
        }
        String startAt = ((EligibleTaskDescriptorImpl) taskDesc).getInternal()
                                                                .getRuntimeGenericInformation()
                                                                .get(GENERIC_INFORMATION_KEY_START_AT);
        if (startAt == null) {
            startAt = ((JobDescriptorImpl) jobDesc).getInternal()
                                                   .getRuntimeGenericInformation()
                                                   .get(GENERIC_INFORMATION_KEY_START_AT);
        }
        startAtCache.put(key, startAt);
        return startAt;
    }

    @VisibleForTesting
    public void clearCaches() {
        delayedJobsWakeUpCache.clear();
        startAtCache.clear();
    }
}
