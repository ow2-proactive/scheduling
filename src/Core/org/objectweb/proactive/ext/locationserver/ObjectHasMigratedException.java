package org.objectweb.proactive.ext.locationserver;

import java.io.IOException;


public class ObjectHasMigratedException extends IOException {

    /**
     *
     */
    private static final long serialVersionUID = 9164402888263834139L;

    public ObjectHasMigratedException(String message) {
        super(message);
    }
}
