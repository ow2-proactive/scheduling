package functionaltests.executables;

import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import functionaltests.ComplexTypeArgsTest.UserTypeA;


public class ComplexParamsExecutable extends JavaExecutable {

    private UserTypeA param1;

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        System.out.println("Parameter 1 is " + param1.toString());
        return null;
    }

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        this.param1 = (UserTypeA) args.get("param1");
    }

}
