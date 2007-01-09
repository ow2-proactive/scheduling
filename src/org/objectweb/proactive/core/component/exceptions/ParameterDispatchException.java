package org.objectweb.proactive.core.component.exceptions;

/**
 * Exception thrown if parameters cannot be dispatched from or to a collective interface.
 * 
 * @author Matthieu Morel
 *
 */
public class ParameterDispatchException extends Exception {

    public ParameterDispatchException() {

        super();
    }

    public ParameterDispatchException(String message, Throwable cause) {

        super(message, cause);
    }

    public ParameterDispatchException(String message) {

        super(message);
    }

    public ParameterDispatchException(Throwable cause) {

        super(cause);
    }

}
