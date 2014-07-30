/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
package org.ow2.proactive.scheduler.rest.ds;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.ILocalSource;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.utils.Zipper;

import com.google.common.collect.Lists;

public class LocalDirSource implements ILocalSource {
    private File source;
    private List<String> includes;
    private List<String> excludes;

    public LocalDirSource(File dir) {
        checkArgument(dir.isDirectory());
        source = dir;
    }

    public LocalDirSource(String path) {
        this(new File(path));
    }

    public void setIncludes(List<String> includes) {
        checkArgument(includes != null && !includes.isEmpty());
        this.includes = includes;
    }

    public void setIncludes(String... include) {
        checkArgument(include != null && include.length > 0);
        this.includes = Lists.newArrayList(include);
    }

    public void setExcludes(List<String> excludes) {
        checkArgument(excludes != null && !excludes.isEmpty());
        this.excludes = excludes;
    }

    public void setExcludes(String... exclude) {
        checkArgument(exclude != null && exclude.length > 0);
        this.excludes = Lists.newArrayList(exclude);
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        Zipper.ZIP.zip(source, includes, excludes, outputStream);
    }

    @Override
    public String getEncoding() throws IOException {
        return "zip";
    }
}
