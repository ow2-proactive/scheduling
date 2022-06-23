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
package org.ow2.proactive_grid_cloud_portal.dataspace.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ListFileMetadata extends ListFile {
    private Map<String, String> types = new HashMap<>();

    private Map<String, String> permissions = new HashMap<>();

    private Map<String, Date> lastModifiedDates = new HashMap<>();

    private Map<String, Long> sizes = new HashMap<>();

    public ListFileMetadata() {}

    public ListFileMetadata(ListFile listFile) {
        super(listFile);
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public Map<String, Date> getLastModifiedDates() {
        return lastModifiedDates;
    }

    public Map<String, Long> getSizes() {
        return sizes;
    }

    public void addType(String filename, String type) {
        types.put(filename, type);
    }

    public void addPermission(String filename, String permission) {
        permissions.put(filename, permission);
    }

    public void addLastModifiedDate(String filename, Date date) {
        lastModifiedDates.put(filename, date);
    }

    public void addSize(String filename, long size) {
        sizes.put(filename, size);
    }

    @Override
    public String toString() {
        return "ListFileMetadata{" +
                "types=" + types +
                ", permissions=" + permissions +
                ", lastModifiedDates=" + lastModifiedDates +
                ", sizes=" + sizes +
                "} " + super.toString();
    }
}
