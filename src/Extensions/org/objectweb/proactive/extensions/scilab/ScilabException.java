package org.objectweb.proactive.extensions.scilab;

public class ScilabException extends TaskException {

    /**
     *
     */
    private static final long serialVersionUID = -677631385223840714L;

    public ScilabException(String message) {
        super(message);
    }

    public ScilabException(Throwable cause) {
        super(cause);
    }

    public ScilabException(String message, Throwable cause) {
        super(message, cause);
    }
}
