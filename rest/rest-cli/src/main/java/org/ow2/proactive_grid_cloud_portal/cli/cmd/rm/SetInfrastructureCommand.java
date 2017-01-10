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
 * $$ACTIVE_INITIAL_DEV$$
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


public class SetInfrastructureCommand extends AbstractCommand implements Command {

    public static final String SET_INFRASTRUCTURE = "org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetInfrastructureCommand.setInfrastructure";

    private String infrastructureType;
    private String[] infrastructureArgs;

    public SetInfrastructureCommand(String[] args) {
        if (!(args.length > 0)) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, "At least one argument required.");
        }
        this.infrastructureType = args[0];

        if (args.length == 1) {
            infrastructureArgs = new String[0];
        } else {
            this.infrastructureArgs = new String[args.length - 1];
            System.arraycopy(args, 1, infrastructureArgs, 0, args.length - 1);
        }
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        Map<String, PluginView> infrastructures = currentContext.getInfrastructures();
        if (infrastructures == null) {
            HttpGet request = new HttpGet(currentContext.getResourceUrl("infrastructures"));
            HttpResponseWrapper response = execute(request, currentContext);
            if (statusCode(OK) == statusCode(response)) {
                infrastructures = new HashMap<>();
                List<PluginView> pluginViewList = readValue(response, new TypeReference<List<PluginView>>() {
                }, currentContext);
                for (PluginView pluginView : pluginViewList) {
                    infrastructures.put(pluginView.getPluginName(), pluginView);
                }
                currentContext.setInfrastructures(infrastructures);
            } else {
                handleError("Unable to retrieve known infrastructure types:", response, currentContext);
            }
        }

        if (infrastructures != null) {
            PluginView pluginView = infrastructures.get(infrastructureType);

            if (pluginView == null) {
                throw new CLIException(REASON_INVALID_ARGUMENTS, String.format(
                        "Unknown infrastructure type: %s", infrastructureType));
            }
            ConfigurableFieldView[] configurableFields = pluginView.getConfigurableFields();
            if (configurableFields.length != infrastructureArgs.length) {
                throw new CLIException(REASON_INVALID_ARGUMENTS, String.format(
                        "Invalid number of arguments specified for '%s' type.", infrastructureType));
            }

            QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
            queryStringBuilder.add("infrastructureType", infrastructureType);
            for (int index = 0; index < configurableFields.length; index++) {
                ConfigurableFieldView cf = configurableFields[index];
                Type ft = cf.getMeta().type();
                if (FILEBROWSER.equals(ft) || CREDENTIAL.equals(ft)) {
                    if ("".equals(infrastructureArgs[index])) {
                        String contents = "";
                        queryStringBuilder.add("infrastructureFileParameters", contents);
                    } else {
                        String contents = FileUtility.readFileToString(new File(infrastructureArgs[index]));
                        queryStringBuilder.add("infrastructureFileParameters", contents);
                    }
                } else {
                    queryStringBuilder.add("infrastructureParameters", infrastructureArgs[index]);
                }
            }
            currentContext.setProperty(SET_INFRASTRUCTURE, queryStringBuilder);
        }
    }

}
