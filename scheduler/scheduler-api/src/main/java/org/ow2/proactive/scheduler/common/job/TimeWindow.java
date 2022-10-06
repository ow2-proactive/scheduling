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
 * Class representing the time window of a job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public enum TimeWindow implements java.io.Serializable {

    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly");

    private final String name;

    TimeWindow(String name) {
        this.name = name;
    }

    /**
     * @see Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    public static TimeWindow getTimeWindow(String timeWindow) {
        if ("daily".equalsIgnoreCase(timeWindow)) {
            return DAILY;
        } else if ("weekly".equalsIgnoreCase(timeWindow)) {
            return WEEKLY;
        } else if ("monthly".equalsIgnoreCase(timeWindow)) {
            return MONTHLY;
        } else if ("yearly".equalsIgnoreCase(timeWindow)) {
            return YEARLY;
        }
        return null;
    }
}
