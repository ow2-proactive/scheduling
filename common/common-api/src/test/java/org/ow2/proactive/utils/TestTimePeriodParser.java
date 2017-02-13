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
package org.ow2.proactive.utils;

import org.junit.Assert;
import org.junit.Test;


public class TestTimePeriodParser {

    @Test
    public void test() {
        final long second = 1000;
        final long minute = 1000 * 60;
        final long hour = 1000 * 60 * 60;
        final long day = 1000 * 60 * 60 * 24;
        Assert.assertEquals(second, Tools.parsePeriod("1s"));
        Assert.assertEquals(minute, Tools.parsePeriod("1m"));
        Assert.assertEquals(hour, Tools.parsePeriod("1h"));
        Assert.assertEquals(day, Tools.parsePeriod("1d"));
        Assert.assertEquals(1234 * second, Tools.parsePeriod("1234s"));
        Assert.assertEquals(3 * day + 5 * hour, Tools.parsePeriod("3d 5h"));
        Assert.assertEquals(second + 2 * minute + 3 * hour + 4 * day, Tools.parsePeriod("1s 2m 3h 4d"));
        Assert.assertEquals(33 * hour + 10 * minute, Tools.parsePeriod(" 33h     10m "));

        testFails("");
        testFails("1");
        testFails("1a");
        testFails("s");
        testFails("-1s");
    }

    private void testFails(String periodString) {
        try {
            Tools.parsePeriod(periodString);
            Assert.fail("Parsing for " + periodString + " should fail");
        } catch (IllegalArgumentException e) {
            System.out.println("Expected error: " + e);
        }
    }

}
