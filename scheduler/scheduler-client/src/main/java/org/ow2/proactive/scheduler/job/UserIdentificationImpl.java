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
package org.ow2.proactive.scheduler.job;

import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.TimerTask;

import javax.security.auth.Subject;

import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.UserIdentification;


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
    private int submitNumber = 0;

    /** Connection time of this user */
    private long connectionTime;

    /** last submit time */
    private long lastSubmitTime = -1;

    /** Host name of this user. */
    private String hostName;

    /** Tell if this user want to receive all events or only his events */
    private boolean myEventsOnly = false;

    /** List of events that the user want to receive. */
    private transient HashSet<SchedulerEvent> userEvents = null;

    /** Associated timerTask for user session management */
    //must be transient because useless on user side and TimerTask is not serializable
    private transient TimerTask session;

    /**
     * Constructor of user identification using user name.
     *
     * @param username the user name.
     */
    public UserIdentificationImpl(String username) {
        this.username = username;
        this.connectionTime = System.currentTimeMillis();
        this.subject = new Subject();
        this.subject.getPrincipals().add(new UserNamePrincipal(username));
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
     * Set the session value to the given session value
     * 
     * @param session the session to set
     */
    public void setSession(TimerTask session) {
        this.session = session;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     *
     * @return true if the two users seems exactly equals.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof UserIdentificationImpl) {
            boolean toReturn = true;
            UserIdentificationImpl id = (UserIdentificationImpl) object;
            toReturn = toReturn && getUsername().equals(id.getUsername());
            toReturn = toReturn && getHostName().equals(id.getHostName());
            toReturn = toReturn && (getConnectionTime() == id.getConnectionTime());
            return toReturn;
        }
        return false;
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
    public boolean checkPermission(final Permission permission, String errorMessage)
            throws PermissionException {
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
