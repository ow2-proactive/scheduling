package modelisation.server;

import org.objectweb.proactive.ext.locationserver.BodyWithLocationServer;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MethodCall;

import java.lang.reflect.InvocationTargetException;

public class TimedBodyWithServer extends BodyWithLocationServer {

  public TimedBodyWithServer(ConstructorCall c, String nodeName) throws InvocationTargetException, org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException {
    super(c, nodeName);
    System.out.println("YYYYYYYYYYYYYYYYYYYYY");
  }
 

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //  

    /**
     * Blocking method that makes the caller thread wait until incoming communications
     * can be accepted.
     */

//    protected synchronized void waitTillAccept() {
//	long startTime = 0;
//	while (! acceptCommunication) {
//	    startTime = System.currentTimeMillis();
//	    try {
//		wait();
//	    } catch (InterruptedException e) {}
//	}
//	if (startTime != 0) {
//	    System.out.println("TimedBody: waitTillAccept() waited " + (System.currentTimeMillis() - startTime));
//	} // end of if (startTime != 0)
//    }

  protected MigrationManager createMigrationManager() {
      return new TimedMigrationManagerWithServer(LocationServerFactory.getLocationServer());
  }

 
    
  /**
   * Creates the factory in charge of constructing the requests.
   * @return the factory in charge of constructing the requests.
   */
   protected RequestFactory createRequestFactory() {
     return new TimedRequestWithLocationServerFactory();
   }


  //
  // -- INNER CLASSES -----------------------------------------------
  //                                              

  protected class TimedRequestWithLocationServerFactory implements RequestFactory, java.io.Serializable {
  
    private LocationServer server = LocationServerFactory.getLocationServer();
    
    /**
     * Creates a request object based on the given parameter
     * @return a Request object.
     */
    public Request createRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
      return new TimedRequestWithLocationServer(methodCall, sourceBody, isOneWay, sequenceID, server);
    }
  
  } // end inner class RequestWithLocationServerFactory
}
