package functionaltests.jobs;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


public class SimpleJob extends JavaExecutable {

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        return new StringWrapper("TEST-JOB");
    }

}
