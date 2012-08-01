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
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.json.MBeanInfoView;

public class RmStatsCommand extends AbstractCommand implements Command {
    private static final String[] RUNTIME_DATA_ATTR = new String[] {
            "MaxBusyNodes", "MaxConfiguringNodes", "ToBeReleasedNodesCount",
            "DeployingNodesCount", "MaxDeployingNodes", "BusyNodesCount",
            "AvailableNodesCount", "MaxLostNodes", "MaxFreeNodes",
            "AverageInactivity", "Status", "LostNodesCount", "FreeNodesCount",
            "DownNodesCount", "AverageActivity", "MaxDownNodes",
            "ConfiguringNodesCount", "MaxToBeReleasedNodes" };

    @Override
    public void execute() throws CLIException {
        StringBuilder url = new StringBuilder();
        url.append("info/ProActiveResourceManager:name=RuntimeData?");
        int index = 0;
        url.append("attr=").append(RUNTIME_DATA_ATTR[index++]);
        while (index < RUNTIME_DATA_ATTR.length) {
            url.append("&attr=").append(RUNTIME_DATA_ATTR[index++]);
        }
        HttpGet request = new HttpGet(resourceUrl(url.toString()));
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            List<MBeanInfoView> infoList = readValue(response,
                    new TypeReference<List<MBeanInfoView>>() {
                    });
            ObjectArrayFormatter formatter = new ObjectArrayFormatter();
            formatter.setMaxColumnLength(80);
            formatter.setSpace(4);
            List<String> titles = new ArrayList<String>();
            titles.add("Stats:");
            titles.add("");
            formatter.setTitle(titles);
            formatter.addEmptyLine();
            for (MBeanInfoView info : infoList) {
                List<String> line = new ArrayList<String>();
                line.add("" + info.getName());
                line.add("" + info.getValue());
                formatter.addLine(line);
            }
            writeLine("%s", formatter.getAsString());

        } else {
            handleError("An error occurred while retrieving stats:", response);
        }

    }
}
