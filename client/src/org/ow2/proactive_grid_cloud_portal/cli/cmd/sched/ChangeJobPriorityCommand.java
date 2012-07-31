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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;

public class ChangeJobPriorityCommand extends AbstractJobCommand implements
        Command {
    private String priorityValue;

    public ChangeJobPriorityCommand(String jobId, String priorityValue) {
        super(jobId);
        this.priorityValue = priorityValue;
    }

    @Override
    public void execute() throws Exception {
        String resourceUrl = resourceUrl("jobs/" + jobId + "/priority/byvalue/"
                + priorityValue);
        HttpPut request = new HttpPut(resourceUrl);
        HttpResponse response = execute(request);
        if (statusCode(NO_CONTENT) == statusCode(response)) {
            writeLine("%s priority changed successfully.", job());
        } else {
            handleError(String.format(
                    "An error occurred while changing %s priority:", job()),
                    response);
        }
    }
}
