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
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import org.apache.http.client.methods.HttpPut;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.json.NSStateView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.QueryStringBuilder;


public class UndeployNodeSourceCommand extends AbstractCommand implements Command {

    private static final String RM_REST_ENDPOINT = "nodesource/undeploy";

    private String nodeSourceName;

    private boolean preempt;

    public UndeployNodeSourceCommand(String nodeSourceName) {
        this(nodeSourceName, Boolean.toString(false));
    }

    public UndeployNodeSourceCommand(String nodeSourceName, String preempt) {
        this.nodeSourceName = nodeSourceName;
        this.preempt = Boolean.valueOf(preempt);
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        HttpPut request = new HttpPut(currentContext.getResourceUrl(RM_REST_ENDPOINT));
        QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
        queryStringBuilder.add("nodeSourceName", this.nodeSourceName).add("preempt", Boolean.toString(this.preempt));
        request.setEntity(queryStringBuilder.buildEntity(APPLICATION_FORM_URLENCODED));
        HttpResponseWrapper response = this.execute(request, currentContext);

        if (this.statusCode(OK) == this.statusCode(response)) {

            NSStateView nsState = this.readValue(response, NSStateView.class, currentContext);
            boolean success = nsState.isResult();
            this.resultStack(currentContext).push(success);
            if (success) {
                writeLine(currentContext, "Node source successfully undeployed.");
            } else {
                writeLine(currentContext, "%s %s", "Cannot undeploy node source:", this.nodeSourceName);
            }
        } else {

            this.handleError("An error occurred while undeploying node source:", response, currentContext);

        }
    }

}
