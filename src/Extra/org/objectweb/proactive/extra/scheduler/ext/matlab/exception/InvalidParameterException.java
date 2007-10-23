package org.objectweb.proactive.extra.scheduler.ext.matlab.exception;

import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;

import ptolemy.data.type.Type;


public class InvalidParameterException extends Exception {
    public InvalidParameterException(Class<?> class1) {
        super(class1.getCanonicalName());
    }

    public InvalidParameterException(Type type, Type type2) {
        super("Type received " + type + " ... expected " + type2);
    }
}
