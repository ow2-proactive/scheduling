package functionaltests.jobs;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import java.io.Serializable;


public class SimpleJob extends JavaExecutable {

    public static final String TEST_JOB = "TEST-JOB";

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        return new StringWrapper(TEST_JOB);
    }

}
