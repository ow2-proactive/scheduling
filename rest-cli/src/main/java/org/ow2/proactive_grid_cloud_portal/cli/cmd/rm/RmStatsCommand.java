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

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.json.MBeanInfoView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

public class RmStatsCommand extends AbstractCommand implements Command {
    private static final String[] RUNTIME_DATA_ATTR = new String[] {
            "MaxBusyNodes", "MaxConfiguringNodes", "ToBeReleasedNodesCount",
            "DeployingNodesCount", "MaxDeployingNodes", "BusyNodesCount",
            "AvailableNodesCount", "MaxLostNodes", "MaxFreeNodes",
            "AverageInactivity", "Status", "LostNodesCount", "FreeNodesCount",
            "DownNodesCount", "AverageActivity", "MaxDownNodes",
            "ConfiguringNodesCount", "MaxToBeReleasedNodes" };

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        StringBuilder url = new StringBuilder();
        url.append("info/ProActiveResourceManager:name=RuntimeData?");
        int index = 0;
        url.append("attr=").append(RUNTIME_DATA_ATTR[index++]);
        while (index < RUNTIME_DATA_ATTR.length) {
            url.append("&attr=").append(RUNTIME_DATA_ATTR[index++]);
        }
        HttpGet request = new HttpGet(currentContext.getResourceUrl(url
                .toString()));
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            List<MBeanInfoView> infoList = readValue(response,
                    new TypeReference<List<MBeanInfoView>>() {
                    }, currentContext);
            MBeanInfoView[] stats =infoList.toArray(new MBeanInfoView[infoList.size()]);
            resultStack(currentContext).push(stats);
            if (!currentContext.isSilent()) {
                writeLine(currentContext, "%s",
                        StringUtility.mBeanInfoAsString(stats));
            }
        } else {
            handleError("An error occurred while retrieving stats:", response,
                    currentContext);
        }

    }
}
