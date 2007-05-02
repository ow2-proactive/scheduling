package org.objectweb.proactive.extra.masterslave.interfaces.internal;

import java.io.Serializable;

import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;


/**
 * An interface which wraps a Task and its result in the same object
 * @author fviale
 *
 */
public interface ResultIntern {

    /**
     * returns the result of a computation
     * @return a serializable result
     */
    public abstract Serializable getResult();

    /**
     * sets the result of a computation
     * @param result
     */
    public abstract void setResult(Serializable result);

    /**
     * Returns the task associated
     * @return the task
     */
    public abstract TaskIntern getTask();

    /**
     * Sets the task associated
     * @param task
     */
    public abstract void setTask(TaskIntern task);
}
