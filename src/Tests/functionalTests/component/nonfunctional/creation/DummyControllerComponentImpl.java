package functionalTests.component.nonfunctional.creation;

/**
 *
 * @author Paul Naoumenko
 *
 * Content class of the dummy controller component
 */
public class DummyControllerComponentImpl implements DummyControllerItf {
    public String dummyMethodWithResult() {
        return "Message from dummy controller";
    }

    public void dummyVoidMethod(String message) {
        System.out.println("Received message :" + message);
    }
}
