package modelisation;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.future.Future;

import org.objectweb.proactive.core.node.Node;

import org.objectweb.proactive.core.mop.MethodCall;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.lang.reflect.InvocationTargetException;

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
