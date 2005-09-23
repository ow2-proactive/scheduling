package org.objectweb.proactive.core.exceptions.manager;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;


public class TypedNFEListener implements NFEListener {
    private Class exceptionClass;
    private NFEListener listener;
    private boolean isRootNFEClass;

    public TypedNFEListener(Class exceptionClass, NFEListener listener) {
        this.exceptionClass = exceptionClass;
        this.listener = listener;
        this.isRootNFEClass = exceptionClass == NonFunctionalException.class;

        if (!NonFunctionalException.class.isAssignableFrom(exceptionClass)) {
            throw new IllegalArgumentException(
                "The exception class must be a NFE class");
        }
    }

    public boolean canHandleNFE(NonFunctionalException e) {
        return (isRootNFEClass || exceptionClass.isInstance(e)) &&
        listener.canHandleNFE(e);
    }

    public void handleNFE(NonFunctionalException e) {
        listener.handleNFE(e);
    }
}
