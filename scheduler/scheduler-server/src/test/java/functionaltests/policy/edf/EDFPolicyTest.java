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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.job.TestJobWhenSchedulerPaused;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


public class EDFPolicyTest extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper.log("Start Scheduler with EDFPolicy.");
        schedulerHelper = new SchedulerTHelper(false,
                                               true,
                                               new File(EDFPolicyTest.class.getResource("/functionaltests/config/functionalTSchedulerProperties-edfpolicy.ini")
                                                                           .toURI()).getAbsolutePath());
    }

    private static URL lowest = TestJobWhenSchedulerPaused.class.getResource("/functionaltests/descriptors/Job_5s_lowest.xml");

    private static URL highest = TestJobWhenSchedulerPaused.class.getResource("/functionaltests/descriptors/Job_5s_highest.xml");

    @Test
    public void emptyListOfJobsTestOnlyPriorities() throws Exception {

        JobId lowestId = schedulerHelper.submitJob(new File(lowest.toURI()).getAbsolutePath());

        JobId highestId = schedulerHelper.submitJob(new File(highest.toURI()).getAbsolutePath());

        schedulerHelper.createNodeSource("local", 1);

        schedulerHelper.waitForEventJobFinished(highestId);
        schedulerHelper.waitForEventJobFinished(lowestId);

        waitAndcheckJobOrderByFinishedTime(highestId, lowestId);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsHaveSamePriorityTest() throws Exception {

        JobId jobPlus10HoursHighest = schedulerHelper.submitJobWithGI(new File(highest.toURI()).getAbsolutePath(),
                                                                      generalInformation(new DateTime().plusHours(10)));

        JobId jobPlus2HoursHighest = schedulerHelper.submitJobWithGI(new File(highest.toURI()).getAbsolutePath(),
                                                                     generalInformation(new DateTime().plusHours(2)));

        JobId jobPlus5HoursHighest = schedulerHelper.submitJobWithGI(new File(highest.toURI()).getAbsolutePath(),
                                                                     generalInformation(new DateTime().plusHours(5)));

        JobId jobEmptyDeadlineHighest = schedulerHelper.submitJob(new File(highest.toURI()).getAbsolutePath());

        JobId jobMinus2HoursHighest = schedulerHelper.submitJobWithGI(new File(highest.toURI()).getAbsolutePath(),
                                                                      generalInformation(new DateTime().minusHours(2)));

        schedulerHelper.createNodeSource("local", 1);

        waitAndcheckJobOrderByFinishedTime(jobMinus2HoursHighest,
                                           jobPlus2HoursHighest,
                                           jobPlus5HoursHighest,
                                           jobPlus10HoursHighest,
                                           jobEmptyDeadlineHighest);

        schedulerHelper.removeNodeSource("local");
    }

    @Test
    public void jobsHaveDifferentPrioritiesTest() throws Exception {

        JobId jobPlus10HoursHighest = schedulerHelper.submitJobWithGI(new File(highest.toURI()).getAbsolutePath(),
                                                                      generalInformation(new DateTime().plusHours(10)));

        JobId jobPlus2HoursLowest = schedulerHelper.submitJobWithGI(new File(lowest.toURI()).getAbsolutePath(),
                                                                    generalInformation(new DateTime().plusHours(2)));

        JobId jobPlus5HoursHighest = schedulerHelper.submitJobWithGI(new File(highest.toURI()).getAbsolutePath(),
                                                                     generalInformation(new DateTime().plusHours(5)));

        JobId jobEmptyDeadlineLowest = schedulerHelper.submitJob(new File(lowest.toURI()).getAbsolutePath());

        JobId jobMinus2HoursHighest = schedulerHelper.submitJobWithGI(new File(highest.toURI()).getAbsolutePath(),
                                                                      generalInformation(new DateTime().minusHours(2)));

        schedulerHelper.createNodeSource("local", 1);

        waitAndcheckJobOrderByFinishedTime(jobMinus2HoursHighest,
                                           jobPlus5HoursHighest,
                                           jobPlus10HoursHighest,
                                           jobPlus2HoursLowest,
                                           jobEmptyDeadlineLowest);

        schedulerHelper.removeNodeSource("local");
    }

    private static Map<String, String> generalInformation(DateTime dateTime) {
        Map<String, String> variables = new HashMap<>();
        variables.put("JOB_DDL", EDFPolicyTest.dateToISOWithoutMillisecondsString(dateTime));
        return variables;
    }

    public static String dateToISOWithoutMillisecondsString(DateTime dateTime) {
        return ISODateTimeFormat.dateTimeNoMillis().print(dateTime);
    }

    public static void waitAndcheckJobOrderByFinishedTime(JobId... expectedOrder) throws Exception {
        for (JobId jobId : expectedOrder) {
            schedulerHelper.waitForEventJobFinished(jobId);
        }

        List<JobId> actualOrder = new ArrayList<>(Arrays.asList(expectedOrder));

        actualOrder.sort((firstJob, secondJob) -> {
            try {
                return (int) (schedulerHelper.getSchedulerInterface().getJobState(firstJob).getFinishedTime() -
                              schedulerHelper.getSchedulerInterface().getJobState(secondJob).getFinishedTime());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(Arrays.asList(expectedOrder), actualOrder);
    }

}
