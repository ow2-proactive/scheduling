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

import java.security.BasicPermission;

import org.ow2.proactive.policy.ClientsPolicy;


/**
 * Base class of client specific permissions.
 * Allows to user wildcard in names and actions.
 * @see ClientsPolicy
 */
public class ClientPermission extends BasicPermission {

    // This serial version uid is meant to prevent issues when restoring Resource Manager database from a previous version.
    // any addition to this class (new method, field, etc) should imply to change this uid.
    private static final long serialVersionUID = 1L;

    public ClientPermission() {
        super("*");
    }

    public ClientPermission(String name) {
        super(name);
    }

    public ClientPermission(String name, String actions) {
        super(name, actions);
    }
}
