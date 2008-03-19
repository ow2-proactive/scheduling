package org.objectweb.proactive.core.body.exceptions;

/**
 * Exception thrown by inactive objects when sending a reply.
 * @author fviale
 * @since 4.0
 */
public class BodyTerminatedReplyException extends BodyTerminatedException {
    private static final String TERMINATED_BODY_REPLY = " while replying to request ";

    public BodyTerminatedReplyException(String objectName, String methodName) {
        super(objectName, TERMINATED_BODY_REPLY + methodName);
    }
}
