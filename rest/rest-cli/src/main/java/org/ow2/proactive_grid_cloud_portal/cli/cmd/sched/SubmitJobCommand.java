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

import static org.apache.http.entity.ContentType.APPLICATION_XML;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_FILE_EMPTY;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;


public class SubmitJobCommand extends AbstractCommand implements Command {
    private final String pathname;

    private String variables;

    private static final Logger logger = null;

    private final JobKeyValueTransformer jobKeyValueTransformer;

    public SubmitJobCommand(String... params) throws NullPointerException {
        Objects.requireNonNull(params);

        this.pathname = params[0];
        if (params.length > 1) {
            this.variables = params[1];
        }
        this.jobKeyValueTransformer = new JobKeyValueTransformer();

    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        try {
            validateFilePath(currentContext);
            File jobFile = new File(pathname);
            String contentType = URLConnection.getFileNameMap().getContentTypeFor(pathname);
            JobIdData jobId;
            if (APPLICATION_XML.getMimeType().equals(contentType)) {
                jobId = currentContext.getRestClient().submitXml(currentContext.getSessionId(),
                                                                 new FileInputStream(jobFile),
                                                                 map(this.variables));
            } else {
                jobId = currentContext.getRestClient().submitJobArchive(currentContext.getSessionId(),
                                                                        new FileInputStream(jobFile),
                                                                        map(this.variables));
            }
            writeLine(currentContext, "Job('%s') successfully submitted: job('%d')", pathname, jobId.getId());
            resultStack(currentContext).push(jobId);
        } catch (Exception e) {
            handleError(String.format("An error occurred while attempting to submit job('%s'):", pathname),
                        e,
                        currentContext);
        }

    }

    private void validateFilePath(ApplicationContext currentContext) throws JobCreationRestException {

        if (!isFilePathValid(pathname)) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, String.format("'%s' is not a valid file.", pathname));
        }

        if (!isValidFileMimeType(pathname)) {
            throw new JobCreationRestException("Unknown job descriptor type: " + pathname);
        }

        if (isFileExisting(pathname) && isFileEmpty(pathname)) {
            throw new CLIException(REASON_FILE_EMPTY, String.format("'%s' is empty.", pathname));
        }

        if (!isFileExisting(pathname)) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, String.format("'%s' does not exist.", pathname));
        }

    }

    private Map<String, String> map(String variables) {
        return jobKeyValueTransformer.transformVariablesToMap(variables);
    }

    private Boolean isFileEmpty(String pathname) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(pathname));
            if (isFileExisting(pathname)) {
                if (reader.readLine() == null) {
                    return true;
                }
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.log(Level.INFO, "Error reading file " + pathname, e);
            return false;
        }
    }

    private Boolean isFileExisting(String pathname) {
        File file = new File(pathname);
        if (file.exists() && !file.isDirectory())
            return true;
        return false;
    }

    private Boolean isValidFileMimeType(String pathname) {
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(pathname);
        if (contentType != null)
            return (contentType.toLowerCase().equals("application/xml") ||
                    contentType.toLowerCase().equals("application/zip"));
        return false;
    }

    public static boolean isFilePathValid(String path) {

        try {

            Paths.get(path);

        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }

        return true;
    }

}
