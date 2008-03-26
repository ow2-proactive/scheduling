/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.scilab.monitor;

import java.io.File;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.scilab.GeneralResult;
import org.objectweb.proactive.extensions.scilab.GeneralTask;


/**
 * SciTaskInfo contains all methods to access to informations about a Scilab/Matlab task running on the MSService
 */

/**
 * @author The ProActive Team
 *
 */
@PublicAPI
public class GenTaskInfo {
    public static final int LOW = 0;
    public static final int NORMAL = 1;
    public static final int HIGH = 2;
    public static final int SUCCEEDED = 0;
    public static final int ABORTED = 1;
    public static final int PENDING = 2;
    public static final int RUNNING = 3;
    public static final int KILLED = 4;
    public static final int CANCELLED = 5;
    public static final int REMOVED = 6;
    private int priority = NORMAL;
    private int state;
    private String idEngine;
    private File fileScript;
    private GeneralTask genTask;
    private GeneralResult genResult;
    private long dateStart;
    private long dateEnd;

    public GenTaskInfo(GeneralTask genTask) {
        this.genTask = genTask;
        this.dateStart = System.currentTimeMillis();
    }

    /**
     * Get the priority of this task
     * @return
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority of this task
     * @param priority new priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * retrieve the static task definition
     * @return GeneralTask
     */
    public GeneralTask getTask() {
        return genTask;
    }

    /**
     * Retrieves the result of this task
     * @return a general result object
     */
    public GeneralResult getResult() {
        return genResult;
    }

    /**
     * Sets the result of this task
     * @param genResult the result of this task
     */
    public void setResult(GeneralResult genResult) {
        this.genResult = genResult;
    }

    /**
     * Returns the id of this task, with regards to the MSService
     * @return
     */
    public String getIdTask() {
        return genTask.getId();
    }

    /**
     * Returns the state of this task
     * @return the task state
     */
    public int getState() {
        return state;
    }

    /**
     * Sets the state of this task
     * @param state the new task state
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Retrieves the id associated with the engine this task's being run
     * @return engine id
     */
    public String getIdEngine() {
        return idEngine;
    }

    /**
     * Sets the id associated with the engine this task's being run
     * @param idEngine engine id
     */
    public void setIdEngine(String idEngine) {
        this.idEngine = idEngine;
    }

    /**
     * Sets the date when this task was finished
     */
    public void setDateEnd() {
        this.dateEnd = System.currentTimeMillis();
    }

    /**
     * Retrieves the global time necessary for this task to complete
     * @return time (ms)
     */
    public long getTimeGlobal() {
        return this.dateEnd - this.dateStart;
    }

    /**
     * Retrieves the actual computing time
     * @return computing time (ms)
     */
    public long getTimeExecution() {
        return this.genResult.getTimeExecution();
    }

    /**
     * Retrieves the date at which this task started
     * @return
     */
    public long getDateStart() {
        return dateStart;
    }

    /**
     * Retrieves the path to the script associated with this task
     * @return
     */
    public String getPathScript() {
        return fileScript.getAbsolutePath();
    }

    /**
     * Retrieves the file name of the script associated with this task
     * @return
     */
    public String getNameScript() {
        return fileScript.getName();
    }

    /**
     * Sets the script associated with this task
     * @param fileScript
     */
    public void setFileScript(File fileScript) {
        this.fileScript = fileScript;
    }
}
