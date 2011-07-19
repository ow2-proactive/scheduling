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
package org.ow2.proactive.scheduler.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.UserIdentification;


/**
 * SchedulerUsers is the list of user connected to the scheduler with the GUI.<br>
 * This can provide informations about who is connected, what are their status, etc...
 *
 * @author The ProActive Team
 * 
 * $Id$
 */
@PublicAPI
public class SchedulerUsers implements Serializable {

    /**  */
    private static final long serialVersionUID = 31L;
    /** 
     * List of connected user.
     * Will be sorted by default as defined in the {@link UserIdentification#compareTo(UserIdentification)} method !
     */
    private Set<UserIdentification> users = new HashSet<UserIdentification>();

    /**
     * Return a sorted collection of all connected users.
     *
     * @return a sorted collection of all connected users
     */
    public Collection<UserIdentification> getUsers() {
        List<UserIdentification> tmp = new ArrayList<UserIdentification>();
        Iterator<UserIdentification> iter = users.iterator();
        while (iter.hasNext()) {
            tmp.add(iter.next());
        }
        Collections.sort(tmp);
        return tmp;
    }

    /**
     * Update the list of users with this given user.
     * As this method could both add or remove user, it doesn't use the default SET.remove() or SET.add() methods.
     * This method will first remove the given user (if it is new, it won't be deleted).
     * Then, if it is not marked as 'toRemoved', the user is added to the list.
     * If it's just an update, for example 'increase number of submit' it will be updated at client side as it is removed
     * and added.
     *
     * @param user the user to update.
     */
    public void update(UserIdentification user) {
        //remove all userIdentification that are equals to the given user
        //if the user is a new one, nothing is removed
        Iterator<UserIdentification> iter = users.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals(user)) {
                iter.remove();
            }
        }
        //add or re-inject the user if it was not to be removed
        if (!user.isToRemove()) {
            users.add(user);
        }
    }
}
