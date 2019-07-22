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
package org.ow2.proactive.scheduler.util;

import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * Calculates the task properties based on global configuration and task parameter.
 */
public class TaskConfiguration {
    // When the fork mode is neither specified in global config, nor in task property, it is set to true.
    public static final boolean DEFAULT_TASK_FORK = true;

    /**
     * Returns whether the task should be run in a forked JVM, depending on the global configuration and task property.
     * The global configuration has a higher priority than the task property; And runasme true implies fork true.
     *
     * @return whether the task should be run in a forked JVM.
     */
    public static boolean isForkingTask(Task task) {
        if (PASchedulerProperties.TASK_RUNASME.getValueAsBoolean()) {
            return true;
        } else if (getGlobalConfigureFork() != null) {
            return getGlobalConfigureFork();
        } else if (task.isRunAsMe()) {
            return true;
        } else if (task.isFork() != null) {
            return task.isFork();
        } else {
            return DEFAULT_TASK_FORK;
        }
    }

    /**
     * Returns whether the task should be run under the job owner user identity
     *
     * @return whether the task should be run under the job owner user identity
     */
    public static boolean isRunAsMeTask(Task task) {
        if (Boolean.FALSE.equals(getGlobalConfigureFork())) {
            return false;
        } else {
            return PASchedulerProperties.TASK_RUNASME.getValueAsBoolean() || task.isRunAsMe();
        }
    }

    public static Boolean getGlobalConfigureFork() {
        if (PASchedulerProperties.TASK_FORK.getValueAsStringOrNull() == null) {
            return null;
        } else {
            return PASchedulerProperties.TASK_FORK.getValueAsBoolean();
        }
    }

}
