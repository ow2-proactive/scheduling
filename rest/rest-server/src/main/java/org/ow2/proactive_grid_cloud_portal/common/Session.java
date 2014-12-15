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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.common;

import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.dataspace.FileSystem;
import org.ow2.proactive_grid_cloud_portal.scheduler.JobsOutputController;


public class Session {

    private static final Logger LOGGER = ProActiveLogger.getLogger(Session.class);

    private long lastAccessTimestamp;

    private String sessionId;

    private SchedulerRMProxyFactory schedulerRMProxyFactory;
    private Clock clock;

    private SchedulerProxyUserInterface scheduler;
    private RMProxyUserInterface rm;

    private String userName;

    private CredData credData;
    private Credentials credentials;

    private FileSystem fs;

    public Session(String sessionId, SchedulerRMProxyFactory schedulerRMProxyFactory, Clock clock) {
        this.sessionId = sessionId;
        this.schedulerRMProxyFactory = schedulerRMProxyFactory;
        this.clock = clock;
        updateLasAccessedTime();
    }

    private void updateLasAccessedTime() {
        this.lastAccessTimestamp = clock.now();
    }

    public void connectToScheduler(Credentials credentials) throws LoginException,
            ActiveObjectCreationException, SchedulerException, NodeException {
        scheduler = schedulerRMProxyFactory.connectToScheduler(credentials);
        this.credentials = credentials;
    }

    public void connectToScheduler(CredData credData) throws LoginException, ActiveObjectCreationException,
            SchedulerException, NodeException {
        scheduler = schedulerRMProxyFactory.connectToScheduler(credData);
        this.credData = credData;
    }

    public SchedulerProxyUserInterface getScheduler() {
        updateLasAccessedTime();
        if (scheduler == null) {
            try {
                if (credData != null) {
                    connectToScheduler(credData);
                } else if (credentials != null) {
                    connectToScheduler(credentials);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to connect to the Scheduler", e);
                throw new RuntimeException(e);
            }
        }
        return scheduler;
    }

    public void connectToRM(Credentials credentials) throws LoginException, ActiveObjectCreationException,
            KeyException, NodeException, RMException {
        rm = schedulerRMProxyFactory.connectToRM(credentials);
        this.credentials = credentials;
    }

    public void connectToRM(CredData credData) throws LoginException, ActiveObjectCreationException,
            KeyException, NodeException, RMException {
        rm = schedulerRMProxyFactory.connectToRM(credData);
        this.credData = credData;
    }

    public RMProxyUserInterface getRM() {
        updateLasAccessedTime();
        if (rm == null) {
            try {
                if (credData != null) {
                    connectToRM(credData);
                } else if (credentials != null) {
                    connectToRM(credentials);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to connect to the RM", e);
                throw new RuntimeException(e);
            }
        }
        return rm;
    }

    public String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    private final JobsOutputController jobsOutputController = new JobsOutputController(this);

    public JobsOutputController getJobsOutputController() {
        return jobsOutputController;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void terminate() {
        terminateActiveObject(rm);
        terminateActiveObject(scheduler);
        jobsOutputController.terminate();
    }

    private void terminateActiveObject(Object activeObject) {
        if (activeObject != null) {
            try {
                PAActiveObject.terminateActiveObject(activeObject, true);
            } catch (Throwable e) {
                LOGGER.warn("Error occurred while terminating active object tied to session " + sessionId, e);
            }
        }
    }

    public boolean isExpired(long expirationDelay) {
        return clock.now() - lastAccessTimestamp >= expirationDelay;
    }

    public void fileSystem(FileSystem fs) {
        this.fs = fs;
    }

    public FileSystem fileSystem() {
        return fs;
    }
}
