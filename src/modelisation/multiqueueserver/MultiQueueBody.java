package modelisation.multiqueueserver;


import org.objectweb.proactive.core.body.BodyImpl;
//import org.objectweb.proactive.core.body.server.RequestServer;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;


public class MultiQueueBody extends BodyImpl {

  public MultiQueueBody() {}

  public MultiQueueBody(ConstructorCall c, String nodeName) throws java.lang.reflect.InvocationTargetException, org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException {
    super(c, nodeName);
  }

 //  protected RequestServer createRequestServer() {
//      return new MultipleQueueSelectiveService();
//   }

    protected BlockingRequestQueue createRequestQueue() {
	return new CompositeRequestQueue(bodyID);
    }
}
