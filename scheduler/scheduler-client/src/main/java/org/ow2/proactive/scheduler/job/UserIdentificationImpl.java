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
package org.ow2.proactive.scheduler.job;

import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.*;

import javax.security.auth.Subject;

import org.ow2.proactive.authentication.principals.DomainNamePrincipal;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.authentication.principals.TenantPrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.permissions.NotificationAdminPermission;
import org.ow2.proactive.permissions.PcaAdminPermission;
import org.ow2.proactive.permissions.RMCoreAllPermission;
import org.ow2.proactive.permissions.ServiceRolePermission;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.permissions.*;


/**
 * This class will be able to authenticate a client.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class UserIdentificationImpl extends UserIdentification {

    /** user name */
    private String username;

    /** user subject */
    private Subject subject;

    /** Number of submit for this user */
    private volatile int submitNumber = 0;

    /** Connection time of this user */
    private long connectionTime;

    /** last submit time */
    private volatile long lastSubmitTime = -1;

    /** Host name of this user. */
    private volatile String hostName;

    /** Tell if this user want to receive all events or only his events */
    private volatile boolean myEventsOnly = false;

    /** List of events that the user want to receive. */
    private transient volatile HashSet<SchedulerEvent> userEvents = null;

    /** Associated timerTask for user session management */
    //must be transient because useless on user side and TimerTask is not serializable
    private transient volatile TimerTask session;

    /**
     * Constructor of user identification using user name.
     *
     * @param username the user name.
     * @param tenant user tenant
     */
    public UserIdentificationImpl(String username, String tenant, String domain) {
        this.username = username;
        this.connectionTime = System.currentTimeMillis();
        this.subject = new Subject();
        this.subject.getPrincipals().add(new UserNamePrincipal(username));
        if (tenant != null) {
            this.subject.getPrincipals().add(new TenantPrincipal(tenant));
        }
        if (domain != null) {
            this.subject.getPrincipals().add(new DomainNamePrincipal(domain));
        }
    }

    /**
     * Constructor of user identification using user name and admin property.
     *
     * @param username the user name.
     * @param subject contains all user's principals and permissions
     */
    public UserIdentificationImpl(String username, Subject subject) {
        this.username = username;
        this.subject = subject;
        this.connectionTime = System.currentTimeMillis();
    }

    /**
     * Set this user to be removed by update method.
     */
    public void setToRemove() {
        this.toRemove = true;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.UserIdentification#getUsername()
     */
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Set<String> getGroups() {
        Set<String> answer = new HashSet<>();
        Set<GroupNamePrincipal> groupPrincipals = subject.getPrincipals(GroupNamePrincipal.class);
        for (GroupNamePrincipal principal : groupPrincipals) {
            answer.add(principal.getName());
        }
        return answer;
    }

    @Override
    public String getTenant() {
        Set<TenantPrincipal> tenants = subject.getPrincipals(TenantPrincipal.class);
        if (tenants == null || tenants.size() == 0) {
            return null;
        }
        return tenants.iterator().next().getName();
    }

    @Override
    public String getDomain() {
        Set<DomainNamePrincipal> domains = subject.getPrincipals(DomainNamePrincipal.class);
        if (domains == null || domains.size() == 0) {
            return null;
        }
        return domains.iterator().next().getName();
    }

    @Override
    public boolean isAllTenantPermission() {
        try {
            return checkPermission(new TenantAllAccessPermission(), "N/A");
        } catch (PermissionException e) {
            return false;
        }
    }

    public Set<JobPriority> getPriorityPermission() {
        Set<JobPriority> availablePriorities = new LinkedHashSet<>();
        for (int i = 0; i <= JobPriority.HIGHEST.getPriority(); i++) {
            try {
                checkPermission(new ChangePriorityPermission(i), "N/A");
                availablePriorities.add(JobPriority.findPriority(i));
            } catch (PermissionException e) {
            }
        }
        return availablePriorities;
    }

    @Override
    public boolean isAllJobPlannerPermission() {
        try {
            return checkPermission(new JobPlannerAllAccessPermission(), "N/A");
        } catch (PermissionException e) {
            return false;
        }
    }

    @Override
    public boolean isCanCreateAssociationPermission() {
        try {
            return checkPermission(new JPCanCreateAssociationPermission(), "N/A");
        } catch (PermissionException e) {
            return false;
        }
    }

    @Override
    public boolean isPcaAdminPermission() {
        try {
            return checkPermission(new PcaAdminPermission(), "N/A");
        } catch (PermissionException e) {
            return false;
        }
    }

    @Override
    public boolean isNotificationAdminPermission() {
        try {
            return checkPermission(new NotificationAdminPermission(), "N/A");
        } catch (PermissionException e) {
            return false;
        }
    }

    @Override
    public boolean isRMCoreAllPermission() {
        try {
            return checkPermission(new RMCoreAllPermission(), "N/A");
        } catch (PermissionException e) {
            return false;
        }
    }

    @Override
    public boolean isSchedulerAdminPermission() {
        try {
            return checkPermission(new ServiceRolePermission("org.ow2.proactive.scheduler.core.SchedulerFrontend.*"),
                                   "N/A");
        } catch (PermissionException e) {
            return false;
        }
    }

    @Override
    public boolean isHandleOnlyMyJobsPermission() {
        try {
            checkPermission(new HandleOnlyMyJobsPermission(false), "N/A");
            return false;
        } catch (PermissionException e) {
            return true;
        }
    }

    @Override
    public boolean isOtherUsersJobReadPermission() {
        try {
            return checkPermission(new OtherUsersJobReadPermission(), "N/A");
        } catch (PermissionException e) {
            return false;
        }
    }

    /**
     * Add one to the submit number
     */
    public void addSubmit() {
        submitNumber++;
        lastSubmitTime = System.currentTimeMillis();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.UserIdentification#getSubmitNumber()
     */
    @Override
    public int getSubmitNumber() {
        return submitNumber;
    }

    /**
     * Returns the userEvents.
     *
     * @return the userEvents.
     */
    public HashSet<SchedulerEvent> getUserEvents() {
        return userEvents;
    }

    /**
     * Sets the hostName to the given hostName value.
     *
     * @param hostName the hostName to set.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Sets the userEvents to the given userEvents value.
     * If the given value is null, user will listen to every events.
     *
     * @param events the userEvents to set.
     */
    public void setUserEvents(SchedulerEvent[] events) {
        if (events == null || events.length == 0) {
            userEvents = null;
            return;
        }
        userEvents = new HashSet<>();
        //protect from duplicated events
        for (SchedulerEvent e : events) {
            userEvents.add(e);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.UserIdentification#getConnectionTime()
     */
    @Override
    public long getConnectionTime() {
        return connectionTime;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.UserIdentification#getHostName()
     */
    @Override
    public String getHostName() {
        return hostName;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.UserIdentification#getLastSubmitTime()
     */
    @Override
    public long getLastSubmitTime() {
        return lastSubmitTime;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.UserIdentification#isMyEventsOnly()
     */
    @Override
    public boolean isMyEventsOnly() {
        return myEventsOnly;
    }

    /**
     * Set the myEventsOnly value to the given myEventsOnly value.
     * 
     * @param myEventsOnly the myEventsOnly to set.
     */
    public void setMyEventsOnly(boolean myEventsOnly) {
        this.myEventsOnly = myEventsOnly;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.UserIdentification#getSession()
     */
    @Override
    public TimerTask getSession() {
        return session;
    }

    /**
     * Cancel the current session of the user
     */
    public synchronized void cancelSession() {
        if (this.session != null) {
            this.session.cancel();
            this.session = null;
        }
    }

    /**
     * Set the session value to the given session value
     * If the user has already a session active, it will be cancelled and replaced by the new one.
     * 
     * @param session the session to set
     */
    public synchronized void setSession(TimerTask session) {
        cancelSession();
        this.session = session;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (connectionTime ^ (connectionTime >>> 32));
        result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     *
     * @return true if the two users seems exactly equals.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof UserIdentificationImpl))
            return false;
        UserIdentificationImpl other = (UserIdentificationImpl) obj;
        if (connectionTime != other.connectionTime)
            return false;
        if (hostName == null) {
            if (other.hostName != null)
                return false;
        } else if (!hostName.equals(other.hostName))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName() + "[" + username + "]";
    }

    /**
     * Checks if user has the specified permission.
     *
     * @return true if it has, throw {@link SecurityException} otherwise with specified error message
     */
    public boolean checkPermission(final Permission permission, String errorMessage) throws PermissionException {
        try {
            Subject.doAsPrivileged(subject, new PrivilegedAction<Object>() {
                public Object run() {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(permission);
                    }
                    return null;
                }
            }, null);
        } catch (SecurityException ex) {
            throw new PermissionException(errorMessage);
        }

        return true;
    }

    /**
     * Gets user's subject retrieved from JAAS authentication
     * @return user's subject
     */
    public Subject getSubject() {
        return subject;
    }
}
