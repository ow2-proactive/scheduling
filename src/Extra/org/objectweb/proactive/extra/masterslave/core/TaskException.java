package org.objectweb.proactive.extra.masterslave.core;


/**
 * A user exception thrown by a task during its execution
 * @author fviale
 *
 */
public class TaskException extends Exception {
    public TaskException(Throwable cause) {
        initCause(cause);
    }
}
