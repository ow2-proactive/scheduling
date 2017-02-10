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
package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility.buildOutputStream;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;


public class DownloadFileCommand extends AbstractCommand implements Command {
    private String spaceName;

    private String pathName;

    private String localFile;

    public DownloadFileCommand(String spaceName, String pathname, String localFile) {
        this.spaceName = spaceName;
        this.pathName = pathname;
        this.localFile = localFile;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = currentContext.getRestClient().getScheduler().pullFile(currentContext.getSessionId(),
                                                                        spaceName,
                                                                        pathName);
            out = buildOutputStream(new File(localFile));
            copy(in, out);
            resultStack(currentContext).push(true);
            writeLine(currentContext, "%s successfully downloaded to %s.", pathName, localFile);
        } catch (Exception error) {
            handleError("An error occurred while pulling the file from the server. " + localFile,
                        error,
                        currentContext);
        } finally {
            if (in != null) {
                closeQuietly(in);
            }
            if (out != null) {
                closeQuietly(out);
            }
        }
    }

}
