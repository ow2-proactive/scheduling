/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.job.factories;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Exception thrown upon detection of an invalid workflow,
 * see {@link FlowChecker#validate(org.ow2.proactive.scheduler.common.job.TaskFlowJob)}
 * 
 *  
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 * 
 */
@PublicAPI
public class FlowError extends Exception {

    /** task responsible for the error */
    private String task;

    /**
     * Generic category of the error type,
     * for a specific message use {@link FlowError#getMessage()}
     */
    public enum FlowErrorType {
        /** an IF action is invalid */
        IF,
        /** a REPLICATE action is invalid */
        REPLICATE,
        /** a LOOP action is invalid */
        LOOP,
        /** a BLOCK is invalid */
        BLOCK,
        /** the name of a task is invalid */
        NAME,
        /** a task is unreachable in the flow */
        UNREACHABLE,
        /** infinite loop */
        RECURSION;
    };

    private FlowErrorType errorType;

    /**
     * Default constructor
     * 
     * @param reason informative message hinting how to fix the issue
     * @param errorType general category of the error
     * @param taskName unique name of the task that caused the error
     */
    public FlowError(String reason, FlowErrorType errorType, String taskName) {
        super(reason);
        this.task = taskName;
        this.errorType = errorType;
    }

    /**
     * @return the name of the task the most likely to be responsible for the error
     */
    public String getTask() {
        return this.task;
    }

    /**
     * @return the type of flow error
     */
    public FlowErrorType getErrorType() {
        return this.errorType;
    }

}
