package functionaltests.forkedmode;

import org.junit.BeforeClass;
import org.junit.Test;

import functionaltests.utils.SchedulerTHelper;


public class TestMissingForkedTaskWithGlobalConfigNonForked extends TestForkedModeTask {

    @BeforeClass
    public static void startSchedulerConfiguredNonForked() throws Exception {
        // start an empty scheduler which configured global task fork mode as false
        schedulerHelper = new SchedulerTHelper(true,
                                               getResourceAbsolutePath("/functionaltests/config/scheduler-nonforkedscripttasks.ini"));
    }

    @Test
    public void testMissingForkedModeParameterWithGlobalConfigNonForked() throws Exception {
        // test the job task which doesn't have the parameter forkedMode
        testTaskIsRunningInNonForkedMode("/functionaltests/descriptors/Job_MissingForkedMode.xml");
    }
}
