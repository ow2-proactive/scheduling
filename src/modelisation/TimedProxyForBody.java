package modelisation;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;

public class TimedProxyForBody extends UniversalBodyProxy implements Serializable {

  public TimedProxyForBody(ConstructorCall c, Object[] p) throws ProActiveException {
    super(c, p);
  }

    
  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  protected void sendRequestInternal(MethodCall methodCall, Future future, Body sourceBody) throws java.io.IOException {
    System.out.println("TimedProxyForBody");
    super.sendRequestInternal(methodCall, future, sourceBody);
  }
}
