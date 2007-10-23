package org.objectweb.proactive.extra.scheduler.ext.matlab.exception;

import org.objectweb.proactive.extra.scheduler.common.task.Task;


public class IllegalTaskException extends Exception {
    public IllegalTaskException(Class<?extends Task> class1) {
        super(class1.getCanonicalName());
    }
}
