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
package org.ow2.proactive.authentication;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;


public interface UsersService {

    /**
     * Refreshes the internal state of the service
     * Must be called before interacting with the service
     * @throws LoginException
     */
    void refresh() throws LoginException;

    /**
     * check if users state have changed
     * @return true if a user has been modified
     */
    boolean usersChanged();

    /**
     * Commit pending changes to the file system
     * @throws LoginException
     */
    void commit() throws LoginException;

    /**
     * Returns the list of users
     * @return list of users info
     * @throws LoginException
     */
    List<OutputUserInfo> listUsers() throws LoginException;

    /**
     * Add a new user
     * @param userInfo user information input
     * @return list of users info after update
     * @throws LoginException
     */
    List<OutputUserInfo> addUser(InputUserInfo userInfo) throws LoginException;

    /**
     * Checks if a user exists
     * @param userName user login name
     * @return true if the user exists
     */
    boolean userExists(String userName);

    /**
     * Returns a user info
     * @param userName user login name
     * @return user info
     * @throws LoginException
     */
    OutputUserInfo getUser(String userName) throws LoginException;

    /**
     * Update an existing user
     * @param userInfo user login name
     * @return list of users info after update
     * @throws LoginException
     */
    List<OutputUserInfo> updateUser(InputUserInfo userInfo) throws LoginException;

    /**
     * Delete an existing user
     * @param userName user login name
     * @return list of users info after update
     * @throws LoginException
     */
    List<OutputUserInfo> deleteUser(String userName) throws LoginException;

    /**
     * List the groups to tenant associations
     * @return map of group to tenant associations
     */
    Map<String, String> listGroupsToTenant();

    /**
     * Add a group to tenant association
     * If the group had already a tenant associated, it will be overridden by the new tenant
     * @param group group name
     * @param tenant tenant name
     * @return map of group to tenant associations after update
     */
    Map<String, String> addGroupTenantAssociation(String group, String tenant);

    /**
     * Remove group to tenant association
     * @param group group name
     * @return map of group to tenant associations after update
     */
    Map<String, String> removeGroupTenantAssociation(String group);

    /**
     * Check the user's password
     * @param userName user login name
     * @param password clear text password to check
     * @return true if the password is correct, false otherwise
     * @throws LoginException
     */
    boolean checkPassword(String userName, String password) throws LoginException;

    /**
     * Return the set of groups associated with a user
     * @param userName user login name
     * @return set of groups
     * @throws LoginException
     */
    Set<String> getGroups(String userName) throws LoginException;

    /**
     * Return the tenant associated with a user
     * @param userName user login name
     * @return a tenant name or null if no tenant is associated
     * @throws LoginException
     */
    String getTenant(String userName) throws LoginException;
}
