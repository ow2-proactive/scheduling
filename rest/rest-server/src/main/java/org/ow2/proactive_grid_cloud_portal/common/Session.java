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
package org.ow2.proactive_grid_cloud_portal.common;

import java.security.KeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.principals.ShadowCredentialsPrincipal;
import org.ow2.proactive.core.properties.PASharedProperties;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive_grid_cloud_portal.dataspace.FileSystem;
import org.ow2.proactive_grid_cloud_portal.dataspace.SchedulerDataspaceImpl;
import org.ow2.proactive_grid_cloud_portal.scheduler.JobsOutputController;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;


public class Session {

    private static final Logger logger = ProActiveLogger.getLogger(Session.class);

    private long lastAccessTimestamp;

    private String sessionId;

    private SchedulerRMProxyFactory schedulerRMProxyFactory;

    private Clock clock;

    private SchedulerProxyUserInterface scheduler;

    private SchedulerSpaceInterface space;

    private RMProxyUserInterface rm;

    private String userName;

    private CredData credData;

    private Credentials credentials;

    private FileSystem fs;

    private static PrivateKey corePrivateKey;

    private static PublicKey corePublicKey;

    public Session(String sessionId, SchedulerRMProxyFactory schedulerRMProxyFactory, Clock clock) {
        this.sessionId = sessionId;
        this.schedulerRMProxyFactory = schedulerRMProxyFactory;
        this.clock = clock;
        initPrivateKey();
        updateLastAccessedTime();
    }

    private static synchronized void initPrivateKey() {
        if (corePrivateKey == null) {
            try {
                corePrivateKey = Credentials.getPrivateKey(PASharedProperties.getAbsolutePath(PASharedProperties.AUTH_PRIVKEY_PATH.getValueAsString()));
            } catch (Exception e) {
                logger.error("Could not initialize private key", e);
            }
        }
        if (corePublicKey == null) {
            try {
                corePublicKey = Credentials.getPublicKey(PASharedProperties.getAbsolutePath(PASharedProperties.AUTH_PUBKEY_PATH.getValueAsString()));
            } catch (Exception e) {
                logger.error("Could not initialize public key", e);
            }
        }
    }

    private void updateLastAccessedTime() {
        this.lastAccessTimestamp = clock.now();
    }

    public SchedulerProxyUserInterface getScheduler() {
        updateLastAccessedTime();
        if (scheduler == null) {
            try {
                if (credData != null) {
                    connectToScheduler(credData);
                } else if (credentials != null) {
                    connectToScheduler(credentials);
                }
            } catch (Exception e) {
                logger.warn("Failed to connect to the Scheduler", e);
                throw new RuntimeException(e);
            }
        }
        return scheduler;
    }

    private void updateCredentials(Credentials credentials, Subject subject) throws LoginException {
        if (subject.getPrincipals(ShadowCredentialsPrincipal.class).iterator().hasNext()) {
            updateShadowCredentials(subject);
        } else {
            this.credentials = credentials;
            try {
                this.credData = this.credentials.decrypt(corePrivateKey);
            } catch (Exception e) {
                logger.error("Could not decrypt user credentials", e);
                throw new LoginException("Could not decrypt user credentials: " + e.getMessage());
            }
        }
    }

    private void updateCredentials(CredData credData, Subject subject) throws LoginException {
        if (subject != null && subject.getPrincipals(ShadowCredentialsPrincipal.class).iterator().hasNext()) {
            updateShadowCredentials(subject);
        } else {
            this.credData = credData;
            try {
                this.credentials = Credentials.createCredentials(credData, corePublicKey);
            } catch (Exception e) {
                logger.error("Could not decrypt user credentials", e);
                throw new LoginException("Could not decrypt user credentials: " + e.getMessage());
            }
        }
    }

    private void updateShadowCredentials(Subject subject) throws LoginException {
        ShadowCredentialsPrincipal shadowCredentialsPrincipal = subject.getPrincipals(ShadowCredentialsPrincipal.class)
                                                                       .iterator()
                                                                       .next();
        try {
            this.credentials = Credentials.getCredentialsBase64(shadowCredentialsPrincipal.getCredentials());
            this.credData = this.credentials.decrypt(corePrivateKey);
        } catch (Exception e) {
            logger.error("Could not decrypt user credentials", e);
            throw new LoginException("Could not decrypt user credentials: " + e.getMessage());
        }
    }

    public void connectToScheduler(Credentials credentials)
            throws LoginException, ActiveObjectCreationException, SchedulerException, NodeException, KeyException {

        scheduler = schedulerRMProxyFactory.connectToScheduler(credentials);

        updateCredentials(credentials, scheduler.getSubject());
        setUserName(scheduler.getCurrentUser());
    }

    public void connectToScheduler(CredData credData)
            throws LoginException, ActiveObjectCreationException, SchedulerException, NodeException {
        scheduler = schedulerRMProxyFactory.connectToScheduler(credData);

        updateCredentials(credData, scheduler.getSubject());
        setUserName(credData.getLogin());
    }

    public void connectToRM(Credentials credentials)
            throws LoginException, ActiveObjectCreationException, KeyException, NodeException, RMException {
        rm = schedulerRMProxyFactory.connectToRM(credentials);

        updateCredentials(credentials, rm.getCurrentUserSubject());
        setUserName(rm.getCurrentUser().getStringValue());
    }

    public void connectToRM(CredData credData)
            throws LoginException, ActiveObjectCreationException, KeyException, NodeException, RMException {
        rm = schedulerRMProxyFactory.connectToRM(credData);

        updateCredentials(credData, rm.getCurrentUserSubject());
        setUserName(credData.getLogin());
    }

    public RMProxyUserInterface getRM() {
        updateLastAccessedTime();
        if (rm == null) {
            try {
                if (credData != null) {
                    connectToRM(credData);
                } else if (credentials != null) {
                    connectToRM(credentials);
                }
            } catch (Exception e) {
                logger.warn("Failed to connect to the RM", e);
                throw new RuntimeException(e);
            }
        }
        return rm;
    }

    public SchedulerSpaceInterface getSpace() throws NotConnectedRestException {
        updateLastAccessedTime();
        if (space == null) {
            space = new SchedulerDataspaceImpl(sessionId);
        }
        return space;
    }

    public String getUserName() {
        return userName;
    }

    public CredData getCredData() {
        return credData;
    }

    protected void setUserName(String userName) {
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
        try {
            if (scheduler.isConnected()) {
                scheduler.disconnect();
            }
        } catch (Exception ignored) {
        }
        try {
            if (rm.isActive().getBooleanValue()) {
                rm.disconnect();
            }
        } catch (Exception ignored) {
        }
        terminateActiveObject(rm);
        terminateActiveObject(scheduler);
        jobsOutputController.terminate();
        logger.debug("Session " + sessionId + " of user " + userName + " terminated");
    }

    private void terminateActiveObject(Object activeObject) {
        if (activeObject != null) {
            try {
                PAActiveObject.terminateActiveObject(activeObject, true);
            } catch (Throwable e) {
                logger.warn("Error occurred while terminating active object tied to session " + sessionId, e);
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

    public void renewSession() throws NotConnectedException {
        updateLastAccessedTime();
        if (scheduler != null) {
            scheduler.renewSession();
        }
    }

    /*
     * For testing purposes only.
     */
    protected void setScheduler(SchedulerProxyUserInterface scheduler) {
        this.scheduler = scheduler;
    }

}
