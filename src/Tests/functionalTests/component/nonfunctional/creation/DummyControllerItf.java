package functionalTests.component.nonfunctional.creation;

/**
 *
 * @author Paul Naoumenko
 * The server interface for the dummyController non functional component
 *
 */
public interface DummyControllerItf {

    /**
     * This is a dummy method that can be called on the server interface of the DummyController component
     * @return a dummy value
     */
    public String dummyMethodWithResult();

    /**
     *  A void dummy method
     * @param The message you want the controller to display
     */
    public void dummyVoidMethod(String message);
}
