package org.objectweb.proactive.core.exceptions.manager;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;


public class TypedNFEListener implements NFEListener {
    private Class exceptionClass;
    private NFEListener listener;

    public TypedNFEListener(Class exceptionClass, NFEListener listener) {
        this.exceptionClass = exceptionClass;
        this.listener = listener;

        if (!NonFunctionalException.class.isAssignableFrom(exceptionClass)) {
            throw new IllegalArgumentException(
                "The exception class must be a NFE class");
        }
    }

    public boolean handleNFE(NonFunctionalException e) {
        if (exceptionClass.isInstance(e)) {
            return listener.handleNFE(e);
        }

        return false;
    }
}
