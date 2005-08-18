package nonregressiontest.component.activity;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.core.component.body.ComponentRunActive;

public class A implements ComponentInitActive, ComponentEndActive, ComponentRunActive, InitActive, RunActive, EndActive {
    
    public static String MESSAGE = "";
    public static String INIT_COMPONENT_ACTIVITY = "init-component-activity";
    public static String RUN_COMPONENT_ACTIVITY = "run-component-activity";
    public static String END_COMPONENT_ACTIVITY = "end-component-activity";
    public static String INIT_FUNCTIONAL_ACTIVITY = "init-functional-activity";
    public static String RUN_FUNCTIONAL_ACTIVITY = "run-functional-activity";
    public static String END_FUNCTIONAL_ACTIVITY = "end-functional-activity";

    public void initComponentActivity(Body body) {
        MESSAGE+=INIT_COMPONENT_ACTIVITY;
    }

    public void runComponentActivity(Body body) {
        MESSAGE+=RUN_COMPONENT_ACTIVITY;

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
        MESSAGE+=END_COMPONENT_ACTIVITY;
    }

    public void initActivity(Body body) {
        MESSAGE+=INIT_FUNCTIONAL_ACTIVITY;
    }

    public void runActivity(Body body) {
        MESSAGE+=RUN_FUNCTIONAL_ACTIVITY;
    }

    public void endActivity(Body body) {
        MESSAGE+=END_FUNCTIONAL_ACTIVITY;
    }

    

}
