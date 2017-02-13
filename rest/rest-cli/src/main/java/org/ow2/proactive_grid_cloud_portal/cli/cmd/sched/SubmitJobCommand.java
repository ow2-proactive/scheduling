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
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLConnection;
import java.util.Map;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;

import com.google.common.collect.Maps;


public class SubmitJobCommand extends AbstractCommand implements Command {
    private final String pathname;

    private String variables;

    private final JobKeyValueTransformer jobKeyValueTransformer;

    public SubmitJobCommand(String... params) {

        this.pathname = params[0];
        if (params.length > 1) {
            this.variables = params[1];
        }
        this.jobKeyValueTransformer = new JobKeyValueTransformer();
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        File jobFile = new File(pathname);
        if (!jobFile.exists()) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, String.format("'%s' does not exist.", pathname));
        }
        try {
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

    private Map<String, String> map(String variables) {
        return jobKeyValueTransformer.transformVariablesToMap(variables);
    }
}
