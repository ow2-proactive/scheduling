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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class InternalUserInfo {

    private String login;

    private String password;

    private String tenant;

    private Set<String> groups;

    private boolean changed = false;

    public InternalUserInfo(String login, String password, String tenant, Collection<String> groups, boolean newUser) {
        this.login = login;
        this.password = password;
        this.tenant = tenant;
        this.groups = new HashSet<>(groups);
        this.changed = newUser;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.changed = this.changed || !this.login.equals(login);
        this.login = login;

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.changed = true;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Collection<String> groups) {
        // null groups means no change
        if (groups != null) {
            Set<String> tmpGroups = new HashSet<>(groups);
            this.changed = this.changed || !this.groups.equals(tmpGroups);
            this.groups = tmpGroups;
        }
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return "InternalUserInfo{" + "login='" + login + '\'' + ", tenant='" + tenant + '\'' + ", groups=" + groups +
               ", changed=" + changed + '}';
    }
}
