package org.objectweb.proactive.extra.masterslave.core;

import java.io.Serializable;

import org.objectweb.proactive.extra.masterslave.interfaces.internal.Identifiable;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * A result of a task, contains the result itself, or an exception if one has been thrown
 * @author fviale
 *
 */
public class ResultInternImpl implements ResultIntern<Serializable> {

    /**
         *
         */
    private static final long serialVersionUID = 8247315893656368289L;

    /**
    * The id of the task
    */
    protected long id = -1;

    /**
     * the result
     */
    protected Serializable result = null;

    /**
     * when this task has thrown an exception
     */
    protected boolean isException = false;

    /**
     *  the exception thrown
     */
    protected Throwable exception = null;

    /**
     * Creates an empty result object for the given task
     * @param task task associated with the result
     */
    public ResultInternImpl(TaskIntern<Serializable> task) {
        this.id = task.getId();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        if (obj instanceof Identifiable) {
            return id == ((Identifiable) obj).getId();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * {@inheritDoc}
     */
    public long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public Serializable getResult() {
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (int) id;
    }

    /**
     * {@inheritDoc}
     */
    public void setException(final Throwable e) {
        if (e == null) {
            throw new IllegalArgumentException("Exception can't be null");
        }
        this.exception = e;
        this.isException = true;
    }

    /**
     * {@inheritDoc}
     */
    public void setResult(final Serializable res) {
        this.result = res;
    }

    /**
     * {@inheritDoc}
     */
    public boolean threwException() {
        return isException;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Identifiable o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return (int) (id - ((Identifiable) o).getId());
    }

    public String toString() {
        return "ID: " + id + " Result: " + result + " Exception: " + exception;
    }
}
