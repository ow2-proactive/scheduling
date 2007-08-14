package org.objectweb.proactive.core.body;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;


// end inner class LocalInactiveBody
public class InactiveBodyException extends ProActiveRuntimeException {
    public InactiveBodyException(UniversalBody body) {
        super("Cannot perform this call because body " + body.getID() +
            "is inactive");
    }

    public InactiveBodyException(UniversalBody body, String nodeURL,
        UniqueID id, String remoteMethodCallName) {
        // TODO when the class of the remote reified object will be available through UniversalBody, add this info.
        super("Cannot send request \"" + remoteMethodCallName +
            "\" to Body \"" + id + "\" located at " + nodeURL +
            " because body " + body.getID() + " is inactive");
    }

    public InactiveBodyException(UniversalBody body, String localMethodName) {
        super("Cannot serve method \"" + localMethodName + "\" because body " +
            body.getID() + " is inactive");
    }

    public InactiveBodyException(String string, Throwable e) {
        super(string, e);
    }
}
