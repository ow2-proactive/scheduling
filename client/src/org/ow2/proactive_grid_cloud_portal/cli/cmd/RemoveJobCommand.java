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
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.ow2.proactive_grid_cloud_portal.cli.ResponseStatus.OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;

public class RemoveJobCommand extends AbstractJobCommand implements Command {

    public RemoveJobCommand(String jobId) {
        super(jobId);
    }

    @Override
    public void execute() throws Exception {
        HttpDelete request = new HttpDelete(resourceUrl("jobs/" + jobId));
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            boolean success = readValue(response, Boolean.TYPE);
            if (success) {
                writeLine("%s removed", job());
            } else {
                writeLine("%s cannot be removed", job());
            }
        } else {
            handleError("An error occured while attempting to remove " + job(),
                    response);
        }
    }

}
