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

import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.json.MBeanInfoView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.QueryStringBuilder;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;


public class RmStatsCommand extends AbstractCommand implements Command {
    private static final String[] RUNTIME_DATA_ATTR = new String[] { "MaxBusyNodes", "MaxConfiguringNodes",
                                                                     "ToBeReleasedNodesCount", "DeployingNodesCount",
                                                                     "MaxDeployingNodes", "BusyNodesCount",
                                                                     "AvailableNodesCount", "MaxLostNodes",
                                                                     "MaxFreeNodes", "AverageInactivity", "Status",
                                                                     "LostNodesCount", "FreeNodesCount",
                                                                     "DownNodesCount", "AverageActivity",
                                                                     "MaxDownNodes", "ConfiguringNodesCount",
                                                                     "MaxToBeReleasedNodes" };

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        StringBuilder url = new StringBuilder();
        url.append("info/ProActiveResourceManager:name=RuntimeData?");
        QueryStringBuilder builder = new QueryStringBuilder();
        for (String attr : RUNTIME_DATA_ATTR) {
            builder.add("attr", attr);
        }
        url.append(builder.buildString());
        HttpGet request = new HttpGet(currentContext.getResourceUrl(url.toString()));
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            List<MBeanInfoView> infoList = readValue(response, new TypeReference<List<MBeanInfoView>>() {
            }, currentContext);
            MBeanInfoView[] stats = infoList.toArray(new MBeanInfoView[infoList.size()]);
            resultStack(currentContext).push(stats);
            if (!currentContext.isSilent()) {
                writeLine(currentContext, "%s", StringUtility.mBeanInfoAsString(stats));
            }
        } else {
            handleError("An error occurred while retrieving stats:", response, currentContext);
        }

    }
}
