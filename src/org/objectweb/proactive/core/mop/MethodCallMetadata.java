package org.objectweb.proactive.core.mop;

import java.io.Serializable;


/* This class is optimized so that its instances are immutable */
public class MethodCallMetadata implements Serializable {

    /**
     * If the caller catches some RuntimeException, we have to wait for all calls
     * generated in the block at its end because any of these calls could throw one.
     */
    private boolean runtimeExceptionHandled;

    /**
     * If the caller told ProActive to catch all the thrown exceptions, we can
     * make the call asynchronous.
     */
    private boolean exceptionAsynchronously;

    /**
     * The default parameters, when the exception mechanism is not used.
     */
    public static final MethodCallMetadata DEFAULT = new MethodCallMetadata(false,
            false);

    /**
     * @param runtimeExceptionHandled
     * @param exceptionAsynchronously
     */
    public MethodCallMetadata(boolean runtimeExceptionHandled,
        boolean exceptionAsynchronously) {
        this.runtimeExceptionHandled = runtimeExceptionHandled;
        this.exceptionAsynchronously = exceptionAsynchronously;
    }

    /**
     * @return Returns the exceptionAsynchronously.
     */
    public boolean isExceptionAsynchronously() {
        return exceptionAsynchronously;
    }

    /**
     * @return Returns the runtimeExceptionHandled.
     */
    public boolean isRuntimeExceptionHandled() {
        return runtimeExceptionHandled;
    }

    public static MethodCallMetadata optimize(MethodCallMetadata metadata) {
        if (DEFAULT.equals(metadata)) {
            metadata = null;
        }

        return metadata;
    }

    public String toString() {
        return "[rt:" + runtimeExceptionHandled + ", async:" +
        exceptionAsynchronously + "]";
    }
}
