package nonregressiontest.component.interceptor;

import nonregressiontest.component.controller.DummyController;

import org.objectweb.proactive.core.component.interception.OutputInterceptor;

public interface OutputInterceptor1 extends OutputInterceptor, DummyController {
    public static final String OUTPUT_INTERCEPTOR_1_NAME = "output-interceptor-1";
    
    public static final String AFTER_INTERCEPTION = " - after-interception-"+OUTPUT_INTERCEPTOR_1_NAME + " - ";
    public static final String BEFORE_INTERCEPTION = " - before-interception-"+OUTPUT_INTERCEPTOR_1_NAME + " - ";


}
