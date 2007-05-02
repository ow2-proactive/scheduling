package org.objectweb.proactive.extra.masterslave.core;

import java.io.Serializable;

import org.objectweb.proactive.extra.masterslave.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;


public class ResultInternImpl implements Serializable, ResultIntern {
    private Serializable result;
    private TaskIntern task;

    public ResultInternImpl(Serializable result, TaskIntern task) {
        this.result = result;
        this.task = task;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.core.ResultIntern#getResult()
     */
    public Serializable getResult() {
        return result;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.core.ResultIntern#setResult(java.io.Serializable)
     */
    public void setResult(Serializable result) {
        this.result = result;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.core.ResultIntern#getTask()
     */
    public TaskIntern getTask() {
        return task;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.core.ResultIntern#setTask(org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern)
     */
    public void setTask(TaskIntern task) {
        this.task = task;
    }
}
