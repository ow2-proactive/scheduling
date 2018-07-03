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

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.utils.NodeSet;


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

    // A map of fixed size lists. A map entry per software license and a list per map entry to handle tasks using this software license.
    private static Map<String, LinkedBlockingQueue<EligibleTaskDescriptor>> eligibleTasksDescriptorsLicenses = null;

    // Will be initialize with the license properties file which includes the maximum licenses numbers per software
    private static Properties properties = null;

    private static void initialize() {
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

    @Override
    public boolean isTaskExecutable(NodeSet selectedNodes, EligibleTaskDescriptor task) {

        logger.debug("Selected Nodes: " + selectedNodes);

        logger.debug("Analysing task: " + ((EligibleTaskDescriptorImpl) task).getInternal().getName());

        // Retrieve required licenses names from the task generic informations
        final String requiredLicenses = ((EligibleTaskDescriptorImpl) task).getInternal()
                                                                           .getRuntimeGenericInformation()
                                                                           .get(REQUIRED_LICENSES);

        // If it requires software licenses
        if (requiredLicenses != null) {

            initialize();
            logger.debug("Need to check licenses with " + properties.toString());

            // Iterate over required software licenses
            final String[] requiredLicensesArray = requiredLicenses.split(",");
            for (int i = 0; i < requiredLicensesArray.length; i++) {
                // Retrieve the current software license name and its available number
                final String currentRequiredLicense = requiredLicensesArray[i];

                // To be executed (ie return true), a task must get a license per requiring software license
                if (!canGetLicense(currentRequiredLicense)) {
                    logger.debug("License for " + currentRequiredLicense + " not available, keep task pending");
                    return false;
                }
            }
            // Here we manage to give a license for all required software licenses
            for (int i = 0; i < requiredLicensesArray.length; i++) {
                eligibleTasksDescriptorsLicenses.get(requiredLicensesArray[i]).add(task);
            }
            logger.debug("All licenses are available, executing task");
            return true;

        } else {
            return true;
        }

    }

    private void removeFinishedTasks(LinkedBlockingQueue<EligibleTaskDescriptor> eligibleTasksDescriptorsLicense) {
        Iterator<EligibleTaskDescriptor> iter = eligibleTasksDescriptorsLicense.iterator();
        EligibleTaskDescriptorImpl current_task;
        while (iter.hasNext()) {
            current_task = (EligibleTaskDescriptorImpl) iter.next();

            if (!current_task.getInternal().isTaskAlive()) {
                iter.remove();
            }
        }
    }

    private boolean canGetLicense(String currentRequiredLicense) {

        // If the required software is not specified in the license properties file
        if (!eligibleTasksDescriptorsLicenses.containsKey(currentRequiredLicense))
            return false;

        LinkedBlockingQueue<EligibleTaskDescriptor> eligibleTasksDescriptorsLicense = eligibleTasksDescriptorsLicenses.get(currentRequiredLicense);
        // if there are still remaining licenses return true
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
