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


public class VerifyChangeTest {

    static {
        PropertyConfigurator.configure(VerifyChangeTest.class.getResource("/performancetests/config/log4j.properties"));
    }

    private static final Logger LOGGER = Logger.getLogger(VerifyChangeTest.class);

    /**
     * When any performance metric changes more or less than
     * given threshold then this test fails
     */
    private static final Double THRESHOLD = Double.parseDouble(System.getProperty("threshold"));

    @Test
    public void changeShouldBeLessThanThreshold() throws IOException {

        final List<File> twoLastFiles = getTwoLastFiles();

        final File newFile = twoLastFiles.get(0);
        final File previousFile = twoLastFiles.get(1);

        final Map<String, Long> prediousReport = readReport(previousFile);
        final Map<String, Long> newReport = readReport(newFile);

        final Map<String, Double> compared = compareReports(prediousReport, newReport);

        boolean toNotify = false;
        LOGGER.info(String.format("Going to compare %s and %s.", previousFile.getName(), newFile.getName()));
        String notifyReport = String.format("Two last performance reports were compared: \n\t\t'%s' \n\tand\n\t\t'%s'\n",
                                            previousFile.getName(),
                                            newFile.getName());
        notifyReport += "Some performance metrics have become bigger than " + (THRESHOLD * 100) + "%:\n";
        final Iterator<Map.Entry<String, Double>> iterator = compared.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, Double> entry = iterator.next();
            if (Math.abs(entry.getValue()) > THRESHOLD) {
                toNotify = true;
                notifyReport += String.format("%s has changed by %.2f%% from %dms to %dms;\n",
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

    private Map<String, Double> compareReports(Map<String, Long> previousReport, Map<String, Long> newReport) {
        assertEquals(previousReport.size(), newReport.size());
        Map<String, Double> result = new HashMap<>(previousReport.size());

        final Iterator<String> iterator = previousReport.keySet().iterator();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            assertTrue(previousReport.containsKey(key));
            assertTrue(newReport.containsKey(key));

            final Long previousValue = previousReport.get(key);
            final Long newValue = newReport.get(key);

            double change = (newValue.doubleValue() / previousValue.doubleValue() - 1) * 100;
            result.put(key, change);

        }

        assertEquals(previousReport.size(), result.size());
        return result;
    }

    private List<File> getTwoLastFiles() {
        final String pathToStorage = System.getProperty("pathToStorage");
        File folder = new File(pathToStorage);
        assertTrue("PathToStorage parameter does not lead to valid path.", folder.exists());
        assertTrue("PathToStorage parameter does not lead to directory.", folder.isDirectory());
        final File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("performance");
            }
        });
        assertNotEquals(0, files.length);
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File fileA, File fileB) {
                return Long.valueOf(fileA.lastModified()).compareTo(fileB.lastModified());
            }
        });

        List<File> result = new ArrayList<>(2);

        result.add(files[files.length - 1]);
        result.add(files[files.length - 2]);

        assertEquals(2, result.size());
        return result;
    }

    private Map<String, Long> readReport(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        Map<String, Long> result = new HashMap<>(8);

        String line = null;
        while ((line = br.readLine()) != null) {
            final String[] values = line.split(",");
            String key = values[1] + "-" + values[2];
            String value = values[5];
            result.put(key, Long.parseLong(value));
        }

        return result;
    }

}
