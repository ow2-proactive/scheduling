package org.objectweb.proactive.core.component.representative;

import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.MethodCall;

/**
 * Associates a functional interface with the proxy to an active object.
 * 
 * @author Matthieu Morel
 */
public interface FunctionalInterfaceProxy  extends Proxy {

    //public void setRefOnBody(UniversalBody body, UniqueID bodyID);

    public abstract Object reify(MethodCall c) throws Throwable;

    public void setBodyProxy(Proxy proxy);

    public Proxy getBodyProxy();

}
