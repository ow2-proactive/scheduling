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

import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.json.NodeEventView;
import org.ow2.proactive_grid_cloud_portal.cli.json.RmStateView;

public class ListNodeCommand extends AbstractCommand implements Command {
    private String nodeSource = null;

    public ListNodeCommand() {
    }

    public ListNodeCommand(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    @Override
    public void execute() throws CLIException {
        HttpGet request = new HttpGet(resourceUrl("monitoring"));
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            RmStateView state = readValue(response, RmStateView.class);
            ObjectArrayFormatter formatter = new ObjectArrayFormatter();
            formatter.setMaxColumnLength(80);
            formatter.setSpace(4);

            List<String> titles = new ArrayList<String>();
            titles.add("SOURCE_NAME");
            titles.add("HOST_NAME");
            titles.add("STATE");
            titles.add("SINCE");
            titles.add("URL");
            titles.add("PROVIDER");
            titles.add("USED_BY");
            formatter.setTitle(titles);

            formatter.addEmptyLine();

            NodeEventView[] nodeEvents = state.getNodesEvents();
            if (nodeEvents != null) {
                for (NodeEventView nodeEvent : nodeEvents) {
                    if (nodeSource != null
                            && !nodeSource.equals(nodeEvent.getNodeSource())) {
                        // node source doesn't match
                        continue;
                    }
                    List<String> line = new ArrayList<String>();
                    line.add(nodeEvent.getNodeSource());
                    line.add(nodeEvent.getHostName());
                    line.add(nodeEvent.getNodeState().toString());
                    long timestamp = Long.parseLong(nodeEvent.getTimeStamp());
                    String date = Tools.getFormattedDate(timestamp);
                    if (timestamp != -1) {
                        date += " (" + Tools.getElapsedTime(timestamp) + ")";
                    }
                    line.add(date);
                    line.add(nodeEvent.getNodeUrl());
                    line.add(nodeEvent.getNodeProvider() == null ? ""
                            : nodeEvent.getNodeProvider());
                    line.add(nodeEvent.getNodeOwner() == null ? "" : nodeEvent
                            .getNodeOwner());
                    formatter.addLine(line);
                }
            }
            writeLine(formatter.getAsString());

        } else {
            handleError("An error occurred while retrieving nodes:", response);
        }
    }

}
