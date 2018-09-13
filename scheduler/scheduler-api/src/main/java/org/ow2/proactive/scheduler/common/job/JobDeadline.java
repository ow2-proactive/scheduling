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

import java.time.Duration;
import java.util.Date;
import java.util.Optional;


public class JobDeadline {

    private final Date absoluteDeadline;

    private final Duration relativeDeadline;

    public JobDeadline(Date absoluteDeadline) {
        this.absoluteDeadline = absoluteDeadline;
        this.relativeDeadline = null;
        Duration s;
    }

    public JobDeadline(Duration relativeDeadline) {
        this.absoluteDeadline = null;
        this.relativeDeadline = relativeDeadline;
    }

    public Date getAbsoluteDeadline() {
        assert isAbsolute() : "Job deadline is not absolute: " + relativeDeadline;
        return absoluteDeadline;
    }

    public Duration getRelativeDeadline() {
        assert isRelative() : "Job deadline is not relative: " + absoluteDeadline;
        return relativeDeadline;
    }

    public boolean isAbsolute() {
        return absoluteDeadline != null;
    }

    public boolean isRelative() {
        return !isAbsolute();
    }

    /**
     * Parse string to the duration:
     * `05:44:15` - 5 hours, 44 minutes, 15 seconds
     * `44:15` - 44 minutes, 15 seconds
     * `15` - 15 seconds
     * @return duration if non empty, otherwise an empty optional
     * @throws Exception if it is not possible to parse the string
     */
    public static Optional<Duration> parseDuration(String stringAsDuration) throws Exception {
        if (stringAsDuration != null && stringAsDuration.length() > 0) {
            final String[] parts = stringAsDuration.split(":");
            if (parts.length > 3) {
                throw new Exception("Wrong format: " + stringAsDuration);
            }
            int seconds = 0;

            if (parts.length == 3) {
                seconds += 60 * 60 * Integer.parseInt(parts[0]);
                seconds += 60 * Integer.parseInt(parts[1]);
                seconds += Integer.parseInt(parts[2]);
            }
            if (parts.length == 2) {
                seconds += 60 * Integer.parseInt(parts[0]);
                seconds += Integer.parseInt(parts[1]);
            }
            if (parts.length == 1) {
                seconds += Integer.parseInt(parts[0]);
            }

            return Optional.of(Duration.ofSeconds(seconds));
        } else {
            return Optional.empty();
        }

    }
}
