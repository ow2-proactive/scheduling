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
package functionaltests.policy.edf;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.job.TestJobWhenSchedulerPaused;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


public class EDFPolicyExtendedTest extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static URL lowest = TestJobWhenSchedulerPaused.class.getResource("/functionaltests/descriptors/Job_5s_lowest.xml");

    private static URL highest = TestJobWhenSchedulerPaused.class.getResource("/functionaltests/descriptors/Job_5s_highest.xml");

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper.log("Start Scheduler with EDFPolicy.");
        schedulerHelper = new SchedulerTHelper(false,
                                               true,
                                               new File(EDFPolicyExtendedTest.class.getResource("/functionaltests/config/functionalTSchedulerProperties-edfpolicyextended.ini")
                                                                                   .toURI()).getAbsolutePath());
    }

    @Test
    public void testAbsoluteDateFormat() throws Exception {
        final JobId jobId0 = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                             generalInformation("1990-10-20T19:45:00+01", "4:30:00"));

        final JobId jobId1 = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                             generalInformation("1990-10-20T19:45:00+00", "4:30:00"));

        schedulerHelper.createNodeSource("local", 1);

        EDFPolicyTest.waitAndcheckJobOrderByFinishedTime(jobId0, jobId1);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsAbsoluteAgainstRelativeDeadline() throws Exception {

        final JobId jobIdAbsolute = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                    generalInformation(new DateTime().plusHours(1),
                                                                                       ""));

        final JobId jobIdRelated = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                   generalInformation("+2:00:00", ""));

        schedulerHelper.createNodeSource("local", 1);

        EDFPolicyTest.waitAndcheckJobOrderByFinishedTime(jobIdAbsolute, jobIdRelated);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsRelativeSeconds() throws Exception {
        final JobId jobIdRelative0 = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                     generalInformation("+2", "1"));

        final JobId jobIdRelative1 = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                     generalInformation("+3", "1"));

        schedulerHelper.createNodeSource("local", 1);

        EDFPolicyTest.waitAndcheckJobOrderByFinishedTime(jobIdRelative0, jobIdRelative1);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsRelativeMinutes() throws Exception {
        final JobId jobIdRelative0 = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                     generalInformation("+2:00", "1:00"));

        final JobId jobIdRelative1 = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                     generalInformation("+3:00", "1:00"));

        schedulerHelper.createNodeSource("local", 1);

        EDFPolicyTest.waitAndcheckJobOrderByFinishedTime(jobIdRelative0, jobIdRelative1);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsRelativeVsNoDeadline() throws Exception {
        final JobId noDeadline = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                 generalInformation("", ""));

        final JobId relativeDaedline = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                       generalInformation("+3:00", "1:00"));

        schedulerHelper.createNodeSource("local", 1);

        EDFPolicyTest.waitAndcheckJobOrderByFinishedTime(relativeDaedline, noDeadline);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsRelativeWithAndWithoutExpectedTime() throws Exception {
        final JobId noExpectedTime = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                     generalInformation("+1:00", ""));

        final JobId relativeDaedline = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                       generalInformation("+60", "1"));

        schedulerHelper.createNodeSource("local", 1);

        EDFPolicyTest.waitAndcheckJobOrderByFinishedTime(relativeDaedline, noExpectedTime);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsRelativeWithAndWithoutExpectedTime2() throws Exception {
        final JobId noExpectedTime = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                     generalInformation("+1:00", ""));

        final JobId relativeDaedline = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                       generalInformation("+63", "1"));

        schedulerHelper.createNodeSource("local", 1);

        EDFPolicyTest.waitAndcheckJobOrderByFinishedTime(noExpectedTime, relativeDaedline);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsExpectedTime() throws Exception {

        final JobId jobIdAbsolute = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                    generalInformation(new DateTime().plusHours(1),
                                                                                       "30:00"));

        final JobId jobIdRelated = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                   generalInformation(new DateTime().plusHours(1),
                                                                                      "40:00"));

        schedulerHelper.createNodeSource("local", 1);

        EDFPolicyTest.waitAndcheckJobOrderByFinishedTime(jobIdRelated, jobIdAbsolute);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsHaveSamePriorityTest() throws Exception {

        JobId noDeadLine0 = schedulerHelper.submitJob(new File(lowest.toURI()).getAbsolutePath());

        JobId noDeadLine1 = schedulerHelper.submitJob(new File(lowest.toURI()).getAbsolutePath());

        final JobId jobIdAbsolute = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                    generalInformation(new DateTime().plusHours(10),
                                                                                       "5:00:00"));

        final JobId jobIdRelated0 = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                    generalInformation("+9:00:00", "4:30:00"));

        final JobId jobIdRelated1 = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                    generalInformation("+8:00:00", "sss"));

        schedulerHelper.createNodeSource("local", 1);

        EDFPolicyTest.waitAndcheckJobOrderByFinishedTime(jobIdRelated0,
                                                         jobIdAbsolute,
                                                         jobIdRelated1,
                                                         noDeadLine0,
                                                         noDeadLine1);

        schedulerHelper.removeNodeSource("local");
    }

    public static Map<String, String> generalInformation(DateTime dateTime) {
        Map<String, String> variables = new HashMap<>();
        variables.put("JOB_DDL", EDFPolicyTest.dateToISOWithoutMillisecondsString(dateTime));
        return variables;
    }

    public static Map<String, String> generalInformation(DateTime deadline, String expectedTime) throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("JOB_DDL", EDFPolicyTest.dateToISOWithoutMillisecondsString(deadline));
        if (expectedTime != null && expectedTime.length() > 0) {
            variables.put("JOB_EXEC_TIME", expectedTime);
        }
        return variables;
    }

    public static Map<String, String> generalInformation(String deadline, String expectedTime) {
        Map<String, String> variables = new HashMap<>();
        variables.put("JOB_DDL", deadline);
        if (expectedTime != null && expectedTime.length() > 0) {
            variables.put("JOB_EXEC_TIME", expectedTime);
        }
        return variables;
    }

}
