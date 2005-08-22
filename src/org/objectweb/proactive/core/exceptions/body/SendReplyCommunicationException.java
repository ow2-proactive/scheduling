package org.objectweb.proactive.core.exceptions.body;

import java.io.IOException;


public class SendReplyCommunicationException extends BodyNonFunctionalException {
    public SendReplyCommunicationException(String message, IOException e) {
        super(message, e);
    }
}
