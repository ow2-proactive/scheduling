package org.ow2.proactive.scheduler.ext.matsci.client;

/**
 * MatSciTaskStatus
 *
 * @author The ProActive Team
 */
public enum MatSciTaskStatus {
    /**
     * OK, the task completed successfully
     */
    OK("Ok"),
    /**
     * Global misbehaviour (Scheduler is stopped, job has been killed)
     */
    GLOBAL_ERROR("Global Error"),
    /**
     * An error occured in the task outside of the MatSci code. This should be an internal error
     */
    RUNTIME_ERROR("Runtime Error"),
    /**
     * An error occured in the MatSci code
     */
    MATSCI_ERROR("MatSci Error");

    /** The textual definition of the status */
    private String definition;

    /**
     * Default constructor.
     * @param def the textual definition of the status.
     */
    MatSciTaskStatus(String def) {
        definition = def;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return definition;
    }
}
