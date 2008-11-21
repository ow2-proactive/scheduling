/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Definition of the common attributes between job and task.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
public abstract class CommonAttribute implements Serializable {

    /** 
     * Do the job has to cancel when an exception occurs in a task. (default is false) <br />
     * You can override this property inside each task.
     */
    private UpdatableProperties<Boolean> cancelJobOnError = new UpdatableProperties<Boolean>(false);

    /** 
     * Where will a task be restarted if an error occurred. (default is ANYWHERE)<br />
     * It will be restarted according to the number of execution remaining.<br />
     * You can override this property inside each task.
     */
    private UpdatableProperties<RestartMode> restartTaskOnError = new UpdatableProperties<RestartMode>(
        RestartMode.ANYWHERE);

    /**
     * The maximum number of execution for a task (default 1). <br />
     * You can override this property inside each task.
     */
    private UpdatableProperties<Integer> maxNumberOfExecution = new UpdatableProperties<Integer>(1);

    /**
     * To get the cancelOnError
     *
     * @return the cancelOnError
     */
    public boolean isCancelJobOnError() {
        return cancelJobOnError.getValue();
    }

    /**
     * Set to true if you want to cancel the job when an exception occurs in a task. (Default is false)
     *
     * @param cancelJobOnError the cancelJobOnError to set
     */
    public void setCancelJobOnError(boolean cancelJobOnError) {
        this.cancelJobOnError.setValue(cancelJobOnError);
    }

    /**
     * Get the cancelJobOnError updatable property.
     * 
     * @return the cancelJobOnError updatable property.
     */
    public UpdatableProperties<Boolean> getCancelJobOnErrorProperty() {
        return cancelJobOnError;
    }

    /**
     * Returns the restartTaskOnError state.
     * 
     * @return the restartTaskOnError state.
     */
    public RestartMode getRestartTaskOnError() {
        return restartTaskOnError.getValue();
    }

    /**
     * Sets the restartTaskOnError to the given restartOnError value. (Default is 'ANYWHERE')
     *
     * @param restartOnError the restartOnError to set.
     */
    public void setRestartTaskOnError(RestartMode restartOnError) {
        this.restartTaskOnError.setValue(restartOnError);
    }

    /**
     * Get the restartTaskOnError updatable property.
     * 
     * @return the restartTaskOnError updatable property.
     */
    public UpdatableProperties<RestartMode> getRestartTaskOnErrorProperty() {
        return restartTaskOnError;
    }

    /**
     * Get the number of execution allowed for this task.
     * 
     * @return the number of execution allowed for this task.
     */
    public int getMaxNumberOfExecution() {
        return maxNumberOfExecution.getValue();
    }

    /**
     * To set the number of execution for this task. (Default is 1)
     *
     * @param the number of times this task can be executed.
     */
    public void setMaxNumberOfExecution(int numberOfExecution) {
        if (numberOfExecution <= 0) {
            throw new IllegalArgumentException(
                "The number of execution must be a non negative integer (>0) !");
        }
        this.maxNumberOfExecution.setValue(numberOfExecution);
    }

    /**
     * Get the maximum number Of Execution updatable property.
     * 
     * @return the maximum number Of Execution updatable property.
     */
    public UpdatableProperties<Integer> getMaxNumberOfExecutionProperty() {
        return maxNumberOfExecution;
    }

}
