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
package org.ow2.proactive.scheduler.rest.ds;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.Dataspace;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.IRemoteSource;
import org.ow2.proactive_grid_cloud_portal.common.FileType;

import com.google.common.collect.Lists;


public class RemoteSource implements IRemoteSource {

    private Dataspace dataspace;

    private String path;

    private List<String> includes;

    private List<String> excludes;

    private FileType pathType = FileType.UNKNOWN;

    public RemoteSource() {
    }

    public RemoteSource(Dataspace dataspace) {
        this.dataspace = dataspace;
    }

    public RemoteSource(Dataspace dataspace, String pathname) {
        this.dataspace = dataspace;
        this.path = pathname;
    }

    @Override
    public Dataspace getDataspace() {
        return dataspace;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setIncludes(List<String> includes) {
        checkNotNull(includes);
        this.includes = Lists.newArrayList(includes);
    }

    public void setIncludes(String... includes) {
        checkNotNull(includes);
        this.includes = Lists.newArrayList(includes);
    }

    @Override
    public List<String> getIncludes() {
        return includes;
    }

    public void setExcludes(List<String> excludes) {
        checkNotNull(excludes);
        this.excludes = Lists.newArrayList(excludes);
    }

    public void setExcludes(String... excludes) {
        checkNotNull(excludes);
        this.excludes = Lists.newArrayList(excludes);
    }

    @Override
    public List<String> getExcludes() {
        return excludes;
    }

    public FileType getType() {
        return pathType;
    }

    public void setType(FileType pathType) {
        this.pathType = pathType;
    }

    @Override
    public String toString() {
        return "RemoteSource{" + "dataspace=" + dataspace + ", path='" + path + '\'' + ", includes=" + includes +
               ", excludes=" + excludes + ", pathType=" + pathType + '}';
    }

}
