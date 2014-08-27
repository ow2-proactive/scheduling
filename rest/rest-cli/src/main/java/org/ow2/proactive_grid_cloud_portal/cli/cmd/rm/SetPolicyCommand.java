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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.type.TypeReference;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;
import static org.ow2.proactive_grid_cloud_portal.cli.json.FieldMetaDataView.Type.CREDENTIAL;
import static org.ow2.proactive_grid_cloud_portal.cli.json.FieldMetaDataView.Type.FILEBROWSER;


public class SetPolicyCommand extends AbstractCommand implements Command {
    public static final String SET_POLICY = "org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetPolicyCommand.setPolicy";
    private String policyType;
    private String[] policyArgs;

    public SetPolicyCommand(String... args) {
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
                knownPolicies = new HashMap<String, PluginView>();
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
                throw new CLIException(REASON_INVALID_ARGUMENTS, String.format("Unknown policy type: %s",
                        policyType));
            }
            ConfigurableFieldView[] configurableFields = pluginView.getConfigurableFields();
            if (configurableFields.length != policyArgs.length) {
                throw new CLIException(REASON_INVALID_ARGUMENTS, String.format(
                        "Invalid number of arguments specified for '%s' type.", policyType));
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
