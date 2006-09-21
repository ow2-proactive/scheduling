/**
 *
 */
package org.objectweb.proactive.scheduler;

import org.objectweb.proactive.core.ProActiveException;


/**
 * This exception is obtained when attempting to insert a job to the queue
 * and that the queue is full.
 * @author cjarjouh
 *
 */
public class QueueFullException extends ProActiveException {

    /**
     *
     */
    public QueueFullException() {
        super();

        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public QueueFullException(String message) {
        super(message);

        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public QueueFullException(String message, Throwable cause) {
        super(message, cause);

        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public QueueFullException(Throwable cause) {
        super(cause);

        // TODO Auto-generated constructor stub
    }
}
