/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job.factories;

import java.util.ArrayList;
import java.util.List;

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

    private List<String> tasks;

    /**
     * Default constructor
     * 
     * @param reason informative message
     */
    public FlowError(String reason) {
        super(reason);
        this.tasks = new ArrayList<String>();
    }

    /**
     * Default constructor
     * 
     * @param reason informative message
     * @param e chained exception
     */
    public FlowError(String reason, Exception e) {
        super(reason, e);
        this.tasks = new ArrayList<String>();
    }

    /**
     * Gives hint to the probable cause of the error by adding
     * task names as String to this FlowError.
     * 
     * @param cause add this task name to the causes
     */
    public void addTask(String cause) {
        tasks.add(cause);
    }

    /**
     * @return a List of String representing Task names hinting for a probable
     * cause for the error
     */
    public List<String> getTasks() {
        return this.tasks;
    }

}
