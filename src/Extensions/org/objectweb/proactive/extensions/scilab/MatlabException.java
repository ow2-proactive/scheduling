package org.objectweb.proactive.extensions.scilab;

public class MatlabException extends TaskException {

    /**
     *
     */
    private static final long serialVersionUID = -677631385223840714L;

    public MatlabException(String message) {
        super(message);
    }

    public MatlabException(Throwable cause) {
        super(cause);
    }

    public MatlabException(String message, Throwable cause) {
        super(message, cause);
    }
}
