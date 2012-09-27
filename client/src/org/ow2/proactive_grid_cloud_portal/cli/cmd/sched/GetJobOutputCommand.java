/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.NO_CONTENT;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

public class GetJobOutputCommand extends AbstractJobCommand implements Command {

    public GetJobOutputCommand(String jobId) {
        super(jobId);
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        HttpGet request = new HttpGet(currentContext.getResourceUrl("jobs/"
                + jobId + "/result/value"));
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            Map<String, String> jobOutput = readValue(response,
                    new TypeReference<Map<String, String>>() {
                    }, currentContext);
            resultStack(currentContext).push(jobOutput);
            if (!currentContext.isSilent()) {
                writeLine(currentContext, "%s",
                        StringUtility.jobOutputAsString(job(), jobOutput));
            }

        } else if (statusCode(NO_CONTENT) == statusCode(response)) {
            writeLine(currentContext, "%s output not available.", job());

        } else {
            handleError(String.format(
                    "An error occurred while retrieving %s output:", job()),
                    response, currentContext);
        }
    }
}
