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

    // Will be initialize with the license properties file which includes the maximum licenses numbers per software
    private Properties properties = null;

    @Override
    public LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobDescList) {

        // Keep only executable jobs according to their required licenses
        List<JobDescriptor> filteredJobDescList = jobDescList.stream()
                                                             .filter(jobDesc -> isJobExecutableUsingLicense((JobDescriptorImpl) jobDesc))
                                                             .collect(Collectors.toList());

        // Get the ordered tasks from the filtered jobs according to the parent policy
        LinkedList<EligibleTaskDescriptor> orderedTasksDescFromParentPolicy = super.getOrderedTasks(filteredJobDescList);

        // Keep only executable tasks according to their required licenses
        LinkedList<EligibleTaskDescriptor> filteredOrderedTasksDescFromParentPolicy = orderedTasksDescFromParentPolicy.stream()
                                                                                                                      .filter(taskDesc -> isTaskExecutableUsingLicense(taskDesc))
                                                                                                                      .collect(Collectors.toCollection(LinkedList::new));

        return filteredOrderedTasksDescFromParentPolicy;
    }

    private void initializeJobsLicenses() {
        if (eligibleJobsDescriptorsLicenses == null) {
            // Map initialization
            eligibleJobsDescriptorsLicenses = new HashMap<String, LinkedBlockingQueue<JobDescriptorImpl>>();
            // Retrieve properties from the license properties file ...
            properties = LicenseConfiguration.getConfiguration().getProperties();
            // ... and per software license, create a list handling all jobs using a this software license
            Enumeration e = properties.propertyNames();
            while (e.hasMoreElements()) {
                String software = (String) e.nextElement();
                int numberOfLicenses = Integer.parseInt(properties.getProperty(software));
                eligibleJobsDescriptorsLicenses.put(software,
                                                    new LinkedBlockingQueue<JobDescriptorImpl>(numberOfLicenses));
            }
        }
    }

    private boolean containsJob(final LinkedBlockingQueue<JobDescriptorImpl> jobList, final JobId jobId) {
        return jobList.stream().anyMatch(jd -> jd.getJobId().equals(jobId));
    }

    private void initializeTasksLicenses() {
        if (eligibleTasksDescriptorsLicenses == null) {
            // Map initialization
            eligibleTasksDescriptorsLicenses = new HashMap<String, LinkedBlockingQueue<EligibleTaskDescriptor>>();
            // Retrieve properties from the license properties file ...
            properties = LicenseConfiguration.getConfiguration().getProperties();
            // ... and per software license, create a list handling all tasks using a this software license
            Enumeration e = properties.propertyNames();
            while (e.hasMoreElements()) {
                String software = (String) e.nextElement();
                int numberOfLicenses = Integer.parseInt(properties.getProperty(software));
                eligibleTasksDescriptorsLicenses.put(software,
                                                     new LinkedBlockingQueue<EligibleTaskDescriptor>(numberOfLicenses));
            }
        }
    }

    private boolean isJobExecutableUsingLicense(JobDescriptorImpl job) {

        logger.debug("Analysing job: " + ((JobDescriptorImpl) job).getInternal().getName());

        // Retrieve required licenses names from the job generic informations
        final String requiredLicenses = ((JobDescriptorImpl) job).getInternal()
                                                                 .getRuntimeGenericInformation()
                                                                 .get(REQUIRED_LICENSES);

        // If it requires software licenses
        if (requiredLicenses != null) {

            initializeJobsLicenses();
            logger.debug("Need for job to check licenses " + requiredLicenses + " with " +
                         eligibleJobsDescriptorsLicenses);

            // Do not execute if one of the required license can not be obtained
            final String[] requiredLicensesArray = requiredLicenses.split(",");
            final int nbRequiredLicenses = requiredLicensesArray.length;
            for (int i = 0; i < nbRequiredLicenses; i++) {

                final String currentRequiredLicense = requiredLicensesArray[i];
                if (!canGetJobLicense(currentRequiredLicense, job.getJobId())) {
                    logger.debug("License for " + currentRequiredLicense + " not available, keep job pending");
                    return false;
                }
            }
            // Can be executed! Give it all required software licenses
            for (int i = 0; i < nbRequiredLicenses; i++) {
                if (!containsJob(eligibleJobsDescriptorsLicenses.get(requiredLicensesArray[i]), job.getJobId())) {
                    eligibleJobsDescriptorsLicenses.get(requiredLicensesArray[i]).add(job);
                }
            }
            logger.debug("All licenses are available, executing job");
            return true;

        } else {
            return true;
        }

    }

    private boolean isTaskExecutableUsingLicense(EligibleTaskDescriptor task) {

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

            initializeTasksLicenses();
            logger.debug("Need for task to check licenses " + requiredLicenses + " with " +
                         eligibleTasksDescriptorsLicenses);

            // Do not execute if one of the required license can not be obtained
            final String[] requiredLicensesArray = requiredLicenses.split(",");
            final int nbRequiredLicenses = requiredLicensesArray.length;
            boolean[] jobAlreadyUseLicense = new boolean[nbRequiredLicenses];

            for (int i = 0; i < nbRequiredLicenses; i++) {

                final String currentRequiredLicense = requiredLicensesArray[i];

                // If the license is already obtained at the job level, use it without getting a new one
                LinkedBlockingQueue<JobDescriptorImpl> eligibleJobsDescriptorsLicense = eligibleJobsDescriptorsLicenses.get(currentRequiredLicense);
                if (containsJob(eligibleJobsDescriptorsLicense,
                                ((EligibleTaskDescriptorImpl) task).getInternal().getJobId())) {
                    jobAlreadyUseLicense[i] = true;
                    continue;
                }

                if (!canGetTaskLicense(currentRequiredLicense)) {
                    logger.debug("License for " + currentRequiredLicense + " not available, keep task pending");
                    return false;
                }
            }
            // Can be executed! Give it all required software licenses
            for (int i = 0; i < nbRequiredLicenses; i++) {
                if (!jobAlreadyUseLicense[i])
                    eligibleTasksDescriptorsLicenses.get(requiredLicensesArray[i]).add(task);
            }
            logger.debug("All licenses are available, executing task");
            return true;

        } else {
            return true;
        }

    }

    private void removeFinishedJobs(LinkedBlockingQueue<JobDescriptorImpl> eligibleJobsDescriptorsLicense) {
        Iterator<JobDescriptorImpl> iter = eligibleJobsDescriptorsLicense.iterator();
        JobDescriptorImpl currentJob;
        while (iter.hasNext()) {
            currentJob = iter.next();

            if (!currentJob.getInternal().getStatus().isJobAlive()) {
                iter.remove();
            }
        }
    }

    private boolean canGetJobLicense(String currentRequiredLicense, JobId jobId) {

        // If the required software is not specified in the license properties file
        if (!eligibleJobsDescriptorsLicenses.containsKey(currentRequiredLicense))
            return false;

        // if there are still remaining licenses return true
        LinkedBlockingQueue<JobDescriptorImpl> eligibleJobsDescriptorsLicense = eligibleJobsDescriptorsLicenses.get(currentRequiredLicense);
        if (containsJob(eligibleJobsDescriptorsLicense, jobId)) {
            return true;
        }
        if (eligibleJobsDescriptorsLicense.remainingCapacity() > 0) {
            return true;
        }
        // otherwise remove all terminated jobs and check again if there are remaining licenses
        else {
            removeFinishedJobs(eligibleJobsDescriptorsLicense);

            if (eligibleJobsDescriptorsLicense.remainingCapacity() > 0) {
                return true;
            }
            return false;
        }
    }

    private void removeFinishedTasks(LinkedBlockingQueue<EligibleTaskDescriptor> eligibleTasksDescriptorsLicense) {
        Iterator<EligibleTaskDescriptor> iter = eligibleTasksDescriptorsLicense.iterator();
        EligibleTaskDescriptorImpl currentTask;
        while (iter.hasNext()) {
            currentTask = (EligibleTaskDescriptorImpl) iter.next();

            if (!currentTask.getInternal().isTaskAlive()) {
                iter.remove();
            }
        }
    }

    private boolean canGetTaskLicense(String currentRequiredLicense) {

        // If the required software is not specified in the license properties file
        if (!eligibleTasksDescriptorsLicenses.containsKey(currentRequiredLicense))
            return false;

        // if there are still remaining licenses return true
        LinkedBlockingQueue<EligibleTaskDescriptor> eligibleTasksDescriptorsLicense = eligibleTasksDescriptorsLicenses.get(currentRequiredLicense);
        if (eligibleTasksDescriptorsLicense.remainingCapacity() > 0) {
            return true;
        }
        // otherwise remove all terminated tasks and check again if there are remaining licenses
        else {
            removeFinishedTasks(eligibleTasksDescriptorsLicense);

            if (eligibleTasksDescriptorsLicense.remainingCapacity() > 0) {
                return true;
            }
            return false;
        }
    }

}
