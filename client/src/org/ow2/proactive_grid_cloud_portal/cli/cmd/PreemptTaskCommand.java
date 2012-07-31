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

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;

public class PreemptTaskCommand extends AbstractTaskCommand implements Command {

    public PreemptTaskCommand(String jobId, String taskId) {
        this(jobId, taskId, null);
    }

    public PreemptTaskCommand(String jobId, String taskId, String delay) {
        super(jobId, taskId);
    }

    @Override
    public void execute() throws Exception {
        HttpPut request = new HttpPut(resourceUrl("jobs/" + jobId + "/tasks/"
                + taskId + "/preempt"));
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            boolean success = readValue(response, Boolean.TYPE).booleanValue();
            if (success) {
                writeLine(
                        "%s has been stopped and will be rescheduled after 5 seconds.",
                        task());
            } else {
                writeLine(
                        "%s cannot be stopped and most likely it is not running.",
                        task());
            }
        } else {
            handleError(String.format(
                    "An error occurred while attempting to preemt %s:", task()),
                    response);
        }
    }

}
