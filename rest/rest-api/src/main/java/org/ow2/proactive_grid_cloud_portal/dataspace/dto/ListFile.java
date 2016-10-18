/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.dataspace.dto;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;


@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class ListFile {
    private List<String> directoryListing;
    private List<String> fileListing;
    private List<String> fullListing;

    public List<String> getDirectoryListing() {
        return directoryListing;
    }

    public void setDirectoryListing(List<String> directoryListing) {
        this.directoryListing = directoryListing;
    }

    public List<String> getFileListing() {
        return fileListing;
    }

    public void setFileListing(List<String> fileListing) {
        this.fileListing = fileListing;
    }

    public void setFullListing(List<String> fullListing) {
        this.fullListing = fullListing;
    }

    public List<String> getFullListing() {
        return fullListing;
    }
}
