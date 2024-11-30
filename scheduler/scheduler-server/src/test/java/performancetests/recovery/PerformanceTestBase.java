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
package performancetests.recovery;

import io.github.pixee.security.BoundedLineReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;


/**
 * We need this class only to increase timeout rule for performance tests.
 */
@SuppressWarnings("squid:S2187")
public class PerformanceTestBase extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    protected static final Logger LOGGER = Logger.getLogger(PerformanceTestBase.class);

    @Rule
    public Timeout testTimeout = new Timeout(CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue() * 10,
                                             TimeUnit.MILLISECONDS);

    public static final String SUCCESS = "SUCCESS";

    public static final String FAILURE = "FAILURE";

    public static final String SEPARATOR = ",";

    public static final String FILE_PREFIX = "performance";

    public static final String ERROR = "ERROR";

    protected static final URL SCHEDULER_CONFIGURATION_START = PerformanceTestBase.class.getResource("/performancetests/config/scheduler-start-memory.ini");

    protected static final URL RM_CONFIGURATION_START = PerformanceTestBase.class.getResource("/performancetests/config/rm-start-memory.ini");

    protected JobId jobId;

    @After
    public void after() throws Exception {
        if (schedulerHelper != null) {
            if (jobId != null) {
                if (!schedulerHelper.getSchedulerInterface().getJobState(jobId).isFinished()) {
                    schedulerHelper.getSchedulerInterface().killJob(jobId);
                }
                schedulerHelper.getSchedulerInterface().removeJob(jobId);
            }
            schedulerHelper.log("Kill Scheduler after test.");
            schedulerHelper.killScheduler();
        }
    }

    public static String makeCSVString(Object... strings) {
        String result = "";
        for (Object object : strings) {
            result += SEPARATOR + object.toString();
        }
        return result.substring(1);
    }

    public static Map<String, Double> readReport(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        Map<String, Double> result = new HashMap<>(8);

        String line = null;
        while ((line = BoundedLineReader.readLine(br, 5_000_000)) != null) {
            final String[] values = line.split(SEPARATOR);
            String key = values[1] + "-" + values[2];
            String value = values[values.length - 2];
            result.put(key, Double.parseDouble(value));
        }

        return result;
    }

    public static List<File> getTwoLastFiles() {
        File folder = new File(System.getProperty("pa.rm.home"));
        assertTrue("PathToStorage parameter does not lead to directory.", folder.isDirectory());
        final File[] files = folder.listFiles((file, s) -> s.startsWith(FILE_PREFIX));
        assertNotEquals(0, files.length);
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        List<File> result = new ArrayList<>(2);

        result.add(files[files.length - 1]);
        result.add(files[files.length - 2]);

        assertEquals(2, result.size());
        return result;
    }

}
