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

import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetInfrastructureCommand.SET_INFRASTRUCTURE;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetNodeSourceCommand.SET_NODE_SOURCE;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetPolicyCommand.SET_POLICY;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.QueryStringBuilder;


public abstract class NodeSourceCommand extends AbstractCommand implements Command {

    protected String nodeSourceName;

    protected NodeSourceCommand(String nodeSourceName) {
        this.nodeSourceName = nodeSourceName;
    }

    protected abstract String getResourceUrlEndpoint();

    protected HttpResponseWrapper executeRequestWithQuery(ApplicationContext currentContext,
            QueryStringBuilder queryStringBuilder, HttpVerb httpVerb) {

        HttpEntityEnclosingRequestBase request;

        switch (httpVerb) {
            case POST:
                request = new HttpPost(currentContext.getResourceUrl(getResourceUrlEndpoint()));
                break;
            case PUT:
                request = new HttpPut(currentContext.getResourceUrl(getResourceUrlEndpoint()));
                break;
            default:
                throw new IllegalArgumentException("Cannot execute node source command " +
                                                   this.getClass().getSimpleName() + " because HTTP verb " + httpVerb +
                                                   " is not recognized");
        }

        request.setEntity(queryStringBuilder.buildEntity(APPLICATION_FORM_URLENCODED));

        return this.execute(request, currentContext);
    }

    protected QueryStringBuilder getQuery(ApplicationContext currentContext) {

        QueryStringBuilder infrastructureArguments = getArgumentsOrFail(currentContext,
                                                                        SET_INFRASTRUCTURE,
                                                                        "Infrastructure not specified");
        QueryStringBuilder policyArguments = getArgumentsOrFail(currentContext, SET_POLICY, "Policy not specified");

        setNodeSourceNameWithCommand(currentContext);

        QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
        queryStringBuilder.add("nodeSourceName", this.nodeSourceName)
                          .addAll(infrastructureArguments)
                          .addAll(policyArguments);

        return queryStringBuilder;
    }

    private QueryStringBuilder getArgumentsOrFail(ApplicationContext currentContext, String setArgumentsCommand,
            String errorMessage) {

        QueryStringBuilder infrastructure = currentContext.getProperty(setArgumentsCommand, QueryStringBuilder.class);

        if (infrastructure == null) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, errorMessage);
        }

        return infrastructure;
    }

    private void setNodeSourceNameWithCommand(ApplicationContext currentContext) {

        if (currentContext.getProperty(SET_NODE_SOURCE, String.class) != null) {
            this.nodeSourceName = currentContext.getProperty(SET_NODE_SOURCE, String.class);
        }
    }

    protected enum HttpVerb {
        POST,
        PUT
    }

}
