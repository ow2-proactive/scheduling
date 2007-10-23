package org.objectweb.proactive.extra.scheduler.ext.matlab.exception;

import ptolemy.data.Token;


public class IllegalReturnTypeException extends Exception {
    public IllegalReturnTypeException(Class<?extends Token> class1) {
        super(class1.getCanonicalName());
    }
}
