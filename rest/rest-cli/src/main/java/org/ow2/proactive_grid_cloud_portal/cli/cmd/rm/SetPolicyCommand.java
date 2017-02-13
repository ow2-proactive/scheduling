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

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;
import static org.ow2.proactive_grid_cloud_portal.cli.json.FieldMetaDataView.Type.CREDENTIAL;
import static org.ow2.proactive_grid_cloud_portal.cli.json.FieldMetaDataView.Type.FILEBROWSER;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.json.ConfigurableFieldView;
import org.ow2.proactive_grid_cloud_portal.cli.json.FieldMetaDataView.Type;
import org.ow2.proactive_grid_cloud_portal.cli.json.PluginView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.QueryStringBuilder;


public class SetPolicyCommand extends AbstractCommand implements Command {
    public static final String SET_POLICY = "org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetPolicyCommand.setPolicy";

    private String policyType;

    private String[] policyArgs;

    public SetPolicyCommand(String[] args) {
        if (!(args.length > 0)) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, "At least one argument required.");
        }
        this.policyType = args[0];

        if (args.length > 1) {
            this.policyArgs = new String[args.length - 1];
            System.arraycopy(args, 1, policyArgs, 0, args.length - 1);
        }
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        Map<String, PluginView> knownPolicies = currentContext.getPolicies();
        if (knownPolicies == null) {
            HttpGet request = new HttpGet(currentContext.getResourceUrl("policies"));
            HttpResponseWrapper response = execute(request, currentContext);
            if (statusCode(OK) == statusCode(response)) {
                knownPolicies = new HashMap<>();
                List<PluginView> pluginViewList = readValue(response, new TypeReference<List<PluginView>>() {
                }, currentContext);
                for (PluginView pluginView : pluginViewList) {
                    knownPolicies.put(pluginView.getPluginName(), pluginView);
                }
                currentContext.setPolicies(knownPolicies);
            } else {
                handleError("Unable to retrieve known policy types:", response, currentContext);
            }
        }

        if (knownPolicies != null) {
            PluginView pluginView = knownPolicies.get(policyType);

            if (pluginView == null) {
                throw new CLIException(REASON_INVALID_ARGUMENTS, String.format("Unknown policy type: %s", policyType));
            }
            ConfigurableFieldView[] configurableFields = pluginView.getConfigurableFields();
            if (configurableFields.length != policyArgs.length) {
                throw new CLIException(REASON_INVALID_ARGUMENTS,
                                       String.format("Invalid number of arguments specified for '%s' type.",
                                                     policyType));
            }

            QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
            queryStringBuilder.add("policyType", policyType);
            for (int index = 0; index < configurableFields.length; index++) {
                ConfigurableFieldView cf = configurableFields[index];
                Type ft = cf.getMeta().type();
                if (FILEBROWSER.equals(ft) || CREDENTIAL.equals(ft)) {
                    String contents = FileUtility.readFileToString(new File(policyArgs[index]));
                    queryStringBuilder.add("policyFileParameters", contents);
                } else {
                    queryStringBuilder.add("policyParameters", policyArgs[index]);
                }
            }
            currentContext.setProperty(SET_POLICY, queryStringBuilder);
        }
    }
}
