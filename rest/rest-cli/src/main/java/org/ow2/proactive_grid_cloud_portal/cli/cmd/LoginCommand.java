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
package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;


public class LoginCommand extends AbstractLoginCommand implements Command {

    public static final String USERNAME = "org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand.username";

    public static final String PASSWORD = "org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand.password";

    private final String username;

    public LoginCommand(String username) {
        this.username = username;
    }

    @Override
    protected String login(ApplicationContext currentContext) throws CLIException {
        String password = currentContext.getProperty(PASSWORD, String.class);
        if (password == null) {
            password = new String(readPassword(currentContext, "password:"));
        }
        HttpPost request = new HttpPost(currentContext.getResourceUrl("login"));
        StringEntity entity = new StringEntity("username=" + username + "&password=" + password,
                                               APPLICATION_FORM_URLENCODED);
        request.setEntity(entity);
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            return StringUtility.responseAsString(response).trim();
        } else {
            throw buildCLIException(REASON_OTHER, response, currentContext);
        }
    }

    @Override
    protected String getAlias(ApplicationContext currentContext) {
        return currentContext.getProperty(USERNAME, String.class);
    }

    @Override
    protected void setAlias(ApplicationContext currentContext) {
        currentContext.setProperty(USERNAME, username);
    }
}
