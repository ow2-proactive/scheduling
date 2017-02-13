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
package org.ow2.proactive.scheduler.common.job;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Class representing the type of the job.
 * Type are best describe below.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public enum JobType implements java.io.Serializable {

    /**
     * Tasks can be executed one by one or all in same time but
     * every task represents the same native or java task.
     * Only the parameters given to the task will change.
     */
    PARAMETER_SWEEPING("Parameter Sweeping"),
    /**
     * Tasks flow with dependences.
     * Only the task that have their dependences finished
     * can be executed.
     */
    TASKSFLOW("Tasks Flow");

    private String name;

    JobType(String name) {
        this.name = name;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    static JobType getJobType(String typeName) {
        if (typeName.equalsIgnoreCase("taskFlow")) {
            return TASKSFLOW;
        } else {
            return PARAMETER_SWEEPING;
        }
    }
}
