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

import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableMap;


@XmlRootElement
public class JAASRMRoles {
    private boolean resourceManagerGlobalAdmin = false;

    private boolean nodeSourceAdmin = false;

    private Map<String, Boolean> resourceManagerRoles = ImmutableMap.of("basic",
                                                                        false,
                                                                        "read",
                                                                        false,
                                                                        "write",
                                                                        false,
                                                                        "provider",
                                                                        false,
                                                                        "nsadmin",
                                                                        false,
                                                                        "admin",
                                                                        false);

    private JAASRMAdvancedRoles advanced = new JAASRMAdvancedRoles();

    public JAASRMRoles() {
    }

    public boolean isResourceManagerGlobalAdmin() {
        return resourceManagerGlobalAdmin;
    }

    public void setResourceManagerGlobalAdmin(boolean resourceManagerGlobalAdmin) {
        this.resourceManagerGlobalAdmin = resourceManagerGlobalAdmin;
    }

    public boolean isNodeSourceAdmin() {
        return nodeSourceAdmin;
    }

    public void setNodeSourceAdmin(boolean nodeSourceAdmin) {
        this.nodeSourceAdmin = nodeSourceAdmin;
    }

    public Map<String, Boolean> getResourceManagerRoles() {
        return resourceManagerRoles;
    }

    public void setResourceManagerRoles(Map<String, Boolean> resourceManagerRoles) {
        this.resourceManagerRoles = resourceManagerRoles;
    }

    public JAASRMAdvancedRoles getAdvanced() {
        return advanced;
    }

    public void setAdvanced(JAASRMAdvancedRoles advanced) {
        this.advanced = advanced;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JAASRMRoles that = (JAASRMRoles) o;
        return resourceManagerGlobalAdmin == that.resourceManagerGlobalAdmin &&
               nodeSourceAdmin == that.nodeSourceAdmin && resourceManagerRoles.equals(that.resourceManagerRoles) &&
               advanced.equals(that.advanced);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceManagerGlobalAdmin, nodeSourceAdmin, resourceManagerRoles, advanced);
    }

    @Override
    public String toString() {
        return "JAASRMRoles{" + "resourceManagerGlobalAdmin=" + resourceManagerGlobalAdmin + ", nodeSourceAdmin=" +
               nodeSourceAdmin + ", resourceManagerRoles=" + resourceManagerRoles + ", advanced=" + advanced + '}';
    }
}
