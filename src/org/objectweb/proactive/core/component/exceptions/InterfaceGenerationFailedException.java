/*
 * Created on Jan 22, 2004
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.exceptions;


/**
 * This exception occurs when the generation of component interfaces fails.
 *
 * @author Matthieu Morel
 */
public class InterfaceGenerationFailedException extends Exception {

    /**
     * constructor
     */
    public InterfaceGenerationFailedException() {
        super();
    }

    /**
     * @param message the error message
     */
    public InterfaceGenerationFailedException(String message) {
        super(message);
    }

    /**
     * @param message the error message
     * @param cause the cause ...
     */
    public InterfaceGenerationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public InterfaceGenerationFailedException(Throwable cause) {
        super(cause);
    }
}
