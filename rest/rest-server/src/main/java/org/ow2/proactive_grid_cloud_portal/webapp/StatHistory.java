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
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.util.List;


public class StatHistory {
    /**
     * time range for the statistic values
     */
    public enum Range {
        a("1 minute", 'a', 60, 5), //
        n("5 minutes", 'n', 60 * 5, 10),
        m("10 minutes", 'm', 60 * 10, 20), //
        t("30 minutes", 't', 60 * 30, 20), //
        h("1 hour", 'h', 60 * 60, 60), //
        j("2 hour", 'j', 60 * 60 * 2, 60), //
        k("4 hour", 'k', 60 * 60 * 4, 60), //
        H("8 hours", 'H', 60 * 60 * 8, 60 * 10), //
        d("1 day", 'd', 60 * 60 * 24, 60 * 30), //
        w("1 week", 'w', 60 * 60 * 24 * 7, 60 * 60 * 3), //
        M("1 month", 'M', 60 * 60 * 24 * 28, 60 * 60 * 8), //
        y("1 year", 'y', 60 * 60 * 24 * 365, 60 * 60 * 24);

        private char charValue;

        private String stringValue;

        private long duration;

        private long updateFreq;

        Range(String str, char c, long duration, long updateFreq) {
            this.stringValue = str;
            this.charValue = c;
            this.duration = duration;
            this.updateFreq = updateFreq;
        }

        public String getString() {
            return this.stringValue;
        }

        public char getChar() {
            return this.charValue;
        }

        public long getDuration() {
            return this.duration;
        }

        public long getUpdateFrequency() {
            return this.updateFreq;
        }

        public static Range create(char c) {
            for (Range r : Range.values()) {
                if (r.charValue == c)
                    return r;
            }
            return Range.a;
        }
    }

    public List<Double> values;

    public String source;

    public Range range;

    public StatHistory(String source, List<Double> values, Range range) {
        this.values = values;
        this.source = source;
        this.range = range;
    }
}
