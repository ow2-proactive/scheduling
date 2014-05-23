/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.job.UserIdentificationImpl;


public class ListeningUser {

    /** Associated listener to client */
    private ClientRequestHandler listener;
    private UserIdentificationImpl user;

    public ListeningUser(UserIdentificationImpl user) {
        this.user = user;
    }

    /**
     * Get the listener associated to this user if any
     *
     * @return the listener if it exists, null otherwise
     */
    public ClientRequestHandler getListener() {
        return listener;
    }

    /**
     * Return true if this user is currently having a listener on Scheduler events, false otherwise.
     *
     * @return true if this user is currently having a listener on Scheduler events, false otherwise.
     */
    public boolean isListening() {
        return listener != null;
    }

    /**
     * Set the listener associated to this user
     *
     * Listener must not be null !
     */
    public void setListener(ClientRequestHandler listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null !");
        }
        this.listener = listener;
    }

    /**
     * clear the listener associated to this user
     */
    public void clearListener() {
        this.listener = null;
    }

    public UserIdentificationImpl getUser() {
        return user;
    }
}
