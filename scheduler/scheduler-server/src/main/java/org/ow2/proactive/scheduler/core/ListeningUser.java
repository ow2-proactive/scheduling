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
