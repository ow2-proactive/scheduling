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

import com.google.common.io.Files;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.ILocalSource;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.utils.Zipper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkArgument;


public class LocalFileSource implements ILocalSource {

    private File file;

    public LocalFileSource(File file) {
        checkArgument(file.isFile());
        this.file = file;
    }

    public LocalFileSource(String path) {
        checkArgument(new File(path).isFile());
        this.file = new File(path);

    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        if (Zipper.isZipFile(file)) {
            Files.asByteSource(file).copyTo(outputStream);
        } else {
            Zipper.GZIP.zip(file, outputStream);
        }
    }

    @Override
    public String getEncoding() throws IOException {
        return Zipper.isZipFile(file) ? null : "gzip";
    }

    @Override
    public String toString() {
        return "LocalFileSource{" +
                "file=" + file +
                '}';
    }

}
