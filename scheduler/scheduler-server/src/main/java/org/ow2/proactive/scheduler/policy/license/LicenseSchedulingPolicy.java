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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

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

    public static final String REQUIRED_LICENSES = "REQUIRED_LICENSES";

    // A map of fixed size lists. A map entry per software license and a list per map entry to handle tasks using this software license.
    private Map<String, FixedSizeArrayList> eligibleTasksDescriptorsLicenses = null;

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

            // Initialize eligibleTasksDescriptorsLicenses
            if (eligibleTasksDescriptorsLicenses == null)
                eligibleTasksDescriptorsLicenses = new HashMap<String, FixedSizeArrayList>();

            // Retrieve the maximum licenses numbers per software from the configuration file
            final Properties properties = LicenseConfiguration.getConfiguration().getProperties();

            logger.debug("Need to check licenses with " + properties.toString());

            // Iterate over required software licenses
            final String[] requiredLicensesArray = requiredLicenses.split(",");
            for (int i = 0; i < requiredLicensesArray.length; i++) {

                // Retrieve the current software license name and its available number
                final String currentRequiredLicense = requiredLicensesArray[i];
                final int numberOfLicenses = Integer.parseInt(properties.getProperty(currentRequiredLicense));

                // Per software license, create a list handling all tasks using a this software license
                if (!eligibleTasksDescriptorsLicenses.containsKey(currentRequiredLicense)) {
                    eligibleTasksDescriptorsLicenses.put(currentRequiredLicense,
                                                         new FixedSizeArrayList(numberOfLicenses));
                }

                // To be executed (ie return true), a task must get a license per requiring software license
                if (!canGetLicense(task, currentRequiredLicense, numberOfLicenses)) {
                    logger.debug("License for " + currentRequiredLicense + " not available, returning false");
                    return false;
                }
            }
            // Here we manage to give a license for all required software licenses
            for (int i = 0; i < requiredLicensesArray.length; i++) {
                eligibleTasksDescriptorsLicenses.get(requiredLicensesArray[i]).add(task);
            }
            logger.debug("All licenses are available, returning true");
            return true;

        } else {
            return true;
        }

    }

    private boolean canGetLicense(EligibleTaskDescriptor task, String currentRequiredLicense, int numberOfLicenses) {

        FixedSizeArrayList eligibleTasksDescriptorsLicense = eligibleTasksDescriptorsLicenses.get(currentRequiredLicense);

        // if the maximum number of license is not reached, return true
        if (!eligibleTasksDescriptorsLicense.isLimitReached()) {
            return true;
        }
        // otherwise remove all terminated tasks and check again if the limit is reached
        else {
            Iterator<Object> iter = eligibleTasksDescriptorsLicense.iterator();
            EligibleTaskDescriptorImpl current_task;
            while (iter.hasNext()) {
                current_task = (EligibleTaskDescriptorImpl) iter.next();

                if (current_task.getInternal().getStatus().equals(TaskStatus.FINISHED)) {
                    iter.remove();
                }
            }

            if (!eligibleTasksDescriptorsLicense.isLimitReached()) {
                return true;
            }
            return false;
        }
    }

}
