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
package org.ow2.proactive.scheduler.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.UserIdentification;


/**
 * SchedulerUsers is the list of user connected to the scheduler with the GUI.<br>
 * This can provide informations about who is connected, what are their status, etc...
 *
 * @author The ProActive Team
 * @date 19 mars 08
 *
 */
@PublicAPI
public class SchedulerUsers implements Serializable {

    /** List of connected user. */
    private HashSet<UserIdentification> users = new HashSet<UserIdentification>();

    /**
     * Add a new user to the list of connected user.
     *
     * @param user the new user to add.
     */
    public void addUser(UserIdentification user) {
        users.add(user);
    }

    /**
     * Return a sorted collection of all connected users.
     *
     * @return a sorted collection of all connected users
     */
    public Collection<UserIdentification> getUsers() {
        ArrayList<UserIdentification> c = new ArrayList<UserIdentification>();
        Iterator<UserIdentification> iter = users.iterator();
        while (iter.hasNext()) {
            c.add(iter.next());
        }
        Collections.sort(c);
        return c;
    }

    /**
     * Update the list of users with this given user.
     *
     * @param user the user to update.
     */
    public void update(UserIdentification user) {
        Iterator<UserIdentification> iter = users.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals(user)) {
                iter.remove();
            }
        }
        if (!user.isToRemove()) {
            users.add(user);
        }
    }
}
