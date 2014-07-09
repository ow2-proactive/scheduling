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

package org.ow2.proactive_grid_cloud_portal.cli.cmd.rm;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetInfrastructureCommand.SET_INFRASTRUCTURE;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetNodeSourceCommand.SET_NODE_SOURCE;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetPolicyCommand.SET_POLICY;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;


public class CreateNodeSourceCommand extends AbstractCommand implements Command {
    private String nodeSource;

    public CreateNodeSourceCommand(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        String infrastructure = currentContext.getProperty(SET_INFRASTRUCTURE, String.class);
        String policy = currentContext.getProperty(SET_POLICY, String.class);
        if (infrastructure == null) {
            throw new CLIException(REASON_INVALID_ARGUMENTS,
                "No value is specified for one of infrastructure parameters");
        }
        if (policy == null) {
            throw new CLIException(REASON_INVALID_ARGUMENTS,
                "No value is specified for one of policy parameters");
        }
        if (currentContext.getProperty(SET_NODE_SOURCE, String.class) != null) {
            nodeSource = currentContext.getProperty(SET_NODE_SOURCE, String.class);
        }
        HttpPost request = new HttpPost(currentContext.getResourceUrl("nodesource/create"));
        String requestContents = (new StringBuilder()).append("nodeSourceName=").append(nodeSource).append(
                '&').append(infrastructure).append('&').append(policy).toString();
        request.setEntity(new StringEntity(requestContents, ContentType.APPLICATION_FORM_URLENCODED));
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
