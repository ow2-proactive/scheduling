package org.ow2.proactive.db;

public interface FilteredExceptionCallback {

    /**
     * Notify the listener when a filtered exception is detected.
     * In such a case, this method is called on the listener. Argument is the exception that
     * caused this call wrapped in a DatabaseManagerException.
     * <p>
     * Note: To get the real exception, just get the cause of the given exception.
     *
     * @param dme the DatabaseManagerException containing the cause.
     */
    void notify(DatabaseManagerException dme);

}