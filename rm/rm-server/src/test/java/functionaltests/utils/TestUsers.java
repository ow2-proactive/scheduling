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
package functionaltests.utils;

public enum TestUsers {

    DEMO("demo", "demo"),
    USER("user", "pwd"),
    TEST("test_executor", "pwd"),
    RADMIN("radmin", "pwd"),
    SUBADMIN("subadmin", "pwd"),
    NSADMIN("nsadmin", "pwd"),
    NSADMIN2("nsadmin2", "pwd"),
    ADMIN("admin", "admin"),
    PROVIDER("provider", "pwd");

    public final String username;

    public final String password;

    TestUsers(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
