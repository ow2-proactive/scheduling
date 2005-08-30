package nonregressiontest.component.activity;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.core.component.body.ComponentRunActive;


public class A implements ComponentInitActive, ComponentEndActive,
    ComponentRunActive, InitActive, RunActive, EndActive {
    public static String message = "";
    public static final String INIT_COMPONENT_ACTIVITY = "init-component-activity";
    public static final String RUN_COMPONENT_ACTIVITY = "run-component-activity";
    public static final String END_COMPONENT_ACTIVITY = "end-component-activity";
    public static final String INIT_FUNCTIONAL_ACTIVITY = "init-functional-activity";
    public static final String RUN_FUNCTIONAL_ACTIVITY = "run-functional-activity";
    public static final String END_FUNCTIONAL_ACTIVITY = "end-functional-activity";
    private static Lock lock = new Lock();

    public void initComponentActivity(Body body) {
        message += INIT_COMPONENT_ACTIVITY;
        lock.acquireLock(); // get the lock for the duration of the component activity
    }

    public void runComponentActivity(Body body) {
        message += RUN_COMPONENT_ACTIVITY;

        Service service = new Service(body);

        // serve startFc
        service.blockingServeOldest();

        // because the ComponentRunActive is not the default one, we have
        // to explicitely initialize, start and end the functional activity :
        initActivity(body);
        runActivity(body);
        endActivity(body);

        // serveStopFc
        service.blockingServeOldest();
    }

    public void endComponentActivity(Body body) {
        message += END_COMPONENT_ACTIVITY;
        lock.releaseLock();
    }

    public void initActivity(Body body) {
        message += INIT_FUNCTIONAL_ACTIVITY;
    }

    public void runActivity(Body body) {
        message += RUN_FUNCTIONAL_ACTIVITY;
    }

    public void endActivity(Body body) {
        message += END_FUNCTIONAL_ACTIVITY;
    }

    public static Lock getLock() {
        return lock;
    }
}
