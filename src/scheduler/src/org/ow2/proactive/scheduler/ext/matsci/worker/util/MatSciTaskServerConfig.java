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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;


/**
 * MatSciTaskServerConfig
 *
 * @author The ProActive Team
 */
public class MatSciTaskServerConfig implements Serializable {

    /**  */
    private static final long serialVersionUID = 31L;

    public static String DEPLOY_IO_THREAD = "matsci.deployIOThread";

    public static String TASK_COUNT_JVM_RESPAWN = "matsci.taskcount";

    public static String SEMAPHORE_TIMEOUT = "matsci.semaphore.timeout";

    public static String SEMAPHORE_RETRY = "matsci.semaphore.retry";

    public static String MAX_ATTEMPTS = "matsci.maxattempts";

    private boolean deployIoThread;
    private int taskCountBeforeJVMRespawn;
    private long semaphoreTimeout;
    private int semaphoreRetryAquire;
    private int maxNbAttempts;

    public MatSciTaskServerConfig(boolean deployIoThread, int taskCountBeforeJVMRespawn,
            long semaphoreTimeout, int semaphoreRetryAquire, int maxNbAttempts) {
        this.deployIoThread = deployIoThread;
        this.taskCountBeforeJVMRespawn = taskCountBeforeJVMRespawn;
        this.semaphoreTimeout = semaphoreTimeout;
        this.semaphoreRetryAquire = semaphoreRetryAquire;
        this.maxNbAttempts = maxNbAttempts;
    }

    public static MatSciTaskServerConfig load(File filepath) throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(filepath));
        boolean deployIoThread;
        try {
            deployIoThread = Boolean.parseBoolean(p.getProperty(DEPLOY_IO_THREAD));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while reading property " + DEPLOY_IO_THREAD + " : " +
                p.getProperty(DEPLOY_IO_THREAD));
        }
        int taskCountBeforeJVMRespawn;
        try {
            taskCountBeforeJVMRespawn = Integer.parseInt(p.getProperty(TASK_COUNT_JVM_RESPAWN));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while reading property " + TASK_COUNT_JVM_RESPAWN +
                " : " + p.getProperty(TASK_COUNT_JVM_RESPAWN));
        }
        long semaphoreTimeout;
        try {
            semaphoreTimeout = Long.parseLong(p.getProperty(SEMAPHORE_TIMEOUT));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while reading property " + SEMAPHORE_TIMEOUT + " : " +
                p.getProperty(SEMAPHORE_TIMEOUT));
        }
        int semaphoreRetryAquire;
        try {
            semaphoreRetryAquire = Integer.parseInt(p.getProperty(SEMAPHORE_RETRY));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while reading property " + SEMAPHORE_RETRY + " : " +
                p.getProperty(SEMAPHORE_RETRY));
        }
        int maxNbAttempts;
        try {
            maxNbAttempts = Integer.parseInt(p.getProperty(MAX_ATTEMPTS));

        } catch (Exception e) {
            throw new IllegalArgumentException("Error while reading property " + MAX_ATTEMPTS + " : " +
                p.getProperty(MAX_ATTEMPTS));
        }

        return new MatSciTaskServerConfig(deployIoThread, taskCountBeforeJVMRespawn, semaphoreTimeout,
            semaphoreRetryAquire, maxNbAttempts);

    }

    public boolean isDeployIoThread() {
        return deployIoThread;
    }

    public int getTaskCountBeforeJVMRespawn() {
        return taskCountBeforeJVMRespawn;
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
