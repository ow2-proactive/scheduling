package functionaltests.forkedmode;

import org.junit.BeforeClass;
import org.junit.Test;

import functionaltests.utils.SchedulerTHelper;


public class TestMissingForkedTaskWithGlobalConfigForked extends TestForkedModeTask {

    @BeforeClass
    public static void startSchedulerConfiguredForked() throws Exception {
        // start an empty scheduler which configured global task fork mode as true
        schedulerHelper = new SchedulerTHelper(true,
                                               getResourceAbsolutePath("/functionaltests/config/scheduler-forkedscripttasks.ini"));
    }

    @Test
    public void testMissingForkedModeParameterWithGlobalConfigForked() throws Exception {
        // test the job task which doesn't have the parameter forkedMode
        testTaskIsRunningInForkedMode("/functionaltests/descriptors/Job_MissingForkedMode.xml");
    }
}
