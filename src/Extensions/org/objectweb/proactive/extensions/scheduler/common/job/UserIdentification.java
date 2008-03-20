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
 * It provides some information like username, admin status, etc...
 *
 * @author jlscheef - ProActiveTeam
 * @date 20 mars 08
 * @version 3.9
 *
 */
public interface UserIdentification extends Serializable, Comparable<UserIdentification> {
    /**
     * To get the username
     *
     * @return the username
     */
    public String getUsername();

    /**
     * To know if this user is an administrator or a user.
     *
     * @return true if this user is admin, false if not.
     */
    public boolean isAdmin();

    /**
     * Get the number of submit for this user.
     * 
     * @return the number of submit for this user.
     */
    public int getSubmitNumber();

}
