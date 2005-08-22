package nonregressiontest.component.interceptor;

import org.objectweb.proactive.core.component.interception.OutputInterceptor;

import nonregressiontest.component.controller.DummyController;


public interface OutputInterceptor1 extends OutputInterceptor, DummyController {
    public static final String OUTPUT_INTERCEPTOR_1_NAME = "output-interceptor-1";
    public static final String AFTER_INTERCEPTION = " - after-interception-" +
        OUTPUT_INTERCEPTOR_1_NAME + " - ";
    public static final String BEFORE_INTERCEPTION = " - before-interception-" +
        OUTPUT_INTERCEPTOR_1_NAME + " - ";
}
