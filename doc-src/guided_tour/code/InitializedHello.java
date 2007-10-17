import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.URIBuilder;


public class InitializedHello extends Hello implements InitActive, EndActive {
    /** Constructor for InitializedHello. */
    public InitializedHello() {
    }

    /** Constructor for InitializedHello.
     * @param name */
    public InitializedHello(String name) {
        super(name);
    }

    /** @see org.objectweb.proactive.InitActive#initActivity(Body)
     * This is the place where to make initialization before the object
     * starts its activity */
    public void initActivity(Body body) {
        System.out.println("I am about to start my activity");
    }

    /** @see org.objectweb.proactive.EndActive#endActivity(Body)
     * This is the place where to clean up or terminate things after the
     * object has finished its activity */
    public void endActivity(Body body) {
        System.out.println("I have finished my activity");
    }

    /** This method will end the activity of the active object */
    public void terminate() {
        // the termination of the activity is done through a call on the
        // terminate method of the body associated to the current active object
        ProActive.getBodyOnThis().terminate();
    }

    public static void main(String[] args) {
        // Registers it with an URL
        try {
            // Creates an active instance of class HelloServer on the local node
            InitializedHello hello = (InitializedHello) org.objectweb.proactive.ProActive.newActive(InitializedHello.class.getName(),
                    new Object[] { "remote" });
            java.net.InetAddress localhost = URIBuilder.getLocalAddress();
            org.objectweb.proactive.api.ProActiveObject.register(hello,
                "//" + localhost.getHostName() + "/Hello");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
