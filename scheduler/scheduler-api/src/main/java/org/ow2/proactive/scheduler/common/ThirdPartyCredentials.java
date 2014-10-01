/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common;

import java.security.KeyException;
import java.util.Set;

import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;


/**
 * Third-party credentials are key-value pairs associated to a particular user and saved in the database.
 * They can be used for instance to authenticate to third-party services in tasks.
 */
public interface ThirdPartyCredentials {

    /**
     * Stores a third-party credential key-value pair in the database.
     *
     * @param key the third-party credential key to store
     * @param value the third-party credential value to store, it will be encrypted
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you can't access this particular method.
     * @throws KeyException if encryption of value fails
     */
    void putThirdPartyCredential(String key, String value) throws NotConnectedException, PermissionException,
            KeyException;

    /**
     * @return all third-party credential keys stored for the current user
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you can't access this particular method.
     */
    Set<String> thirdPartyCredentialsKeySet() throws NotConnectedException, PermissionException;

    /**
     *
     * @param key the third-party credential key to remove
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you can't access this particular method.
     */
    void removeThirdPartyCredential(String key) throws NotConnectedException, PermissionException;
}
