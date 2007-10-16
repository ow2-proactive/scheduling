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

import org.objectweb.proactive.extensions.scilab.GeneralResult;
import org.objectweb.proactive.extensions.scilab.GeneralTask;


/**
 * SciTaskInfo contains all methods to access to informations about a Scilab task
 */
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public GeneralTask getTask() {
        return genTask;
    }

    public GeneralResult getResult() {
        return genResult;
    }

    public void setResult(GeneralResult genResult) {
        this.genResult = genResult;
    }

    public String getIdTask() {
        return genTask.getId();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getIdEngine() {
        return idEngine;
    }

    public void setIdEngine(String idEngine) {
        this.idEngine = idEngine;
    }

    public void setDateEnd() {
        this.dateEnd = System.currentTimeMillis();
    }

    public long getTimeGlobal() {
        return this.dateEnd - this.dateStart;
    }

    public long getTimeExecution() {
        return this.genResult.getTimeExecution();
    }

    public long getDateStart() {
        return dateStart;
    }

    public String getPathScript() {
        return fileScript.getAbsolutePath();
    }

    public String getNameScript() {
        return fileScript.getName();
    }

    public void setFileScript(File fileScript) {
        this.fileScript = fileScript;
    }
}
