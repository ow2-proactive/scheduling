package modelisation.forwarder;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;

import java.io.Serializable;

public class TimedProxyForBodyWithForwarder extends UniversalBodyProxy implements Serializable {

    public TimedProxyForBodyWithForwarder(ConstructorCall c, Object[] p) throws ProActiveException {
        super(c, p);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    protected void sendRequestInternal(MethodCall methodCall, Future future, Body sourceBody) throws java.io.IOException {
        //For benchmarking purpose
        long startTime = System.currentTimeMillis();
        super.sendRequestInternal(methodCall, future, sourceBody);
        long endTime = System.currentTimeMillis();
        if (!methodCall.getName().equals("start"))
            System.out.println(System.currentTimeMillis() + " TimedProxyForBodyWithForwarder:  .............. done after " + (endTime - startTime));
    }

}
