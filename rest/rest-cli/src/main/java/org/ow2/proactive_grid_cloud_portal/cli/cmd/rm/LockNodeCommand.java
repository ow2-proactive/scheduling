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
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpUtility;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

public class LockNodeCommand extends AbstractCommand implements Command {

    private String[] nodeUrls;

    public LockNodeCommand(String... nodeUrls) {
        if (StringUtility.isEmpty(nodeUrls)) {
            throw new CLIException(REASON_INVALID_ARGUMENTS,
                    "No value specified for node-urls.");
        }
        this.nodeUrls = nodeUrls;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        HttpPost request = new HttpPost(
                currentContext.getResourceUrl("node/lock"));
        StringBuilder buffer = new StringBuilder();
        buffer.append("nodeurls=").append(nodeUrls[0]);
        if (nodeUrls.length > 1) {
            for (int index = 1; index < nodeUrls.length; index++) {
                buffer.append("&nodeurls=").append(
                        HttpUtility.encodeUrl(nodeUrls[index]));
            }
        }
        request.setEntity(new StringEntity(buffer.toString(),
                APPLICATION_FORM_URLENCODED));
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            boolean successful = readValue(response, Boolean.TYPE,
                    currentContext);
            resultStack(currentContext).push(successful);
            if (successful) {
                writeLine(currentContext, "Node(s) locked successfully.");
            } else {
                writeLine(currentContext, "Cannot lock node(s).");
            }
        } else {
            handleError("An error occurred while locking nodes:", response,
                    currentContext);
        }

    }

}
