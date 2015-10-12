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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.json.PluginView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;


public class ListPolicyCommand extends AbstractCommand implements Command {

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        Map<String, PluginView> knownPolicyMap = currentContext.getPolicies();
        if (knownPolicyMap == null) {
            HttpGet request = new HttpGet(currentContext.getResourceUrl("policies"));
            HttpResponseWrapper response = execute(request, currentContext);
            if (statusCode(OK) == statusCode(response)) {
                List<PluginView> pluginViewList = readValue(response, new TypeReference<List<PluginView>>() {
                }, currentContext);
                resultStack(currentContext).push(
                        pluginViewList.toArray(new PluginView[pluginViewList.size()]));
                knownPolicyMap = new HashMap<>();
                for (PluginView pluginView : pluginViewList) {
                    knownPolicyMap.put(pluginView.getPluginName(), pluginView);
                }
                currentContext.setPolicies(knownPolicyMap);
            } else {
                handleError("An error occurred while retrieving supported policy types:", response,
                        currentContext);
            }
        }
        if (!currentContext.isSilent()) {
            if (knownPolicyMap != null) {
                writeLine(currentContext, "%n%s:%n", "Supported policy types");
                for (PluginView policy : knownPolicyMap.values()) {
                    writeLine(currentContext, "%s%n", policy.toString());
                }
            }
        }

    }
}
