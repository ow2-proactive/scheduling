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
public class JAASSchedulerRoles {

    private Boolean handleOnlyMyJobs = null;

    private Map<String, Boolean> changeJobPriority = ImmutableMap.of("0",
                                                                     false,
                                                                     "1",
                                                                     false,
                                                                     "2",
                                                                     false,
                                                                     "3",
                                                                     false,
                                                                     "4",
                                                                     false,
                                                                     "5",
                                                                     false);

    private Map<String, Boolean> schedulerRoles = ImmutableMap.of("basic",
                                                                  false,
                                                                  "read",
                                                                  false,
                                                                  "write",
                                                                  false,
                                                                  "admin",
                                                                  false);

    private JAASSchedulerAdvancedRoles advanced = new JAASSchedulerAdvancedRoles();

    public JAASSchedulerRoles() {
    }

    public Boolean getHandleOnlyMyJobs() {
        return handleOnlyMyJobs;
    }

    public void setHandleOnlyMyJobs(Boolean handleOnlyMyJobs) {
        this.handleOnlyMyJobs = handleOnlyMyJobs;
    }

    public JAASSchedulerAdvancedRoles getAdvanced() {
        return advanced;
    }

    public void setAdvanced(JAASSchedulerAdvancedRoles advanced) {
        this.advanced = advanced;
    }

    public Map<String, Boolean> getChangeJobPriority() {
        return changeJobPriority;
    }

    public void setChangeJobPriority(Map<String, Boolean> changeJobPriority) {
        this.changeJobPriority = changeJobPriority;
    }

    public Map<String, Boolean> getSchedulerRoles() {
        return schedulerRoles;
    }

    public void setSchedulerRoles(Map<String, Boolean> schedulerRoles) {
        this.schedulerRoles = schedulerRoles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JAASSchedulerRoles that = (JAASSchedulerRoles) o;
        return Objects.equals(handleOnlyMyJobs, that.handleOnlyMyJobs) &&
               changeJobPriority.equals(that.changeJobPriority) && schedulerRoles.equals(that.schedulerRoles) &&
               advanced.equals(that.advanced);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handleOnlyMyJobs, changeJobPriority, schedulerRoles, advanced);
    }

    @Override
    public String toString() {
        return "JAASSchedulerRoles{" + "handleOnlyMyJobs=" + handleOnlyMyJobs + ", changeJobPriority=" +
               changeJobPriority + ", schedulerRoles=" + schedulerRoles + ", advanced=" + advanced + '}';
    }
}
