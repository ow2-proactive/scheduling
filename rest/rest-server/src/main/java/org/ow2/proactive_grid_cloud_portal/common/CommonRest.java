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
import java.security.Permission;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateRest;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;


public class CommonRest implements CommonRestInterface {

    private static final Logger logger = Logger.getLogger(CommonRest.class);

    public static final String YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST = "You are not connected to the scheduler, you should log on first";

    private final SessionStore sessionStore = SharedSessionStore.getInstance();

    private SchedulerStateRest schedulerRest = null;

    private SchedulerRestInterface scheduler() {
        if (schedulerRest == null) {
            schedulerRest = new SchedulerStateRest();
        }
        return schedulerRest;
    }

    private String getUserName(String sessionId) throws NotConnectedRestException {
        Session ss = sessionStore.get(sessionId);
        return ss.getUserName();
    }

    @Override
    public String login(String username, String password) throws LoginException, SchedulerRestException {
        logger.info("Logging as " + username);
        return scheduler().login(username, password);
    }

    @Override
    public String loginWithCredential(LoginForm multipart) throws KeyException, LoginException, SchedulerRestException {
        logger.info("Logging using credential file");
        return scheduler().loginWithCredential(multipart);
    }

    @Override
    public void logout(String sessionId) throws RestException {
        logger.info("logout");
        scheduler().disconnect(sessionId);
    }

    @Override
    public boolean isConnected(String sessionId) {
        try {
            getUserName(sessionId);
            return true;
        } catch (NotConnectedRestException e) {
            return false;
        }
    }

    @Override
    public String currentUser(String sessionId) {
        try {
            return getUserName(sessionId);
        } catch (NotConnectedRestException e) {
            return null;
        }
    }

    @Override
    public UserData currentUserData(String sessionId) {
        return scheduler().getUserDataFromSessionId(sessionId);
    }

    @Override
    public boolean portalAccess(String sessionId, String portal) throws NotConnectedRestException {
        Scheduler scheduler = checkAccess(sessionId, "EMPTY");

        try {
            return checkPermission(scheduler.getSubject(),
                                   new PortalAccessPermission(portal),
                                   "Acess to portal " + portal + " is disabled");
        } catch (PermissionException e) {
            return false;
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException("you are not connected to the scheduler, you should log on first");
        }
    }

    /**
     * the method check if the session id is valid i.e. a scheduler client is
     * associated to the session id in the session map. If not, a
     * NotConnectedRestException is thrown specifying the invalid access *
     *
     * @return the scheduler linked to the session id, an
     * NotConnectedRestException, if no such mapping exists.
     * @throws NotConnectedRestException
     */
    private Scheduler checkAccess(String sessionId, String path) throws NotConnectedRestException {
        Session session = sessionStore.get(sessionId);

        Scheduler schedulerProxy = session.getScheduler();

        if (schedulerProxy == null) {
            throw new NotConnectedRestException(YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST);
        }

        renewSession(sessionId);

        return schedulerProxy;
    }

    /**
     * Call a method on the scheduler's frontend in order to renew the lease the
     * user has on this frontend. see PORTAL-70
     *
     * @throws NotConnectedRestException
     */
    protected void renewSession(String sessionId) throws NotConnectedRestException {
        try {
            SharedSessionStore.getInstance().renewSession(sessionId);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST, e);
        }
    }

    /**
     * Checks if user has the specified permission.
     *
     * @return true if it has, throw {@link SecurityException} otherwise with specified error message
     */
    private boolean checkPermission(final Subject subject, final Permission permission, String errorMessage)
            throws PermissionException {
        try {
            Subject.doAsPrivileged(subject, (PrivilegedAction<Object>) () -> {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkPermission(permission);
                }
                return null;
            }, null);
        } catch (SecurityException ex) {
            throw new PermissionException(errorMessage);
        }

        return true;
    }

}
