package modelisation.timedrequest;

import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.body.UniversalBody;



public class TimedFactory implements RequestFactory, java.io.Serializable{

    public Request newRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
	System.out.println("TimedFactory: creating request");
	
	return new TimedRequest(methodCall, sourceBody, isOneWay, sequenceID);
    }

}
