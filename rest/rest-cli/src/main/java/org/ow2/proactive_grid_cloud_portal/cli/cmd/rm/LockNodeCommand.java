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
package org.ow2.proactive_grid_cloud_portal.cli.cmd.rm;

import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import org.apache.http.client.methods.HttpPost;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.QueryStringBuilder;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;


public class LockNodeCommand extends AbstractCommand implements Command {

    private String[] nodeUrls;

    public LockNodeCommand(String... nodeUrls) {
        if (StringUtility.isEmpty(nodeUrls)) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, "No value specified for node-urls.");
        }
        this.nodeUrls = nodeUrls;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        HttpPost request = new HttpPost(currentContext.getResourceUrl("node/lock"));
        QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
        for (String nodeUrl : nodeUrls) {
            queryStringBuilder.add("nodeurls", nodeUrl);
        }
        request.setEntity(queryStringBuilder.buildEntity(APPLICATION_FORM_URLENCODED));
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            boolean successful = readValue(response, Boolean.TYPE, currentContext);
            resultStack(currentContext).push(successful);
            if (successful) {
                writeLine(currentContext, "Node(s) locked successfully.");
            } else {
                writeLine(currentContext, "Cannot lock node(s).");
            }
        } else {
            handleError("An error occurred while locking nodes:", response, currentContext);
        }

    }

}
