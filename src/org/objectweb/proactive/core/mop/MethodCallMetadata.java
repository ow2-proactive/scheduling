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
     * If the caller asks to catch the NFE (which is a RuntimeException btw), we
     * won't trigger the NFE mechanism but simply throw the exception back to the caller.
     * This flag implies runtimeExceptionHandled.
     */
    private boolean NFEHandled;

    /**
     * If the caller told ProActive to catch all the thrown exceptions, we can
     * make the call asynchronous.
     */
    private boolean exceptionAsynchronously;

    /**
     * The default parameters, when the exception mechanism is not used.
     */
    public static final MethodCallMetadata DEFAULT = new MethodCallMetadata(false,
            false, false);

    /**
     * @param runtimeExceptionHandled
     * @param NFEHandled
     * @param asynchronousException
     */
    public MethodCallMetadata(boolean runtimeExceptionHandled,
        boolean NFEHandled, boolean exceptionAsynchronously) {
        this.runtimeExceptionHandled = runtimeExceptionHandled;
        this.NFEHandled = NFEHandled;
        this.exceptionAsynchronously = exceptionAsynchronously;
    }

    /**
     * @return Returns the exceptionAsynchronously.
     */
    public boolean isExceptionAsynchronously() {
        return exceptionAsynchronously;
    }

    /**
     * @return Returns the nFEHandled.
     */
    public boolean isNFEHandled() {
        return NFEHandled;
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
    	return "[rt:" + runtimeExceptionHandled + ", async:" + exceptionAsynchronously + ", nfe:" + NFEHandled + "]";
    }
}
