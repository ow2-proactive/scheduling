package org.objectweb.proactive.extensions.scilab;

public class TaskException extends Exception {

    /**
         *
         */
    private static final long serialVersionUID = -5925533409979930415L;

    public TaskException() {
        super();
    }

    public TaskException(String message) {
        super(message);
    }

    public TaskException(Throwable cause) {
        super(cause);
    }

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
