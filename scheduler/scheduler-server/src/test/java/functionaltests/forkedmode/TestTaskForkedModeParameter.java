package functionaltests.forkedmode;

import functionaltests.utils.SchedulerTHelper;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestTaskForkedModeParameter extends TestForkedModeTask {

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(true);
    }

    @Test
    public void testForkedTask() throws Exception {
        // test the job task which specified the parameter forkedMode as true
        testTaskIsRunningInForkedMode("/functionaltests/descriptors/Job_ForkedMode.xml");
    }

    @Test
    public void testNonForkedTask() throws Exception {
        // test the job task which specified the parameter forkedMode as false
        testTaskIsRunningInNonForkedMode("/functionaltests/descriptors/Job_NonForkedMode.xml");
    }
}
