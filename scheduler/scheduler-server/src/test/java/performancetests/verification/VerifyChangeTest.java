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
package performancetests.verification;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import performancetests.recovery.PeformanceTestBase;


public class VerifyChangeTest {

    static {
        PropertyConfigurator.configure(VerifyChangeTest.class.getResource("/performancetests/config/log4j.properties"));
    }

    private static final Logger LOGGER = Logger.getLogger(VerifyChangeTest.class);

    /**
     * When any performance metric changes more or less than
     * given threshold then this test fails
     */
    private static final Double THRESHOLD = Double.parseDouble(System.getProperty("performanceTestThreshold"));

    @Test
    public void changeShouldBeLessThanThreshold() throws IOException {

        final List<File> twoLastFiles = PeformanceTestBase.getTwoLastFiles();

        final File newFile = twoLastFiles.get(0);
        final File previousFile = twoLastFiles.get(1);

        final Map<String, Double> prediousReport = PeformanceTestBase.readReport(previousFile);
        final Map<String, Double> newReport = PeformanceTestBase.readReport(newFile);

        final Map<String, Double> compared = compareReports(prediousReport, newReport);

        boolean toNotify = false;

        LOGGER.info(String.format("Going to compare %s and %s.", previousFile.getName(), newFile.getName()));

        String notifyReport = String.format("Two last performance reports were compared: \n\t\t'%s' \n\tand\n\t\t'%s'\n",
                                            previousFile.getName(),
                                            newFile.getName());

        notifyReport += "Some performance metrics have become bigger than " + THRESHOLD + "%:\n";

        for (Map.Entry<String, Double> entry : compared.entrySet()) {
            if (Math.abs(entry.getValue()) > THRESHOLD) {
                toNotify = true;
                notifyReport += String.format("%s has changed by %.2f%% from %fms to %fms;\n",
                                              entry.getKey(),
                                              entry.getValue(),
                                              prediousReport.get(entry.getKey()),
                                              newReport.get(entry.getKey()));
            }
        }
        if (toNotify) {
            LOGGER.info(notifyReport);
            LOGGER.info("Verification status: FAILURE.\n");
        } else {
            LOGGER.info("Verification status: SUCCESS.\n");
        }

        assertFalse(notifyReport, toNotify);
    }

    private Map<String, Double> compareReports(Map<String, Double> previousReport, Map<String, Double> newReport) {
        Map<String, Double> result = new HashMap<>(previousReport.size());

        Set<String> intersectionOfKeys = new HashSet<>(previousReport.keySet());
        intersectionOfKeys.removeAll(newReport.keySet());

        for (String key : intersectionOfKeys) {
            final double previousValue = previousReport.get(key);
            final double newValue = newReport.get(key);

            double change = (newValue / previousValue - 1) * 100; // change in percentage
            result.put(key, change);
        }

        return result;
    }

}
