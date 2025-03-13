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
package org.ow2.proactive_grid_cloud_portal.common.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class JAASGroupAdvancedRoles {

    boolean allPermissions = false;

    List<String> deniedMethodCalls = new ArrayList<String>();

    public JAASGroupAdvancedRoles() {
    }

    public List<String> getDeniedMethodCalls() {
        return deniedMethodCalls;
    }

    public void setDeniedMethodCalls(List<String> deniedMethodCalls) {
        this.deniedMethodCalls = deniedMethodCalls;
    }

    public boolean isAllPermissions() {
        return allPermissions;
    }

    public void setAllPermissions(boolean allPermissions) {
        this.allPermissions = allPermissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JAASGroupAdvancedRoles that = (JAASGroupAdvancedRoles) o;
        return allPermissions == that.allPermissions && deniedMethodCalls.equals(that.deniedMethodCalls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allPermissions, deniedMethodCalls);
    }

    @Override
    public String toString() {
        return "JAASGroupAdvancedRoles{" + "allPermissions=" + allPermissions + ", deniedMethodCalls=" +
               deniedMethodCalls + '}';
    }
}
