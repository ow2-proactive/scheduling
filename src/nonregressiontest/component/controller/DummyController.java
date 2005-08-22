package nonregressiontest.component.controller;


/**
 * @author Matthieu Morel
 *
 */
public interface DummyController {
    public static final String DUMMY_CONTROLLER_NAME = "dummy-controller";

    public void setDummyValue(String value);

    public String getDummyValue();
}
