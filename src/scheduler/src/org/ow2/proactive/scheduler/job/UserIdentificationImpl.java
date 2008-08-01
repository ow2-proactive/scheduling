/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.job;

import java.util.HashSet;

import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent;


/**
 * This class will be able to authenticate a user/admin.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class UserIdentificationImpl extends UserIdentification {

    /** user name */
    private String username;

    /** is this user an admin */
    private Boolean admin = false;

    /** Number of submit for this user */
    private int submitNumber = 0;

    /** Connection time of this user */
    private long connectionTime;

    /** last submit time */
    private long lastSubmitTime = -1;

    /** Host name of this user. */
    private String hostName;

    /** List of events that the user want to receive. */
    private HashSet<SchedulerEvent> userEvents = null;

    /**
     * Constructor of user identification using user name.
     *
     * @param username the user name.
     */
    public UserIdentificationImpl(String username) {
        this.username = username;
        this.connectionTime = System.currentTimeMillis();
    }

    /**
     * Constructor of user identification using user name and admin property.
     *
     * @param username the user name.
     * @param admin true if the user is an administrator, false if not.
     */
    public UserIdentificationImpl(String username, boolean admin) {
        this.username = username;
        this.admin = admin;
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
     * @see org.ow2.proactive.scheduler.common.job.UserIdentification#isAdmin()
     */
    @Override
    public Boolean isAdmin() {
        return admin;
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
     *
     * @param events the userEvents to set.
     */
    public void setUserEvents(SchedulerEvent[] events) {
        userEvents = new HashSet<SchedulerEvent>();
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
            toReturn = toReturn && isAdmin().equals(id.isAdmin());
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
        String a = admin ? "(admin)" : "";
        return username + a;
    }

}
