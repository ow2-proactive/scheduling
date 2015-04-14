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

import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.ILocalDestination;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.utils.Zipper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class LocalDestination implements ILocalDestination {

    private File dest;

    public LocalDestination(File dest) {
        this.dest = dest;
    }

    @Override
    public void readFrom(InputStream is, String encoding) throws IOException {
        if (Strings.isNullOrEmpty(encoding)) {
            Files.asByteSink(dest).writeFrom(is);
        } else if ("gzip".equals(encoding)) {
            Zipper.GZIP.unzip(is, dest);
        } else if ("zip".equals(encoding)) {
            Zipper.ZIP.unzip(is, dest);
        } else {
            throw new RuntimeException("Unkonwn content encoding type: " + encoding);
        }
    }

    @Override
    public String toString() {
        return "LocalDestination{" +
                "dest=" + dest +
                '}';
    }

}
