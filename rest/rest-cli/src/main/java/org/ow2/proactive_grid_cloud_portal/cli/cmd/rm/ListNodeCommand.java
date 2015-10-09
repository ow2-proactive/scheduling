/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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

import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.json.NodeEventView;
import org.ow2.proactive_grid_cloud_portal.cli.json.RmStateView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;


public class ListNodeCommand extends AbstractCommand implements Command {
    private String nodeSource = null;

    public ListNodeCommand() {
    }

    public ListNodeCommand(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        HttpGet request = new HttpGet(currentContext.getResourceUrl("monitoring"));
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            RmStateView state = readValue(response, RmStateView.class, currentContext);
            NodeEventView[] nodeEvents = state.getNodesEvents();
            NodeEventView[] selectedNodeEvents = null;
            if (nodeEvents != null) {
                if (nodeSource == null) {
                    selectedNodeEvents = nodeEvents;
                } else {
                    List<NodeEventView> selectedList = new ArrayList<>();
                    for (NodeEventView nodeEvent : nodeEvents) {
                        if (!nodeSource.equals(nodeEvent.getNodeSource())) {
                            // node source doesn't match
                            continue;
                        } else {
                            selectedList.add(nodeEvent);
                        }
                    }
                    selectedNodeEvents = selectedList.toArray(new NodeEventView[selectedList.size()]);
                }
            }
            resultStack(currentContext).push(selectedNodeEvents);
            writeLine(currentContext, "%s", StringUtility.string(selectedNodeEvents));

        } else {
            handleError("An error occurred while retrieving nodes:", response, currentContext);
        }
    }

}
