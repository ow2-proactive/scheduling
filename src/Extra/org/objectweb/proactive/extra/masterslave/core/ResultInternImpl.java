package org.objectweb.proactive.extra.masterslave.core;

import java.io.Serializable;

import org.objectweb.proactive.extra.masterslave.interfaces.internal.Identified;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;


public class ResultInternImpl implements ResultIntern {
    // The id of the task
    private long id = -1;

    // the result
    private Serializable result = null;

    // when this task has thrown an exception
    private boolean isException = false;

    // the exception thrown
    private Throwable exception = null;

    public ResultInternImpl(TaskIntern task) {
        this.id = task.getId();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof Identified) {
            return id == ((Identified) obj).getId();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern#getException()
     */
    public Throwable getException() {
        return exception;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.Identified#getId()
     */
    public long getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern#getResult()
     */
    public Serializable getResult() {
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (int) id;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern#setException(java.lang.Throwable)
     */
    public void setException(Throwable e) {
        if (e == null) {
            throw new IllegalArgumentException("Exception can't be null");
        }
        this.exception = e;
        this.isException = true;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern#setResult(java.io.Serializable)
     */
    public void setResult(Serializable res) {
        this.result = res;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern#threwException()
     */
    public boolean threwException() {
        return isException;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (o instanceof Identified) {
            return (int) (id - ((Identified) o).getId());
        } else {
            throw new IllegalArgumentException("" + o);
        }
    }

    public String toString() {
        return "ID: " + id + " Result: " + result + " Exception: " + exception;
    }
}
