package functionaltests;

import jline.WindowsTerminal;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sandrine on 18/09/2015.
 */
public class TagCommandsFunctTest extends AbstractFunctCmdTest {


    private static JobId jobId = null;



    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        if(jobId == null) {
            cleanScheduler();

            //submit a job with a loop and out and err outputs
            System.out.println("submit a job with loop, out and err outputs");
            jobId = submitJob("flow_loop_out.xml", JobStatus.FINISHED);
            System.out.println("Job finished");
        }
    }


    @Test
    public void testListJobTaskIds() throws Exception {
        typeLine("listtasks(1)");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("listtasks(1, 'T2-LOOP-1')");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("listtasks(1, 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("listtasks(2, 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("taskstates(1)");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("taskstates(1, 'T2-LOOP-1')");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("taskstates(1, 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("taskstates(2, 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("joboutput(1)");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("joboutput(1, 'T2-LOOP-1')");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("joboutput(1, 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.out.println(out);
        assertTrue(!out.contains("Task 1 : Test STDERR"));
        assertTrue(!out.contains("Task 1 : Test STDOUT"));
        assertTrue(!out.contains("Terminate task number 1"));
    }


    @Test
    public void testListJobOutputUnknownJob() throws Exception {
        typeLine("joboutput(2, 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.out.println(out);
        assertTrue(out.contains("error"));
    }


    @Test
    public void testJobResult() throws Exception {
        typeLine("jobresult(1)");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("jobresult(1, 'T2-LOOP-1')");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("jobresult(1, 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
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
        typeLine("jobresult(2, 'unknownTag')");

        runCli();

        String out = this.capturedOutput.toString();
        System.out.println(out);
        assertTrue(out.contains("error"));
    }
}
