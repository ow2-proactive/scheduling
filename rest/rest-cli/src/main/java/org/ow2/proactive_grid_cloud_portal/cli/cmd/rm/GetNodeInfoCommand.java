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

import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.json.NodeEventView;
import org.ow2.proactive_grid_cloud_portal.cli.json.RmStateView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;


public class GetNodeInfoCommand extends AbstractCommand implements Command {

    private String nodeUrl;

    public GetNodeInfoCommand(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        HttpGet request = new HttpGet(currentContext.getResourceUrl("monitoring"));
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            RmStateView state = readValue(response, RmStateView.class, currentContext);
            NodeEventView[] nodeEvents = state.getNodesEvents();
            NodeEventView target = null;
            for (NodeEventView nodeEvent : nodeEvents) {
                if (nodeUrl.equals(nodeEvent.getNodeUrl())) {
                    target = nodeEvent;
                    break;
                }
            }
            if (target == null) {
                writeLine(currentContext, "Cannot find node: '%s'", nodeUrl);
                return;
            }
            resultStack(currentContext).push(target);
            writeLine(currentContext, "%s", target.getNodeInfo());
        } else {
            handleError("An error occurred while retrieving node info.", response, currentContext);
        }
    }

}
