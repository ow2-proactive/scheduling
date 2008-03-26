package org.objectweb.proactive.core.body.exceptions;

/**
 * Exception thrown by inactive objects when receiving a request.
 * @author The ProActive Team
 * @since 4.0
 */
public class BodyTerminatedRequestException extends BodyTerminatedException {
    private static final String TERMINATED_BODY_REQUEST = " while receiving request ";

    public BodyTerminatedRequestException(String objectName, String methodName) {
        super(objectName, TERMINATED_BODY_REQUEST + methodName);
    }

}
