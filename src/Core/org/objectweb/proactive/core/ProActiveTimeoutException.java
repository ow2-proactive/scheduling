package org.objectweb.proactive.core;

import java.util.concurrent.TimeoutException;

import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
/**
 * Exception thrown when a blocking operation times out. Blocking operations for which a timeout is
 * specified need a means to indicate that the timeout has occurred. For many such operations it is
 * possible to return a value that indicates timeout; when that is not possible or desirable then
 * <tt>TimeoutException</tt> should be declared and thrown.
 * 
 * @since 4.0
 */
public class ProActiveTimeoutException extends Exception {

    public ProActiveTimeoutException() {
        super();
    }

    public ProActiveTimeoutException(String message) {
        super(message);
    }

    public ProActiveTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProActiveTimeoutException(Throwable cause) {
        super(cause);
    }

}
