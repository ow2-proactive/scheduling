package nonregressiontest.component.interceptor;

public interface InputOutputInterceptor extends InputInterceptor1, OutputInterceptor1 {
    
    public static final String INPUT_OUTPUT_INTERCEPTOR_NAME = "input-output-interceptor";
    
    public static final String AFTER_INPUT_INTERCEPTION = " - after-input-interception-"+INPUT_OUTPUT_INTERCEPTOR_NAME + " - ";
    public static final String BEFORE_INPUT_INTERCEPTION = " - before-input-interception-"+INPUT_OUTPUT_INTERCEPTOR_NAME + " - ";

    public static final String AFTER_OUTPUT_INTERCEPTION = " - after-output-interception-"+INPUT_OUTPUT_INTERCEPTOR_NAME + " - ";
    public static final String BEFORE_OUTPUT_INTERCEPTION = " - before-output-interception-"+INPUT_OUTPUT_INTERCEPTOR_NAME + " - ";

}
