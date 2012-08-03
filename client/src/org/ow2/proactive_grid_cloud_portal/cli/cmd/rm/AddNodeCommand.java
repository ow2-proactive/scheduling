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

import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetNodeSourceCommand.SET_NODE_SOURCE;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpUtility;

public class AddNodeCommand extends AbstractCommand implements Command {
    private String nodeUrl;
    private String nodeSource;

    public AddNodeCommand(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public AddNodeCommand(String nodeUrl, String nodeSource) {
        this.nodeUrl = nodeUrl;
        this.nodeSource = nodeSource;
    }

    @Override
    public void execute() throws CLIException {
        HttpPost request = new HttpPost(resourceUrl("node"));
        StringBuilder buffer = new StringBuilder();
        buffer.append("nodeurl=" + HttpUtility.encodeUrl(nodeUrl));
        if (context().getProperty(SET_NODE_SOURCE, String.class) != null) {
            nodeSource = context().getProperty(SET_NODE_SOURCE, String.class);
        }
        if (nodeSource != null) {
            buffer.append("&nodesource=").append(nodeSource);
        }
        StringEntity entity = new StringEntity(buffer.toString(),
                APPLICATION_FORM_URLENCODED);
        request.setEntity(entity);
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            boolean successful = readValue(response, Boolean.TYPE);
            resultStack().push(successful);
            if (successful) {
                writeLine("Node('%s') added successfully.", nodeUrl);
            } else {
                writeLine("Cannot add node('%s').", nodeUrl);
            }
        } else {
            handleError("An error occurred while adding the node:", response);
        }
    }
}
