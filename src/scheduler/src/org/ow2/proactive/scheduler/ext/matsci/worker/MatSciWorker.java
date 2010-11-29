package org.ow2.proactive.scheduler.ext.matsci.worker;

import org.ow2.proactive.scheduler.common.task.TaskResult;

import java.io.Serializable;


/**
 * MatSciWorker
 *
 * @author The ProActive Team
 */
public interface MatSciWorker {

    Serializable execute(int index, TaskResult... results) throws Throwable;

    boolean terminate();

    boolean pack();
}
