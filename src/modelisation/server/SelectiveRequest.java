/**
 * Server with an exponential rate mu
 *
 */
package modelisation.server;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.util.timer.MicroTimer;

public class SelectiveRequest extends RequestImpl implements java.io.Serializable {

  private MicroTimer microTimer;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
  
  public SelectiveRequest(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID, MicroTimer microTimer) {
    super(methodCall, sender, isOneWay, nextSequenceID);
    this.microTimer = microTimer;
  }



  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public Reply serve(Body targetBody) throws ServeException {
    Reply reply = null;
    microTimer.start();
    Object result = serveInternal(targetBody);
    if (! isOneWay && sender != null) {
      reply = createReply(targetBody, result);
      if ("searchObject".equals(methodName)) {
        if (sendAnswer(targetBody, "updateLocation")) {
          //sender.receiveReply(reply);
        } else {
          try {
            System.out.println("SelectiveService: puting the request back in the line");
            targetBody.getRequestQueue().add(this);
            reply = null;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } else {
        //sender.receiveReply(reply);
      }	
    }
    long endTime = microTimer.getCumulatedTime();
    System.out.println("SelectiveService: .............. done after " + endTime + " for method " + methodName);  
    return reply;
  }


  /**
   * Check wether we can send the answer to the request
   * which has been served
   * More precisely, we send the request if otherRequest is not
   * currently in the requestQueue  
   */
  protected boolean sendAnswer(Body body, String otherRequest) {
    return (! body.getRequestQueue().hasRequest(otherRequest));
  }
}
