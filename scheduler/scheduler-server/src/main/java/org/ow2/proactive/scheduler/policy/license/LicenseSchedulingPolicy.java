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

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
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

    private void initialize() {

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
                    logger.debug("Fatal error, cannot parse token number from the license properties file");
                    throw e;
                }

                if (nbTokens > 0) {
                    licenseSynchronization.addSoftware(software, nbTokens);
                } else {
                    // In order to distinguish the case where there is no more token (all are used)
                    // and the case where it is set to <= 0 in the config file
                    licenseSynchronization.addSoftware(software, -1);
                }
            }
            licenseSynchronization.persist();
        }
    }

    private void removeFinishedJobsAndReleaseTokens(String requiredLicense) {

        LinkedBlockingQueue<String> eligibleJobsDescriptorsLicense = licenseSynchronization.getPersistedJobsLicense(requiredLicense);

        Iterator<String> iter = eligibleJobsDescriptorsLicense.iterator();
        String currentJobIdAsString;
        JobId currentJobId;
        JobStatus currentJobStatus;
        int currentNbTokens = licenseSynchronization.getRemainingTokens(requiredLicense);
        while (iter.hasNext()) {

            currentJobIdAsString = iter.next();
            currentJobId = JobIdImpl.makeJobId(currentJobIdAsString);
            currentJobStatus = schedulingService.getJobStatus(currentJobId);
            logger.debug("Considering job " + currentJobIdAsString + " with status " + currentJobStatus +
                         " requiring token for license " + requiredLicense);

            if (!schedulingService.isJobAlive(currentJobId) || currentJobStatus == JobStatus.PAUSED ||
                currentJobStatus == JobStatus.IN_ERROR) {
                logger.debug("Releasing token");
                iter.remove();
                currentNbTokens++;
            } else {
                logger.debug("Job status does not allow to release a token");
            }
        }
        licenseSynchronization.setNbTokens(requiredLicense, currentNbTokens);
        licenseSynchronization.markJobLicenseChange(requiredLicense);
        licenseSynchronization.persist();
    }

    private void removeFinishedTasksAndReleaseTokens(String requiredLicense) {

        LinkedBlockingQueue<String> eligibleTasksDescriptorsLicense = licenseSynchronization.getPersistedTasksLicense(requiredLicense);

        Iterator<String> iter = eligibleTasksDescriptorsLicense.iterator();
        String currentTaskIdAsString;
        TaskId currentTaskId;
        TaskStatus currentTaskStatus;
        int currentNbTokens = licenseSynchronization.getRemainingTokens(requiredLicense);
        while (iter.hasNext()) {
            currentTaskIdAsString = iter.next();
            currentTaskId = TaskIdImpl.makeTaskId(currentTaskIdAsString);
            currentTaskStatus = schedulingService.getTaskStatus(currentTaskId);
            logger.debug("Considering task " + currentTaskIdAsString + " with status " + currentTaskStatus +
                         " requiring token for license " + requiredLicense);

            if (!schedulingService.isTaskAlive(currentTaskId) || currentTaskStatus == TaskStatus.PAUSED ||
                currentTaskStatus == TaskStatus.IN_ERROR) {
                logger.debug("Releasing token");
                iter.remove();
                currentNbTokens++;
            } else {
                logger.debug("Task status does not allow to release a token");
            }
        }
        licenseSynchronization.setNbTokens(requiredLicense, currentNbTokens);
        licenseSynchronization.markTaskLicenseChange(requiredLicense);
        licenseSynchronization.persist();
    }

    private boolean canGetToken(String currentRequiredLicense) {

        if (licenseSynchronization.getRemainingTokens(currentRequiredLicense) == -1) {
            logger.debug("No token specified in the config file, return false");
            return false;
        } else if (licenseSynchronization.getRemainingTokens(currentRequiredLicense) > 0) {
            logger.debug("There are still remaining licenses, return true");
            return true;
        } else { // == 0
            logger.debug("Removing all terminated jobs/tasks and check again if there are remaining licenses");
            removeFinishedJobsAndReleaseTokens(currentRequiredLicense);
            removeFinishedTasksAndReleaseTokens(currentRequiredLicense);

            if (licenseSynchronization.getRemainingTokens(currentRequiredLicense) > 0) {
                logger.debug("There are remaining licenses after releasing tokens, return true");
                return true;
            }
            return false;
        }
    }

    private boolean acquireTokensForJob(JobDescriptorImpl job) {

        // Retrieve required licenses names from the job generic informations
        final String requiredLicenses = job.getInternal().getRuntimeGenericInformation().get(REQUIRED_LICENSES);
        final String jobId = job.getJobId().value();

        // If it requires software licenses
        if (requiredLicenses != null) {

            logger.debug("For job considering licenses " + requiredLicenses);

            // Do not execute if one of the required license can not be obtained
            final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split("\\s*,\\s*")));
            boolean jobHoldsToken, canGetToken;
            for (String requiredLicense : requiredLicensesSet) {

                if (!licenseSynchronization.containsLicense(requiredLicense)) {
                    logger.debug("The required software is not specified in the license properties file, keep job pending");
                    return false;
                }

                jobHoldsToken = licenseSynchronization.containsJobId(requiredLicense, jobId);
                canGetToken = canGetToken(requiredLicense);
                if (!jobHoldsToken && !canGetToken) {
                    logger.debug("Job does not already hold a token for " + requiredLicense +
                                 " and cannot get a new one, keep job pending");
                    return false;
                }
            }
            logger.debug("Job eligible since it can get all tokens or already hold them");
            for (String requiredLicense : requiredLicensesSet) {
                if (!licenseSynchronization.containsJobId(requiredLicense, jobId)) {
                    licenseSynchronization.addJobToLicense(requiredLicense, jobId);
                    licenseSynchronization.markJobLicenseChange(requiredLicense);
                }
            }
            licenseSynchronization.persist();
            return true;

        } else {
            logger.debug("Job eligible since it does not require any license");
            return true;
        }

    }

    private String getRequiredLicenses(EligibleTaskDescriptor task) {
        // Do not consider the runtime GIs to test if REQUIRED_LICENSES is specified at the task level
        // since these inherit from the job level ones
        // And if REQUIRED_LICENSES is specified at the task level (i.e. in the unresolved GIs)
        // Consider the runtime GIs to get the resolved REQUIRED_LICENSES
        final InternalTask internTask = ((EligibleTaskDescriptorImpl) task).getInternal();
        if (internTask.getGenericInformation().get(REQUIRED_LICENSES) != null) {
            // Retrieve required licenses names from the task generic informations
            return internTask.getRuntimeGenericInformation().get(REQUIRED_LICENSES);
        } else {
            logger.debug("Task eligible since it does not require any license");
            return null;
        }
    }

    private boolean notRequireLicenseOrLicenseExists(EligibleTaskDescriptor task) {

        String requiredLicenses = getRequiredLicenses(task);

        // If it requires software licenses
        if (requiredLicenses != null) {

            logger.debug("For task considering licenses " + requiredLicenses);

            // Do not execute if one of the required license can not be obtained
            final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split("\\s*,\\s*")));
            for (String requiredLicense : requiredLicensesSet) {

                if (!licenseSynchronization.containsLicense(requiredLicense)) {
                    logger.debug("Task not eligible since no token exists for this license");
                    return false;
                }
            }
            logger.debug("Task eligible since all required licenses are specified in the properties file");
            return true;

        } else {
            logger.debug("Task eligible since it does not require any license");
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

        // Keep only executable jobs according to their required license tokens
        List<JobDescriptor> filteredJobDescList = jobDescList.stream()
                                                             .filter(jobDesc -> acquireTokensForJob((JobDescriptorImpl) jobDesc))
                                                             .collect(Collectors.toList());

        // Retrieve the ordered tasks from the filtered jobs according to the parent policy
        LinkedList<EligibleTaskDescriptor> orderedTasksDescFromParentPolicy = super.getOrderedTasks(filteredJobDescList);

        // Keep only tasks which, either does not require license, or require licenses specified in the config file (without considering tokens)
        LinkedList<EligibleTaskDescriptor> filteredOrderedTasksDescFromParentPolicy = orderedTasksDescFromParentPolicy.stream()
                                                                                                                      .filter(taskDesc -> notRequireLicenseOrLicenseExists(taskDesc))
                                                                                                                      .collect(Collectors.toCollection(LinkedList::new));

        return filteredOrderedTasksDescFromParentPolicy;
    }

    /* A task is executable if it does not require any license or if it can get tokens */
    @Override
    public boolean isTaskExecutable(NodeSet selectedNodes, EligibleTaskDescriptor task) {

        // If it requires software licenses
        String requiredLicenses = getRequiredLicenses(task);
        String jobId = ((EligibleTaskDescriptorImpl) task).getInternal().getJobId().value();
        String taskId = task.getTaskId().toString();

        if (requiredLicenses != null) {

            logger.debug("Try to get tokens for licenses " + requiredLicenses);
            boolean taskHoldsToken, jobHoldsToken, canGetToken;
            final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split("\\s*,\\s*")));
            for (String requiredLicense : requiredLicensesSet) {

                jobHoldsToken = licenseSynchronization.containsJobId(requiredLicense, jobId);
                taskHoldsToken = licenseSynchronization.containsTaskId(requiredLicense, taskId);
                canGetToken = canGetToken(requiredLicense);

                logger.debug("For license " + requiredLicense + " task " + taskId + " holds a token? " +
                             taskHoldsToken + " job " + jobId + " holds a token? " + jobHoldsToken +
                             " a new token is available? " + canGetToken);

                if (!jobHoldsToken && !taskHoldsToken && !canGetToken) {
                    logger.debug("Task and its job do not already hold a token for " + requiredLicense +
                                 " and cannot get a new one, keep task pending");
                    return false;
                }
            }
            for (String requiredLicense : requiredLicensesSet) {

                taskHoldsToken = licenseSynchronization.containsTaskId(requiredLicense, taskId);
                jobHoldsToken = licenseSynchronization.containsJobId(requiredLicense, jobId);

                logger.debug("For license " + requiredLicense + " taskHoldsToken? " + taskHoldsToken +
                             " jobHoldsToken? " + jobHoldsToken);

                if (taskHoldsToken || jobHoldsToken) {
                    logger.debug("The license " + requiredLicense +
                                 " is already obtained at the job level or the task level, use it without getting a new token");
                } else {
                    logger.debug("Task and its job do not already hold a token for " + requiredLicense +
                                 " but acquiring a new one");
                    licenseSynchronization.addTaskToLicense(requiredLicense, taskId);
                    licenseSynchronization.markTaskLicenseChange(requiredLicense);
                }
            }
            licenseSynchronization.persist();
            return true;
        } else {
            logger.debug("Executable since it does not require any license");
            return true;
        }
    }

}
