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
package functionaltests;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobStatus;


public class TagCommandsFunctTest extends AbstractFunctCmdTest {

    private static JobId jobId = null;

    /**
     * Very large jobId, very probably not existent, in this test env.
     */
    private static long NOT_EXISTENT_JOBID = 234454567;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Init class: " + TagCommandsFunctTest.class);
        init();
        System.out.println("Finished init class: " + TagCommandsFunctTest.class);
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("Setup test case for class: " + TagCommandsFunctTest.class);
        synchronized (TagCommandsFunctTest.class) {
            System.out.println("Synchronized");
            super.setUp();
            if (jobId == null) {
                System.out.println("JobId was null");
                cleanScheduler();

                //submit a job with a loop and out and err outputs
                System.out.println("submit a job with loop, out and err outputs");
                jobId = submitJob("flow_loop_out.xml", JobStatus.FINISHED);
                System.out.println("Job " + jobId + " finished");
            }
        }
        System.out.println("Finished setup test case");
    }

    @Test
    public void testListJobTaskIds() throws Exception {
        typeLine("listtasks(+" + jobId.longValue() + ")");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListJobTaskIds:");
        System.out.println(out);
        assertTrue(out.contains("T1#1"));
        assertTrue(out.contains("Print1#1"));
        assertTrue(out.contains("Print2#1"));
        assertTrue(out.contains("T2#1"));
        assertTrue(out.contains("T1#2"));
        assertTrue(out.contains("Print1#2"));
        assertTrue(out.contains("Print2#2"));
        assertTrue(out.contains("T2#2"));
    }

    @Test
    public void testListJobTaskIdsWithTag() throws Exception {
        typeLine("listtasks(" + jobId.longValue() + ", 'LOOP-T2-1')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListJobTaskIdsWithTag:");
        System.out.println(out);
        assertTrue(out.contains("T1#1"));
        assertTrue(out.contains("Print1#1"));
        assertTrue(out.contains("Print2#1"));
        assertTrue(out.contains("T2#1"));
        assertTrue(!out.contains("T1#2"));
        assertTrue(!out.contains("Print1#2"));
        assertTrue(!out.contains("Print2#2"));
        assertTrue(!out.contains("T2#2"));
    }

    @Test
    public void testListJobTaskIdsWithUnknownTag() throws Exception {
        typeLine("listtasks(" + jobId.longValue() + ", 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListJobTaskIdsWithUnknownTag:");
        System.out.println(out);
        assertTrue(!out.contains("T1#1"));
        assertTrue(!out.contains("Print1#1"));
        assertTrue(!out.contains("Print2#1"));
        assertTrue(!out.contains("T2#1"));
        assertTrue(!out.contains("T1#2"));
        assertTrue(!out.contains("Print1#2"));
        assertTrue(!out.contains("Print2#2"));
        assertTrue(!out.contains("T2#2"));
    }

    @Test
    public void testListJobTaskIdsUnknownJob() throws Exception {
        typeLine("listtasks(" + NOT_EXISTENT_JOBID + ", 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListJobTaskIdsUnknownJob:");
        System.out.println(out);
        assertTrue(out.contains("error"));
        assertTrue(!out.contains("T1#1"));
        assertTrue(!out.contains("Print1#1"));
        assertTrue(!out.contains("Print2#1"));
        assertTrue(!out.contains("T2#1"));
        assertTrue(!out.contains("T1#2"));
        assertTrue(!out.contains("Print1#2"));
        assertTrue(!out.contains("Print2#2"));
        assertTrue(!out.contains("T2#2"));
    }

    @Test
    public void testListTaskStates() throws Exception {
        typeLine("taskstates(" + jobId.longValue() + ")");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListTaskStates:");
        System.out.println(out);
        assertTrue(out.contains("T1#1"));
        assertTrue(out.contains("Print1#1"));
        assertTrue(out.contains("Print2#1"));
        assertTrue(out.contains("T2#1"));
        assertTrue(out.contains("T1#2"));
        assertTrue(out.contains("Print1#2"));
        assertTrue(out.contains("Print2#2"));
        assertTrue(out.contains("T2#2"));
    }

    @Test
    public void testListTaskStateWithTag() throws Exception {
        typeLine("taskstates(" + jobId.longValue() + ", 'LOOP-T2-1')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListTaskStateWithTag:");
        System.out.println(out);
        assertTrue(out.contains("T1#1"));
        assertTrue(out.contains("Print1#1"));
        assertTrue(out.contains("Print2#1"));
        assertTrue(out.contains("T2#1"));
        assertTrue(!out.contains("T1#2"));
        assertTrue(!out.contains("Print1#2"));
        assertTrue(!out.contains("Print2#2"));
        assertTrue(!out.contains("T2#2"));
    }

    @Test
    public void testListTaskStatesWithUnknownTag() throws Exception {
        typeLine("taskstates(" + jobId.longValue() + ", 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListTaskStatesWithUnknownTag:");
        System.out.println(out);
        assertTrue(!out.contains("T1#1"));
        assertTrue(!out.contains("Print1#1"));
        assertTrue(!out.contains("Print2#1"));
        assertTrue(!out.contains("T2#1"));
        assertTrue(!out.contains("T1#2"));
        assertTrue(!out.contains("Print1#2"));
        assertTrue(!out.contains("Print2#2"));
        assertTrue(!out.contains("T2#2"));
    }

    @Test
    public void testListTaskStatesUnknownJob() throws Exception {
        typeLine("taskstates(" + NOT_EXISTENT_JOBID + ", 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListTaskStatesUnknownJob:");
        System.out.println(out);
        assertTrue(out.contains("error"));
        assertTrue(!out.contains("T1#1"));
        assertTrue(!out.contains("Print1#1"));
        assertTrue(!out.contains("Print2#1"));
        assertTrue(!out.contains("T2#1"));
        assertTrue(!out.contains("T1#2"));
        assertTrue(!out.contains("Print1#2"));
        assertTrue(!out.contains("Print2#2"));
        assertTrue(!out.contains("T2#2"));
    }

    @Test
    public void testJobOutput() throws Exception {
        typeLine("joboutput(" + jobId.longValue() + ")");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testJobOutput:");
        System.out.println(out);
        assertTrue(StringUtils.countMatches(out, "Task 1 : Test STDERR") == 2);
        assertTrue(StringUtils.countMatches(out, "Task 1 : Test STDOUT") == 2);
        assertTrue(StringUtils.countMatches(out, "Terminate task number 1") == 2);
        assertTrue(StringUtils.countMatches(out, "Task 2 : Test STDERR") == 2);
        assertTrue(StringUtils.countMatches(out, "Task 2 : Test STDOUT") == 2);
        assertTrue(StringUtils.countMatches(out, "Terminate task number 2") == 2);
    }

    @Test
    public void testJobOutputWithTag() throws Exception {
        typeLine("joboutput(" + jobId.longValue() + ", 'LOOP-T2-1')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testJobOutputWithTag:");
        System.out.println(out);
        assertEquals(2, StringUtils.countMatches(out, "Task 1 : Test STDERR"));
        assertEquals(2, StringUtils.countMatches(out, "Task 1 : Test STDOUT"));
        assertEquals(2, StringUtils.countMatches(out, "Terminate task number 1"));
        assertTrue(!out.contains("Task 2 : Test STDERR"));
        assertTrue(!out.contains("Task 2 : Test STDOUT"));
        assertTrue(!out.contains("Terminate task number 2"));
    }

    @Test
    public void testJobOutputWithUnknownTag() throws Exception {
        typeLine("joboutput(" + jobId.longValue() + ", 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testJobOutputWithUnknownTag:");
        System.out.println(out);
        assertTrue(!out.contains("Task 1 : Test STDERR"));
        assertTrue(!out.contains("Task 1 : Test STDOUT"));
        assertTrue(!out.contains("Terminate task number 1"));
    }

    @Test
    public void testListJobOutputUnknownJob() throws Exception {
        typeLine("joboutput(" + NOT_EXISTENT_JOBID + ", 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListJobOutputUnknownJob:");
        System.out.println(out);
        assertTrue(out.contains("error"));
    }

    @Test
    public void testJobResult() throws Exception {
        typeLine("jobresult(" + jobId.longValue() + ")");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testJobResult:");
        System.out.println(out);
        assertTrue(out.contains("T1#1"));
        assertTrue(out.contains("Print1#1"));
        assertTrue(out.contains("Print2#1"));
        assertTrue(out.contains("T2#1"));
        assertTrue(out.contains("T1#2"));
        assertTrue(out.contains("Print1#2"));
        assertTrue(out.contains("Print2#2"));
        assertTrue(out.contains("T2#2"));
    }

    @Test
    public void testJobResultWithTag() throws Exception {
        typeLine("jobresult(" + jobId.longValue() + ", 'LOOP-T2-1')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testJobResultWithTag:");
        System.out.println(out);
        assertTrue(out.contains("T1#1"));
        assertTrue(out.contains("Print1#1"));
        assertTrue(out.contains("Print2#1"));
        assertTrue(out.contains("T2#1"));
        assertTrue(!out.contains("T1#2"));
        assertTrue(!out.contains("Print1#2"));
        assertTrue(!out.contains("Print2#2"));
        assertTrue(!out.contains("T2#2"));
    }

    @Test
    public void testJobResultWithUnknownTag() throws Exception {
        typeLine("jobresult(" + jobId.longValue() + ", 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testJobResultWithUnknownTag:");
        System.out.println(out);
        assertTrue(!out.contains("T1#1"));
        assertTrue(!out.contains("Print1#1"));
        assertTrue(!out.contains("Print2#1"));
        assertTrue(!out.contains("T2#1"));
        assertTrue(!out.contains("T1#2"));
        assertTrue(!out.contains("Print1#2"));
        assertTrue(!out.contains("Print2#2"));
        assertTrue(!out.contains("T2#2"));
    }

    @Test
    public void testListJobResultUnknownJob() throws Exception {
        typeLine("jobresult(" + NOT_EXISTENT_JOBID + ", 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.setOut(stdOut);
        System.out.println("------------- testListJobResultUnknownJob:");
        System.out.println(out);
        assertTrue(out.contains("error"));
    }
}
