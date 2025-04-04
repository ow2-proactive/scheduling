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
package org.ow2.proactive.permissions;

import java.util.HashMap;
import java.util.Objects;


public class DeniedMethodCallPermissionRepository {

    private static DeniedMethodCallPermissionRepository instance;

    private HashMap<UserMethodKey, Boolean> deniedMethodCallMemory = new HashMap<>();

    public DeniedMethodCallPermissionRepository() {

    }

    public synchronized static DeniedMethodCallPermissionRepository getInstance() {
        if (instance == null) {
            instance = new DeniedMethodCallPermissionRepository();
        }
        return instance;
    }

    public synchronized boolean checkAndSetDeniedMethodCall(String userName, String method, boolean denied) {
        UserMethodKey key = new UserMethodKey(userName, method);
        if (deniedMethodCallMemory.containsKey(key)) {
            return deniedMethodCallMemory.get(key);
        } else {
            deniedMethodCallMemory.put(key, denied);
            return denied;
        }

    }

    static class UserMethodKey {

        private String userName;

        private String method;

        public UserMethodKey(String userName, String method) {
            this.userName = userName;
            this.method = method;
        }

        public String getUserName() {
            return userName;
        }

        public String getMethod() {
            return method;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            UserMethodKey that = (UserMethodKey) o;
            return userName.equals(that.userName) && method.equals(that.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userName, method);
        }
    }
}
