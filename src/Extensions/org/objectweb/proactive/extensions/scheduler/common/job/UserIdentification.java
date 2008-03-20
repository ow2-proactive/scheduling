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
package org.objectweb.proactive.extensions.scheduler.common.job;

import java.io.Serializable;


/**
 * SchedulerUser is an internal representation of a user.<br>
 * It provides some information like user name, admin status, etc...
 *
 * @author jlscheef - ProActiveTeam
 * @date 20 mars 08
 * @version 3.9
 *
 */
public abstract class UserIdentification implements Serializable, Comparable<UserIdentification> {

    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_ADMIN = 2;
    public static final int SORT_BY_SUBMIT = 3;
    public static final int SORT_BY_HOST = 4;
    public static final int SORT_BY_CONNECTION = 5;
    public static final int SORT_BY_LASTSUBMIT = 6;
    public static final int ASC_ORDER = 1;
    public static final int DESC_ORDER = 2;
    private static int currentSort = SORT_BY_NAME;
    private static int currentOrder = ASC_ORDER;
    protected boolean toRemove = false;

    /**
     * To get the user name
     *
     * @return the user name
     */
    public abstract String getUsername();

    /**
     * To know if this user is an administrator or a user.
     *
     * @return true if this user is admin, false if not.
     */
    public abstract Boolean isAdmin();

    /**
     * Get the number of submit for this user.
     * 
     * @return the number of submit for this user.
     */
    public abstract int getSubmitNumber();

    /**
     * Get the host name of this user.
     * 
     * @return the host name of this user.
     */
    public abstract String getHostName();

    /**
     * Get the time of the connection of this user.
     * 
     * @return the time of the connection of this user.
     */
    public abstract long getConnectionTime();

    /**
     * Get the last time this user has submit a job.
     * 
     * @return the last time this user has submit a job.
     */
    public abstract long getLastSubmitTime();

    /**
     * Set the field to sort on.
     *
     * @param sortBy
     *            the field on which the sort will be made.
     */
    public static void setSortingBy(int sortBy) {
        currentSort = sortBy;
    }

    /**
     * Set the order for the next sort.
     *
     * @param order the new order to set.
     */
    public static void setSortingOrder(int order) {
        if ((order == ASC_ORDER) || (order == DESC_ORDER)) {
            currentOrder = order;
        } else {
            currentOrder = ASC_ORDER;
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(UserIdentification user) {
        switch (currentSort) {
            case SORT_BY_ADMIN:
                return (currentOrder == ASC_ORDER) ? isAdmin().compareTo(user.isAdmin()) : user.isAdmin()
                        .compareTo(isAdmin());
            case SORT_BY_SUBMIT:
                return (currentOrder == ASC_ORDER) ? getSubmitNumber() - user.getSubmitNumber() : user
                        .getSubmitNumber() -
                    getSubmitNumber();
            case SORT_BY_HOST:
                return (currentOrder == ASC_ORDER) ? getHostName().compareTo(user.getHostName()) : user
                        .getHostName().compareTo(getHostName());
            case SORT_BY_CONNECTION:
                return (currentOrder == ASC_ORDER) ? (int) (getConnectionTime() - user.getConnectionTime())
                        : (int) (user.getConnectionTime() - getConnectionTime());
            case SORT_BY_LASTSUBMIT:
                return (currentOrder == ASC_ORDER) ? (int) (getLastSubmitTime() - user.getLastSubmitTime())
                        : (int) (user.getLastSubmitTime() - getLastSubmitTime());
            default:
                return (currentOrder == ASC_ORDER) ? getHostName().compareTo(user.getHostName()) : user
                        .getHostName().compareTo(getHostName());
        }
    }

    /**
     * Returns true if this user has to be removed, false if not.
     * 
     * @return true if this user has to be removed, false if not.
     */
    public boolean isToRemove() {
        return toRemove;
    }

}
