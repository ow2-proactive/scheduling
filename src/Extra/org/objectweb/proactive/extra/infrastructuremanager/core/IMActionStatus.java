package org.objectweb.proactive.extra.infrastructuremanager.core;

import java.io.Serializable;


/**
 * @deprecated
 */
public class IMActionStatus implements Serializable {
    // Attributes
    private boolean successAction;
    private String status;
    private Exception exception;

    //----------------------------------------------------------------------//
    // Constructors

    /** ProActive compulsory no-args constructor */
    public IMActionStatus() {
    }

    public IMActionStatus(String status) {
        this.successAction = true;
        this.status = status;
    }

    public IMActionStatus(String status, Exception exception) {
        this.successAction = false;
        this.status = status;
        this.exception = exception;
    }

    //----------------------------------------------------------------------//
    // Accessors
    public boolean isSuccessAction() {
        return successAction;
    }

    public String getStatus() {
        return status;
    }

    public Exception getException() {
        return exception;
    }

    //----------------------------------------------------------------------//
    public String toString() {
        String actionStatus;
        if (this.successAction) {
            actionStatus = "success";
        } else {
            actionStatus = "failure";
        }
        return "Action " + actionStatus + " : " + this.status;
    }
}
