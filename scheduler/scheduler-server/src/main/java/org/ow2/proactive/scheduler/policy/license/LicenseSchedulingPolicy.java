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
package org.ow2.proactive.scheduler.policy.license;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.MultipleTimingLogger;
import org.ow2.proactive.utils.NodeSet;


/**
 *
 * This Policy is designed to manage software licenses.
 * When a job/task contains the generic information REQUIRE_LICENSES,
 * this policy will check if there are available tokens per required license.
 *
 */
public class LicenseSchedulingPolicy extends ExtendedSchedulerPolicy {

    private static final Logger logger = Logger.getLogger(LicenseSchedulingPolicy.class);

    private static final String REQUIRED_LICENSES = "REQUIRED_LICENSES";

    // Will be initialize with the license properties file which includes the number of tokens per license
    private Properties properties = null;

    // For DB persistence
    private LicenseSynchronization licenseSynchronization = null;

    // cache used to store the start at value of a given job or task
    private static final Map<String, String> requiredLicencesCache = Collections.synchronizedMap(new LRUMap<>(PASchedulerProperties.SCHEDULER_STARTAT_VALUE_CACHE.getValueAsInt()));

    private volatile boolean needPersist = false;

    protected void initialize() {
        if (schedulingPolicyTimingLogger == null) {
            schedulingPolicyTimingLogger = new MultipleTimingLogger("LicenseSchedulingPolicyTiming", logger, true);
        }

        if (properties == null) {

            logger.debug("Retrieve properties from the license properties file");
            properties = LicenseConfiguration.getConfiguration().getProperties();

            logger.debug("Initializing LicenseSynchronization");
            licenseSynchronization = new LicenseSynchronization();

            Enumeration propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String software = (String) propertyNames.nextElement();
                int nbTokens;
                try {
                    nbTokens = Integer.parseInt(properties.getProperty(software));
                } catch (NumberFormatException e) {
                    logger.error("Fatal error, cannot parse token number from the license properties file");
                    throw e;
                }

                if (nbTokens > 0) {
                    licenseSynchronization.addSoftware(software, nbTokens);
                } else {
                    logger.error("In " + LicenseConfiguration.getConfiguration().getPath() + ": software " + software +
                                 " has incorrect value " + nbTokens);
                    continue;
                }
            }
            licenseSynchronization.persist();
        }
    }

    private void removeFinishedJobsAndReleaseTokens(String requiredLicense) {
        schedulingPolicyTimingLogger.start("LP.removeFinishedJobsAndReleaseTokens");

        schedulingPolicyTimingLogger.start("LP.getPersistedJobsLicense");
        LinkedBlockingQueue<String> eligibleJobsDescriptorsLicense = licenseSynchronization.getPersistedJobsLicense(requiredLicense);
        schedulingPolicyTimingLogger.end("LP.getPersistedJobsLicense");

        Iterator<String> iter = eligibleJobsDescriptorsLicense.iterator();
        String currentJobIdAsString;
        JobId currentJobId;
        JobStatus currentJobStatus;
        boolean anyChange = false;
        schedulingPolicyTimingLogger.start("LP.Jobs.getRemainingTokens");
        int currentNbTokens = licenseSynchronization.getRemainingTokens(requiredLicense);
        schedulingPolicyTimingLogger.end("LP.Jobs.getRemainingTokens");
        while (iter.hasNext()) {
            currentJobIdAsString = iter.next();
            currentJobId = JobIdImpl.makeJobId(currentJobIdAsString);
            schedulingPolicyTimingLogger.start("LP.getJobStatus");
            currentJobStatus = schedulingService.getJobStatus(currentJobId);
            schedulingPolicyTimingLogger.end("LP.getJobStatus");
            logger.trace("Considering job " + currentJobIdAsString + " with status " + currentJobStatus +
                         " requiring token for license " + requiredLicense);

            schedulingPolicyTimingLogger.start("LP.isJobAlive");
            if (!schedulingService.isJobAlive(currentJobId) || currentJobStatus == JobStatus.PAUSED ||
                currentJobStatus == JobStatus.IN_ERROR) {
                logger.trace("Releasing token");
                iter.remove();
                currentNbTokens++;
                anyChange = true;
            } else {
                logger.trace("Job status does not allow to release a token");
            }
            schedulingPolicyTimingLogger.end("LP.isJobAlive");
        }
        if (anyChange) {
            licenseSynchronization.setNbTokens(requiredLicense, currentNbTokens);
            licenseSynchronization.markJobLicenseChange(requiredLicense);
            needPersist = true;
        }
        schedulingPolicyTimingLogger.end("LP.removeFinishedJobsAndReleaseTokens");
    }

    private void removeFinishedTasksAndReleaseTokens(String requiredLicense) {
        schedulingPolicyTimingLogger.start("LP.removeFinishedTasksAndReleaseTokens");

        schedulingPolicyTimingLogger.start("LP.getPersistedTasksLicense");
        LinkedBlockingQueue<String> eligibleTasksDescriptorsLicense = licenseSynchronization.getPersistedTasksLicense(requiredLicense);
        schedulingPolicyTimingLogger.end("LP.getPersistedTasksLicense");

        Iterator<String> iter = eligibleTasksDescriptorsLicense.iterator();
        String currentTaskIdAsString;
        TaskId currentTaskId;
        TaskStatus currentTaskStatus;
        boolean anyChange = false;
        schedulingPolicyTimingLogger.start("LP.Tasks.getRemainingTokens");
        int currentNbTokens = licenseSynchronization.getRemainingTokens(requiredLicense);
        schedulingPolicyTimingLogger.end("LP.Tasks.getRemainingTokens");
        while (iter.hasNext()) {
            currentTaskIdAsString = iter.next();
            currentTaskId = TaskIdImpl.makeTaskId(currentTaskIdAsString);
            schedulingPolicyTimingLogger.start("LP.getTaskStatus");
            currentTaskStatus = schedulingService.getTaskStatus(currentTaskId);
            schedulingPolicyTimingLogger.end("LP.getTaskStatus");
            logger.trace("Considering task " + currentTaskIdAsString + " with status " + currentTaskStatus +
                         " requiring token for license " + requiredLicense);

            schedulingPolicyTimingLogger.start("LP.isTaskAlive");
            if (!schedulingService.isTaskAlive(currentTaskId) || currentTaskStatus == TaskStatus.PAUSED ||
                currentTaskStatus == TaskStatus.IN_ERROR) {
                logger.trace("Releasing token");
                iter.remove();
                currentNbTokens++;
                anyChange = true;
            } else {
                logger.trace("Task status does not allow to release a token");
            }
            schedulingPolicyTimingLogger.end("LP.isTaskAlive");
        }
        if (anyChange) {
            licenseSynchronization.setNbTokens(requiredLicense, currentNbTokens);
            licenseSynchronization.markTaskLicenseChange(requiredLicense);
            needPersist = true;
        }
        schedulingPolicyTimingLogger.end("LP.removeFinishedTasksAndReleaseTokens");
    }

    private boolean canGetToken(String currentRequiredLicense) {
        schedulingPolicyTimingLogger.start("LP.canGetToken");
        try {
            if (licenseSynchronization.getRemainingTokens(currentRequiredLicense) == -1) {
                logger.trace("No token specified in the config file, return false");
                return false;
            } else if (licenseSynchronization.getRemainingTokens(currentRequiredLicense) > 0) {
                logger.trace("There are still remaining licenses, return true");
                return true;
            } else { // == 0
                logger.trace("Removing all terminated jobs/tasks and check again if there are remaining licenses");

                removeFinishedJobsAndReleaseTokens(currentRequiredLicense);
                removeFinishedTasksAndReleaseTokens(currentRequiredLicense);

                if (licenseSynchronization.getRemainingTokens(currentRequiredLicense) > 0) {
                    logger.trace("There are remaining licenses after releasing tokens, return true");
                    return true;
                }
                return false;
            }
        } finally {
            schedulingPolicyTimingLogger.end("LP.canGetToken");
        }
    }

    private boolean acquireTokensForJob(JobDescriptorImpl job) {
        schedulingPolicyTimingLogger.start("LP.acquireTokensForJob");
        try {

            // Retrieve required licenses names from the job generic informations
            schedulingPolicyTimingLogger.start("LP.acquireTokensForJob.getRequiredLicenses");
            final String requiredLicenses = getRequiredLicenses(job);
            schedulingPolicyTimingLogger.end("LP.acquireTokensForJob.getRequiredLicenses");
            final String jobId = job.getJobId().value();

            // If it requires software licenses
            if (requiredLicenses != null) {

                logger.trace("For job considering licenses " + requiredLicenses);

                // Do not execute if one of the required license can not be obtained
                final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split("\\s*,\\s*")));
                boolean jobHoldsToken, canGetToken;
                for (String requiredLicense : requiredLicensesSet) {

                    schedulingPolicyTimingLogger.start("LP.acquireTokensForJob.containsLicense");
                    if (!licenseSynchronization.containsLicense(requiredLicense)) {
                        logger.trace("The required software is not specified in the license properties file, keep job pending");
                        return false;
                    }
                    schedulingPolicyTimingLogger.end("LP.acquireTokensForJob.containsLicense");

                    jobHoldsToken = licenseSynchronization.containsJobId(requiredLicense, jobId);
                    schedulingPolicyTimingLogger.start("LP.acquireTokensForJob.canGetToken");
                    canGetToken = canGetToken(requiredLicense);
                    schedulingPolicyTimingLogger.end("LP.acquireTokensForJob.canGetToken");
                    if (!jobHoldsToken && !canGetToken) {
                        logger.trace("Job does not already hold a token for " + requiredLicense +
                                     " and cannot get a new one, keep job pending");
                        return false;
                    }
                }
                logger.trace("Job eligible since it can get all tokens or already hold them");
                schedulingPolicyTimingLogger.start("LP.acquireTokensForJob.addJobToLicense");
                for (String requiredLicense : requiredLicensesSet) {
                    if (!licenseSynchronization.containsJobId(requiredLicense, jobId)) {
                        licenseSynchronization.addJobToLicense(requiredLicense, jobId);
                        licenseSynchronization.markJobLicenseChange(requiredLicense);
                        needPersist = true;
                    }
                }
                schedulingPolicyTimingLogger.end("LP.acquireTokensForJob.addJobToLicense");
                return true;

            } else {
                logger.trace("Job eligible since it does not require any license");
                return true;
            }
        } finally {
            schedulingPolicyTimingLogger.end("LP.acquireTokensForJob");
        }

    }

    private String getRequiredLicenses(JobDescriptorImpl job) {
        if (requiredLicencesCache.containsKey(job.getJobId().toString())) {
            return requiredLicencesCache.get(job.getJobId().toString());
        }
        final String requiredLicenses = job.getInternal().getRuntimeGenericInformation().get(REQUIRED_LICENSES);
        requiredLicencesCache.put(job.getJobId().toString(), requiredLicenses);
        return requiredLicenses;
    }

    private String getRequiredLicenses(EligibleTaskDescriptor task) {
        // Do not consider the runtime GIs to test if REQUIRED_LICENSES is specified at the task level
        // since these inherit from the job level ones
        // And if REQUIRED_LICENSES is specified at the task level (i.e. in the unresolved GIs)
        // Consider the runtime GIs to get the resolved REQUIRED_LICENSES
        if (requiredLicencesCache.containsKey(task.getTaskId().toString())) {
            return requiredLicencesCache.get(task.getTaskId().toString());
        }
        final InternalTask internTask = ((EligibleTaskDescriptorImpl) task).getInternal();
        if (internTask.getGenericInformation().containsKey(REQUIRED_LICENSES)) {
            // Retrieve required licenses names from the task generic information
            String licenses = internTask.getRuntimeGenericInformation().get(REQUIRED_LICENSES);
            requiredLicencesCache.put(task.getTaskId().toString(), licenses);
            return licenses;
        } else {
            logger.trace("Task eligible since it does not require any license");
            requiredLicencesCache.put(task.getTaskId().toString(), null);
            return null;
        }
    }

    private boolean notRequireLicenseOrLicenseExists(EligibleTaskDescriptor task) {

        schedulingPolicyTimingLogger.start("LP.notRequireLicense.getRequiredLicenses");
        String requiredLicenses = getRequiredLicenses(task);
        schedulingPolicyTimingLogger.end("LP.notRequireLicense.getRequiredLicenses");

        // If it requires software licenses
        if (requiredLicenses != null) {

            logger.trace("For task considering licenses " + requiredLicenses);

            // Do not execute if one of the required license can not be obtained
            final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split("\\s*,\\s*")));
            for (String requiredLicense : requiredLicensesSet) {

                if (!licenseSynchronization.containsLicense(requiredLicense)) {
                    logger.trace("Task not eligible since no token exists for this license");
                    return false;
                }
            }
            logger.trace("Task eligible since all required licenses are specified in the properties file");
            return true;

        } else {
            logger.trace("Task eligible since it does not require any license");
            return true;
        }
    }

    /*
     * Here we only keep tasks which, either do not require license, or require licenses specified
     * in the license configuration file.
     * We do not consider available tokens number yet at task level (i.e. we consider we have an
     * infinite
     * number of tokens for each license).
     * By this way, we wont reserve tokens for non running tasks (blocking selection script, ...).
     * We only consider tokens number at job level.
     */
    @Override
    public LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobDescList) {

        initialize();
        schedulingPolicyTimingLogger.start("LP.getOrderedTasks");
        needPersist = false;

        // Filter jobs according to the parent policy
        List<JobDescriptor> filteredJobDescList = super.filterJobs(jobDescList);
        // Keep only executable jobs according to their required license tokens
        filteredJobDescList = filteredJobDescList.stream()
                                                 .filter(jobDesc -> acquireTokensForJob((JobDescriptorImpl) jobDesc))
                                                 .collect(Collectors.toList());

        // Retrieve the ordered tasks from the filtered jobs according to the parent policy
        LinkedList<EligibleTaskDescriptor> orderedTasksDescFromParentPolicy = super.getOrderedTasks(filteredJobDescList);

        // Keep only tasks which, either does not require license, or require licenses specified in the config file (without considering tokens)
        schedulingPolicyTimingLogger.start("LP.notRequireLicenseOrLicenseExists");
        LinkedList<EligibleTaskDescriptor> filteredOrderedTasksDescFromParentPolicy = orderedTasksDescFromParentPolicy.stream()
                                                                                                                      .filter(taskDesc -> notRequireLicenseOrLicenseExists(taskDesc))
                                                                                                                      .collect(Collectors.toCollection(LinkedList::new));
        schedulingPolicyTimingLogger.end("LP.notRequireLicenseOrLicenseExists");
        if (needPersist) {
            schedulingPolicyTimingLogger.start("LP.getOrderedTasks.persist");
            licenseSynchronization.persist();
            schedulingPolicyTimingLogger.end("LP.getOrderedTasks.persist");
            needPersist = false;
        }

        schedulingPolicyTimingLogger.end("LP.getOrderedTasks");
        schedulingPolicyTimingLogger.printTimings(Level.DEBUG);
        schedulingPolicyTimingLogger.clear();
        return filteredOrderedTasksDescFromParentPolicy;
    }

    @Override
    public void beforeIsTaskExecutable() {
        schedulingPolicyTimingLogger.clear();
    }

    @Override
    public void afterIsTaskExecutable() {
        schedulingPolicyTimingLogger.printTimings(Level.DEBUG);
        schedulingPolicyTimingLogger.clear();
    }

    /* A task is executable if it does not require any license or if it can get tokens */
    @Override
    public boolean isTaskExecutable(NodeSet selectedNodes, EligibleTaskDescriptor task) {

        schedulingPolicyTimingLogger.start("LP.isTaskExecutable");
        needPersist = false;
        try {
            // If it requires software licenses
            schedulingPolicyTimingLogger.start("LP.isTaskExecutable.getRequiredLicenses");
            String requiredLicenses = getRequiredLicenses(task);
            schedulingPolicyTimingLogger.end("LP.isTaskExecutable.getRequiredLicenses");
            String jobId = ((EligibleTaskDescriptorImpl) task).getInternal().getJobId().value();
            String taskId = task.getTaskId().toString();

            if (requiredLicenses != null) {

                logger.trace("Try to get tokens for licenses " + requiredLicenses);
                boolean taskHoldsToken, jobHoldsToken, canGetToken;
                final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split("\\s*,\\s*")));
                for (String requiredLicense : requiredLicensesSet) {

                    jobHoldsToken = licenseSynchronization.containsJobId(requiredLicense, jobId);
                    taskHoldsToken = licenseSynchronization.containsTaskId(requiredLicense, taskId);
                    schedulingPolicyTimingLogger.start("LP.isTaskExecutable.canGetToken");
                    canGetToken = canGetToken(requiredLicense);
                    schedulingPolicyTimingLogger.end("LP.isTaskExecutable.canGetToken");

                    logger.trace("For license " + requiredLicense + " task " + taskId + " holds a token? " +
                                 taskHoldsToken + " job " + jobId + " holds a token? " + jobHoldsToken +
                                 " a new token is available? " + canGetToken);

                    if (!jobHoldsToken && !taskHoldsToken && !canGetToken) {
                        logger.trace("Task and its job do not already hold a token for " + requiredLicense +
                                     " and cannot get a new one, keep task pending");
                        return false;
                    }
                }
                for (String requiredLicense : requiredLicensesSet) {

                    taskHoldsToken = licenseSynchronization.containsTaskId(requiredLicense, taskId);
                    jobHoldsToken = licenseSynchronization.containsJobId(requiredLicense, jobId);

                    logger.trace("For license " + requiredLicense + " taskHoldsToken? " + taskHoldsToken +
                                 " jobHoldsToken? " + jobHoldsToken);

                    if (taskHoldsToken || jobHoldsToken) {
                        logger.trace("The license " + requiredLicense +
                                     " is already obtained at the job level or the task level, use it without getting a new token");
                    } else {
                        logger.trace("Task and its job do not already hold a token for " + requiredLicense +
                                     " but acquiring a new one");
                        licenseSynchronization.addTaskToLicense(requiredLicense, taskId);
                        licenseSynchronization.markTaskLicenseChange(requiredLicense);
                    }
                }
                if (needPersist) {
                    schedulingPolicyTimingLogger.start("LP.isTaskExecutable.persist");
                    licenseSynchronization.persist();
                    schedulingPolicyTimingLogger.end("LP.isTaskExecutable.persist");
                    needPersist = false;
                }
                return true;
            } else {
                logger.trace("Executable since it does not require any license");
                return true;
            }
        } finally {
            schedulingPolicyTimingLogger.end("LP.isTaskExecutable");
        }
    }

}
