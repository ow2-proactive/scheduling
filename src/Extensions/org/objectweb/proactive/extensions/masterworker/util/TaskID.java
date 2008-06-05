package org.objectweb.proactive.extensions.masterworker.util;

/**
 * Object containing a task id and the worker name which has submitted the task
 * (null if it's the main client)
 *
 * @author The ProActive Team
 */
public class TaskID {
    private final String originator;
    private final Long id;

    public TaskID(String originator, long id) {
        this.originator = originator;
        this.id = id;
    }

    public String getOriginator() {
        return originator;
    }

    public Long getID() {
        return id;
    }

    public boolean equals(Object obj) {

        if (obj instanceof TaskID) {
            return id.equals(((TaskID) obj).getID());
        } else if (obj instanceof Long) {
            return id.equals(obj);
        }
        return false;
    }

    public int hashCode() {
        return id.hashCode();
    }
}
