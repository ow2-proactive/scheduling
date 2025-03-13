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

import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class JAASRMAdvancedRoles {
    boolean nodeFullAccess = false;

    boolean rmMyAccountReader = false;

    boolean rmAllAccountsReader = false;

    public JAASRMAdvancedRoles() {
    }

    public boolean isNodeFullAccess() {
        return nodeFullAccess;
    }

    public void setNodeFullAccess(boolean nodeFullAccess) {
        this.nodeFullAccess = nodeFullAccess;
    }

    public boolean isRmMyAccountReader() {
        return rmMyAccountReader;
    }

    public void setRmMyAccountReader(boolean rmMyAccountReader) {
        this.rmMyAccountReader = rmMyAccountReader;
    }

    public boolean isRmAllAccountsReader() {
        return rmAllAccountsReader;
    }

    public void setRmAllAccountsReader(boolean rmAllAccountsReader) {
        this.rmAllAccountsReader = rmAllAccountsReader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JAASRMAdvancedRoles that = (JAASRMAdvancedRoles) o;
        return nodeFullAccess == that.nodeFullAccess && rmMyAccountReader == that.rmMyAccountReader &&
               rmAllAccountsReader == that.rmAllAccountsReader;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeFullAccess, rmMyAccountReader, rmAllAccountsReader);
    }

    @Override
    public String toString() {
        return "JAASRMAdvancedRoles{" + "nodeFullAccess=" + nodeFullAccess + ", rmMyAccountReader=" +
               rmMyAccountReader + ", rmAllAccountsReader=" + rmAllAccountsReader + '}';
    }
}
