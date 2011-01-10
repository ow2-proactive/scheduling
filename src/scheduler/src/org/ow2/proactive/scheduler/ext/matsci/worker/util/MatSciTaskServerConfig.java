/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matsci.worker.util;

import java.io.Serializable;


/**
 * MatSciTaskServerConfig
 *
 * @author The ProActive Team
 */
public class MatSciTaskServerConfig implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;
	private boolean deployIoThread;
    private int taskCountBeforeJVMRespawn;
    private int taskCountBeforeJVMRespawnWindows;
    private long semaphoreTimeout;
    private int semaphoreRetryAquire;
    private int maxNbAttempts;

    public MatSciTaskServerConfig(boolean deployIoThread, int taskCountBeforeJVMRespawn,
            int taskCountBeforeJVMRespawnWindows, long semaphoreTimeout, int semaphoreRetryAquire,
            int maxNbAttempts) {
        this.deployIoThread = deployIoThread;
        this.taskCountBeforeJVMRespawn = taskCountBeforeJVMRespawn;
        this.taskCountBeforeJVMRespawnWindows = taskCountBeforeJVMRespawnWindows;
        this.semaphoreTimeout = semaphoreTimeout;
        this.semaphoreRetryAquire = semaphoreRetryAquire;
        this.maxNbAttempts = maxNbAttempts;
    }

    public boolean isDeployIoThread() {
        return deployIoThread;
    }

    public int getTaskCountBeforeJVMRespawn() {
        return taskCountBeforeJVMRespawn;
    }

    public int getTaskCountBeforeJVMRespawnWindows() {
        return taskCountBeforeJVMRespawnWindows;
    }

    public long getSemaphoreTimeout() {
        return semaphoreTimeout;
    }

    public int getSemaphoreRetryAquire() {
        return semaphoreRetryAquire;
    }

    public int getMaxNbAttempts() {
        return maxNbAttempts;
    }
}
