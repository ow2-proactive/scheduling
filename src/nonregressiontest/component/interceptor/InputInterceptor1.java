package nonregressiontest.component.interceptor;

import org.objectweb.proactive.core.component.interception.InputInterceptor;

import nonregressiontest.component.controller.DummyController;


public interface InputInterceptor1 extends DummyController, InputInterceptor {
    public static final String INPUT_INTERCEPTOR1_NAME = "input-interceptor-1";
    public static final String AFTER_INTERCEPTION = " - after-interception-" +
        INPUT_INTERCEPTOR1_NAME + " - ";
    public static final String BEFORE_INTERCEPTION = " - before-interception-" +
        INPUT_INTERCEPTOR1_NAME + " - ";
}
