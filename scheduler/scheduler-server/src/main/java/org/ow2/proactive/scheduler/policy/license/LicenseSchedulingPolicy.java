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

    // A map of fixed size lists. A map entry per software license and a list per map entry to handle jobs using this software license.
    private Map<String, LinkedBlockingQueue<JobDescriptorImpl>> eligibleJobsDescriptorsLicenses = null;

    // A map of fixed size lists. A map entry per software license and a list per map entry to handle tasks using this software license.
    private Map<String, LinkedBlockingQueue<EligibleTaskDescriptor>> eligibleTasksDescriptorsLicenses = null;

    private Map<String, Integer> remainingTokens = null;

    // Will be initialize with the license properties file which includes the maximum licenses numbers per software
    private Properties properties = null;

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

                if (maxNbTokens > 0) {

                    eligibleJobsDescriptorsLicenses.put(software,
                                                        new LinkedBlockingQueue<JobDescriptorImpl>(maxNbTokens));
                    eligibleTasksDescriptorsLicenses.put(software,
                                                         new LinkedBlockingQueue<EligibleTaskDescriptor>(maxNbTokens));
                    remainingTokens.put(software, maxNbTokens);
                } else {
                    // In order to distinguish the case where there is no more token (all are used)
                    // and the case where it is set to <= 0 in the config file
                    remainingTokens.put(software, -1);
                }
            }

            logger.debug("eligibleJobsDescriptorsLicenses initialized: " + eligibleJobsDescriptorsLicenses);
            logger.debug("eligibleTasksDescriptorsLicenses initialized: " + eligibleTasksDescriptorsLicenses);
            logger.debug("remainingTokens initialized: " + remainingTokens);
        }
    }

    private boolean containsJob(final LinkedBlockingQueue<JobDescriptorImpl> jobList, final JobId jobId) {
        return jobList.stream().anyMatch(jd -> jd.getJobId().equals(jobId));
    }

    private void removeFinishedJobsAndReleaseTokens(String requiredLicense) {

        LinkedBlockingQueue<JobDescriptorImpl> eligibleJobsDescriptorsLicense = eligibleJobsDescriptorsLicenses.get(requiredLicense);

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

    private void removeFinishedTasksAndReleaseTokens(String requiredLicense) {
        LinkedBlockingQueue<EligibleTaskDescriptor> eligibleTasksDescriptorsLicense = eligibleTasksDescriptorsLicenses.get(requiredLicense);
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

    private boolean canGetToken(String currentRequiredLicense) {

        if (remainingTokens.get(currentRequiredLicense) == -1) {
            logger.debug("No token specified in the config file, return false");
            return false;
        } else if (remainingTokens.get(currentRequiredLicense) > 0) {
            logger.debug("There are still remaining licenses, return true");
            return true;
        } else { // == 0
            logger.debug("Removing all terminated jobs/tasks and check again if there are remaining licenses");
            removeFinishedJobsAndReleaseTokens(currentRequiredLicense);
            removeFinishedTasksAndReleaseTokens(currentRequiredLicense);

            if (remainingTokens.get(currentRequiredLicense) > 0) {
                logger.debug("There are remaining licenses after releasing tokens, return true");
                return true;
            }
            return false;
        }
    }

    private boolean acquireTokensForJob(JobDescriptorImpl job) {

        // Retrieve required licenses names from the job generic informations
        final String requiredLicenses = job.getInternal().getRuntimeGenericInformation().get(REQUIRED_LICENSES);

        // If it requires software licenses
        if (requiredLicenses != null) {

            logger.debug("Considering licenses " + requiredLicenses);

            // Do not execute if one of the required license can not be obtained
            final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split(",")));
            for (String requiredLicense : requiredLicensesSet) {

                if (!remainingTokens.containsKey(requiredLicense)) {
                    logger.debug("The required software is not specified in the license properties file, keep job pending");
                    return false;
                }

                if (!containsJob(eligibleJobsDescriptorsLicenses.get(requiredLicense), job.getJobId()) &&
                    !canGetToken(requiredLicense)) {
                    logger.debug("Token for " + requiredLicense + " not available, keep job pending");
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
            logger.debug("Eligible since it can get all tokens");
            return true;

        } else {
            logger.debug("Eligible since it does not require any license");
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
            logger.debug("Eligible since it does not require any license");
            return null;
        }
    }

    private boolean notRequireLicenseOrLicenseExists(EligibleTaskDescriptor task) {

        String requiredLicenses = getRequiredLicenses(task);

        // If it requires software licenses
        if (requiredLicenses != null) {

            logger.debug("Considering licenses " + requiredLicenses);

            // Do not execute if one of the required license can not be obtained
            final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split(",")));
            for (String requiredLicense : requiredLicensesSet) {

                if (!remainingTokens.containsKey(requiredLicense)) {
                    logger.debug("Not eligible since no token exists for this license");
                    return false;
                }
            }
            logger.debug("Eligible since tokens exist for this license");
            return true;

        } else {
            logger.debug("Eligible since it does not require any license");
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
        if (requiredLicenses != null) {

            logger.debug("Try to get tokens for licenses " + requiredLicenses);
            final HashSet<String> requiredLicensesSet = new HashSet<String>(Arrays.asList(requiredLicenses.split(",")));
            for (String requiredLicense : requiredLicensesSet) {

                if (!containsJob(eligibleJobsDescriptorsLicenses.get(requiredLicense),
                                 ((EligibleTaskDescriptorImpl) task).getInternal().getJobId()) &&
                    !canGetToken(requiredLicense)) {
                    logger.debug("Token for " + requiredLicense + " not available, keep task pending");
                    return false;
                }
            }
            logger.debug("Can be executed since all tokens are available for licenses " + requiredLicenses);
            for (String requiredLicense : requiredLicensesSet) {
                if (!containsJob(eligibleJobsDescriptorsLicenses.get(requiredLicense),
                                 ((EligibleTaskDescriptorImpl) task).getInternal().getJobId())) {

                    eligibleTasksDescriptorsLicenses.get(requiredLicense).add(task);
                    remainingTokens.put(requiredLicense, remainingTokens.get(requiredLicense) - 1);
                } else {
                    logger.debug("The license " + requiredLicense +
                                 " is already obtained at the job level, use it without getting a new token");
                }
            }
            return true;
        } else {
            logger.debug("Executable since it does not require any license");
            return true;
        }
    }

}
