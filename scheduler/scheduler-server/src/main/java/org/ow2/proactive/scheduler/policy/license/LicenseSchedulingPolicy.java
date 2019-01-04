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
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 *
 * This Policy is designed to manage software licenses.
 * When a task contains the generic information REQUIRE_LICENSES,
 * this policy will check if there is an available license per
 * software to return true
 *
 */
public class LicenseSchedulingPolicy extends ExtendedSchedulerPolicy {

    private static final Logger logger = Logger.getLogger(LicenseSchedulingPolicy.class);

    private static final String REQUIRED_LICENSES = "REQUIRED_LICENSES";

    // A map of fixed size lists. A map entry per software license and a list per map entry to handle jobs using this software license.
    private Map<String, LinkedBlockingQueue<JobDescriptorImpl>> eligibleJobsDescriptorsLicenses = null;

    // A map of fixed size lists. A map entry per software license and a list per map entry to handle tasks using this software license.
    private Map<String, LinkedBlockingQueue<EligibleTaskDescriptor>> eligibleTasksDescriptorsLicenses = null;

    private Map<String, Integer> remainingTokens = null;

    // Will be initialize with the license properties file which includes the maximum licenses numbers per software
    private Properties properties = null;

    @Override
    public LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobDescList) {

        initialize();

        // Keep only executable jobs according to their required licenses
        List<JobDescriptor> filteredJobDescList = jobDescList.stream()
                                                             .filter(jobDesc -> getTokensAndReturnTrue((JobDescriptorImpl) jobDesc))
                                                             .collect(Collectors.toList());

        // Get the ordered tasks from the filtered jobs according to the parent policy
        LinkedList<EligibleTaskDescriptor> orderedTasksDescFromParentPolicy = super.getOrderedTasks(filteredJobDescList);

        // Keep only executable tasks according to their required licenses
        LinkedList<EligibleTaskDescriptor> filteredOrderedTasksDescFromParentPolicy = orderedTasksDescFromParentPolicy.stream()
                                                                                                                      .filter(taskDesc -> getTokensAndReturnTrue(taskDesc))
                                                                                                                      .collect(Collectors.toCollection(LinkedList::new));

        return filteredOrderedTasksDescFromParentPolicy;
    }

    private void initialize() {

        if (remainingTokens == null) {

            logger.debug("Retrieve properties from the license properties file");
            properties = LicenseConfiguration.getConfiguration().getProperties();

            logger.debug("Initializing maps");
            eligibleJobsDescriptorsLicenses = new HashMap<String, LinkedBlockingQueue<JobDescriptorImpl>>(properties.size());
            eligibleTasksDescriptorsLicenses = new HashMap<String, LinkedBlockingQueue<EligibleTaskDescriptor>>(properties.size());
            remainingTokens = new HashMap<String, Integer>(properties.size());

            Enumeration propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String software = (String) propertyNames.nextElement();
                int maxNbTokens = Integer.parseInt(properties.getProperty(software));
                eligibleJobsDescriptorsLicenses.put(software, new LinkedBlockingQueue<JobDescriptorImpl>(maxNbTokens));
                eligibleTasksDescriptorsLicenses.put(software,
                                                     new LinkedBlockingQueue<EligibleTaskDescriptor>(maxNbTokens));
                remainingTokens.put(software, maxNbTokens);
            }

            logger.debug("eligibleJobsDescriptorsLicenses initialized: " + eligibleJobsDescriptorsLicenses);
            logger.debug("eligibleTasksDescriptorsLicenses initialized: " + eligibleTasksDescriptorsLicenses);
            logger.debug("remainingTokens initialized: " + remainingTokens);
        }
    }

    private boolean containsJob(final LinkedBlockingQueue<JobDescriptorImpl> jobList, final JobId jobId) {
        return jobList.stream().anyMatch(jd -> jd.getJobId().equals(jobId));
    }

    private void removeFinishedJobsAndReleaseTokens(
            LinkedBlockingQueue<JobDescriptorImpl> eligibleJobsDescriptorsLicense, String requiredLicense) {
        Iterator<JobDescriptorImpl> iter = eligibleJobsDescriptorsLicense.iterator();
        JobDescriptorImpl currentJob;
        int currentNbTokens = remainingTokens.get(requiredLicense);
        while (iter.hasNext()) {
            currentJob = iter.next();

            if (!currentJob.getInternal().getStatus().isJobAlive()) {
                logger.debug("releasing token for license " + requiredLicense + " given to job " +
                             currentJob.getJobId());
                iter.remove();
                currentNbTokens++;
            }
        }
        remainingTokens.put(requiredLicense, currentNbTokens);
    }

    private boolean canGetTokenFromJobs(String currentRequiredLicense) {

        if (!eligibleJobsDescriptorsLicenses.containsKey(currentRequiredLicense)) {
            logger.debug("The required software is not specified in the license properties file, return false");
            return false;
        }

        LinkedBlockingQueue<JobDescriptorImpl> eligibleJobsDescriptorsLicense = eligibleJobsDescriptorsLicenses.get(currentRequiredLicense);
        if (remainingTokens.get(currentRequiredLicense) > 0) {
            logger.debug("There are still remaining licenses, return true");
            return true;
        } else {
            logger.debug("Removing all terminated jobs and check again if there are remaining licenses");
            removeFinishedJobsAndReleaseTokens(eligibleJobsDescriptorsLicense, currentRequiredLicense);

            if (remainingTokens.get(currentRequiredLicense) > 0) {
                logger.debug("There are remaining licenses after releasing tokens, return true");
                return true;
            }
            return false;
        }
    }

    private boolean getTokensAndReturnTrue(JobDescriptorImpl job) {

        logger.debug("Analysing job: " + ((JobDescriptorImpl) job).getInternal().getName());

        // Retrieve required licenses names from the job generic informations
        final String requiredLicenses = ((JobDescriptorImpl) job).getInternal()
                                                                 .getRuntimeGenericInformation()
                                                                 .get(REQUIRED_LICENSES);

        // If it requires software licenses
        if (requiredLicenses != null) {

            logger.debug("Need for job to check licenses " + requiredLicenses);

            // Do not execute if one of the required license can not be obtained
            final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split(",")));
            for (String requiredLicense : requiredLicensesSet) {

                logger.debug("Considering license " + requiredLicense);

                if (containsJob(eligibleJobsDescriptorsLicenses.get(requiredLicense), job.getJobId())) {
                    logger.debug("The job already hold a token, no need to try to get a new one");
                } else if (!canGetTokenFromJobs(requiredLicense) && !canGetTokenFromTasks(requiredLicense)) {
                    logger.debug("License for " + requiredLicense + " not available, keep job pending");
                    return false;
                }
            }
            // Can get a new token for each required license !
            for (String requiredLicense : requiredLicensesSet) {
                if (!containsJob(eligibleJobsDescriptorsLicenses.get(requiredLicense), job.getJobId())) {
                    eligibleJobsDescriptorsLicenses.get(requiredLicense).add(job);
                    remainingTokens.put(requiredLicense, remainingTokens.get(requiredLicense) - 1);
                }
            }
            logger.debug("All licenses are available, executing job");
            return true;

        } else {
            return true;
        }

    }

    private void removeFinishedTasksAndReleaseTokens(
            LinkedBlockingQueue<EligibleTaskDescriptor> eligibleTasksDescriptorsLicense, String requiredLicense) {
        Iterator<EligibleTaskDescriptor> iter = eligibleTasksDescriptorsLicense.iterator();
        EligibleTaskDescriptorImpl currentTask;
        int currentNbTokens = remainingTokens.get(requiredLicense);
        while (iter.hasNext()) {
            currentTask = (EligibleTaskDescriptorImpl) iter.next();

            if (!currentTask.getInternal().isTaskAlive()) {
                logger.debug("releasing token for license " + requiredLicense + " given to task " +
                             currentTask.getTaskId() + " of job " + currentTask.getJobId());
                iter.remove();
                currentNbTokens++;
            }
        }
        remainingTokens.put(requiredLicense, currentNbTokens);
    }

    private boolean canGetTokenFromTasks(String currentRequiredLicense) {

        if (!eligibleTasksDescriptorsLicenses.containsKey(currentRequiredLicense)) {
            logger.debug("The required software is not specified in the license properties file, return false");
            return false;
        }

        LinkedBlockingQueue<EligibleTaskDescriptor> eligibleTasksDescriptorsLicense = eligibleTasksDescriptorsLicenses.get(currentRequiredLicense);
        if (remainingTokens.get(currentRequiredLicense) > 0) {
            logger.debug("There are still remaining licenses, return true");
            return true;
        } else {
            logger.debug("Removing all terminated tasks and check again if there are remaining licenses");
            removeFinishedTasksAndReleaseTokens(eligibleTasksDescriptorsLicense, currentRequiredLicense);

            if (remainingTokens.get(currentRequiredLicense) > 0) {
                logger.debug("There are remaining licenses after releasing tokens, return true");
                return true;
            }
            return false;
        }
    }

    private boolean getTokensAndReturnTrue(EligibleTaskDescriptor task) {

        logger.debug("Analysing task: " + ((EligibleTaskDescriptorImpl) task).getInternal().getName());

        // Do not consider the runtime GIs to test if REQUIRED_LICENSES is specified at the task level
        // since these inherit from the job level ones
        // And if REQUIRED_LICENSES is specified at the task level (i.e. in the unresolved GIs)
        // Consider the runtime GIs to get the resolved REQUIRED_LICENSES
        String requiredLicenses = null;
        final InternalTask internTask = ((EligibleTaskDescriptorImpl) task).getInternal();
        if (internTask.getGenericInformation().get(REQUIRED_LICENSES) == null)
            return true;
        else {
            // Retrieve required licenses names from the task generic informations
            requiredLicenses = internTask.getRuntimeGenericInformation().get(REQUIRED_LICENSES);
        }

        // If it requires software licenses
        if (requiredLicenses != null) {

            logger.debug("Need for task to check licenses " + requiredLicenses);

            // Do not execute if one of the required license can not be obtained
            final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split(",")));
            for (String requiredLicense : requiredLicensesSet) {

                logger.debug("Considering license " + requiredLicense);

                if (containsJob(eligibleJobsDescriptorsLicenses.get(requiredLicense),
                                ((EligibleTaskDescriptorImpl) task).getInternal().getJobId())) {
                    logger.debug("The task job already hold a token, no need to try to get a new one");
                } else if (!canGetTokenFromJobs(requiredLicense) && !canGetTokenFromTasks(requiredLicense)) {
                    logger.debug("License for " + requiredLicense + " not available, keep task pending");
                    return false;
                }
            }
            // Can get a new token for each required license !
            for (String requiredLicense : requiredLicensesSet) {
                // If the license is already obtained at the job level, use it without getting a new one
                if (!containsJob(eligibleJobsDescriptorsLicenses.get(requiredLicense),
                                 ((EligibleTaskDescriptorImpl) task).getInternal().getJobId())) {
                    eligibleTasksDescriptorsLicenses.get(requiredLicense).add(task);
                    remainingTokens.put(requiredLicense, remainingTokens.get(requiredLicense) - 1);
                }
            }
            logger.debug("All licenses are available, executing task");
            return true;

        } else {
            return true;
        }

    }

}
