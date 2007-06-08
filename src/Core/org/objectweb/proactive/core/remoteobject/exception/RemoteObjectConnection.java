package org.objectweb.proactive.core.remoteobject.exception;

import org.objectweb.proactive.core.ProActiveException;


public class RemoteObjectConnection extends ProActiveException {
    public RemoteObjectConnection() {
        super();
    }

    public RemoteObjectConnection(String message) {
        super(message);
    }

    public RemoteObjectConnection(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteObjectConnection(Throwable cause) {
        super(cause);
    }
}
