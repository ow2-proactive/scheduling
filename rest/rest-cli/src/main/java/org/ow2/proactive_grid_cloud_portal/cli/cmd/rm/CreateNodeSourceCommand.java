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

import static org.apache.http.entity.ContentType.*;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetInfrastructureCommand.SET_INFRASTRUCTURE;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetNodeSourceCommand.SET_NODE_SOURCE;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetPolicyCommand.SET_POLICY;

import org.apache.http.client.methods.HttpPost;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.QueryStringBuilder;


public class CreateNodeSourceCommand extends AbstractCommand implements Command {
    private String nodeSource;

    public CreateNodeSourceCommand(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        QueryStringBuilder infrastructure = currentContext.getProperty(SET_INFRASTRUCTURE, QueryStringBuilder.class);
        QueryStringBuilder policy = currentContext.getProperty(SET_POLICY, QueryStringBuilder.class);
        if (infrastructure == null) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, "Infrastructure not specified");
        }
        if (policy == null) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, "Policy not specified");
        }
        if (currentContext.getProperty(SET_NODE_SOURCE, String.class) != null) {
            nodeSource = currentContext.getProperty(SET_NODE_SOURCE, String.class);
        }
        HttpPost request = new HttpPost(currentContext.getResourceUrl("nodesource/create"));
        QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
        queryStringBuilder.add("nodeSourceName", nodeSource).addAll(infrastructure).addAll(policy);
        request.setEntity(queryStringBuilder.buildEntity(APPLICATION_FORM_URLENCODED));
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            boolean success = readValue(response, Boolean.TYPE, currentContext);
            resultStack(currentContext).push(success);
            if (success) {
                writeLine(currentContext, "Node source successfully created.");
            } else {
                writeLine(currentContext, "%s %s", "Cannot create node source:", nodeSource);
            }
        } else {
            handleError("An error occurred while creating node source:", response, currentContext);

        }
    }
}
