package org.ow2.proactive.scripting.helper.filetransfer.exceptions;

public class NotInitializedFileTransfertSessionException extends Exception {

    public NotInitializedFileTransfertSessionException() {
        super();
    }

    public NotInitializedFileTransfertSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotInitializedFileTransfertSessionException(String message) {
        super(message);
    }

    public NotInitializedFileTransfertSessionException(Throwable cause) {
        super(cause);
    }

}
