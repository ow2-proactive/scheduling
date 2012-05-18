package org.ow2.proactive.scheduler.ext.matsci.client.common.exception;

import org.ow2.proactive.scheduler.ext.common.util.StackTraceUtil;


/**
 * PASchedulerException
 *
 * This class encapsulate ProActive Scheduler exceptions and transform them as Strings (for classloading issues).
 *
 * @author The ProActive Team
 */
public class PASchedulerException extends RuntimeException {

    private static final long serialVersionUID = 32L;

    private ExceptionType type;

    public enum ExceptionType {
        KeyException, LoginException, SchedulerException, PermissionException, NotConnectedException, AlreadyConnectedException, UnknownJobException, UnknownTaskException
    }

    public PASchedulerException() {
        super();
    }

    public PASchedulerException(ExceptionType type) {
        super();
        this.type = type;
    }

    public PASchedulerException(String message) {
        super(message);
    }

    public PASchedulerException(String message, ExceptionType type) {
        super(message);
        this.type = type;
    }

    public PASchedulerException(String message, Throwable cause) {
        super(message + "\nCaused By:\n" + StackTraceUtil.getStackTrace(cause));
    }

    public PASchedulerException(String message, Throwable cause, ExceptionType type) {
        super(message + "\nCaused By:\n" + StackTraceUtil.getStackTrace(cause));
        this.type = type;
    }

    public PASchedulerException(Throwable cause) {
        super(StackTraceUtil.getStackTrace(cause));
    }

    public PASchedulerException(Throwable cause, ExceptionType type) {
        super(StackTraceUtil.getStackTrace(cause));
        this.type = type;
    }

    public ExceptionType getType() {
        return type;
    }

}
