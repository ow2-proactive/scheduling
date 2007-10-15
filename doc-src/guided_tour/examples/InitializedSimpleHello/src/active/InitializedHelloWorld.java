package active;
import java.io.IOException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;

public class InitializedHelloWorld extends HelloWorld implements InitActive,
		EndActive {
	public void initActivity(Body body) {
		System.out.println("Starting activity.....");
	}
	public void endActivity(Body body) {
		System.out.println("Ending activity.....");
	}
	public void terminate() throws IOException {
		// the termination of the activity is done through a call on the
		// terminate method of the body associated to the current active object
		ProActiveObject.getBodyOnThis().terminate();
	}
}
