package modelisation.timedrequest;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.mop.MethodCall;


public class TimedRequest extends RequestImpl implements java.io.Serializable {
    //transient, will be false after a migration
    protected transient boolean shouldMesureTime;

    public TimedRequest(MethodCall methodCall, UniversalBody sourceBody,
        boolean isOneWay, long sequenceID) {
        super(methodCall, sourceBody, isOneWay, sequenceID);
        this.shouldMesureTime = true;
    }

    protected void sendRequest(UniversalBody destinationBody)
        throws java.io.IOException {
        long startTime = 0;

        //        if (shouldMesureTime) {
        startTime = System.currentTimeMillis();
        //        }
        try {
            destinationBody.receiveRequest(this);
            if (shouldMesureTime) {
                System.out.println("TimedRequest: done after " +
                    (System.currentTimeMillis() - startTime));
            } else {
                System.out.println("TimedRequest: partial is  " +
                    (System.currentTimeMillis() - startTime));
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyReception(UniversalBody bodyReceiver)
        throws java.io.IOException {
        if (hasBeenForwarded()) {
            System.out.println("TimedRequest: the request has been forwarded " +
                (sendCounter - 1));
        }
        super.notifyReception(bodyReceiver);
    }
}
