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

package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import javax.security.auth.login.LoginException;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;

public class LoginSchedCommand extends AbstractLoginCommand implements Command {

    public static final String USERNAME = "org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand.username";
    public static final String PASSWORD = "org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand.password";

    private String username;

    public LoginSchedCommand(String username) {
        this.username = username;
    }

    @Override
    protected String login(ApplicationContext currentContext)
            throws CLIException {
        String password = currentContext.getProperty(PASSWORD, String.class);
        if (password == null) {
            password = new String(readPassword(currentContext, "password:"));
        }

        SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
        try {
            return scheduler.login(username, password);
        } catch (LoginException e) {
            handleError("An error occurred while logging:", e, currentContext);
            throw new CLIException(REASON_OTHER, "An error occurred while logging.");
        } catch (SchedulerRestException e) {
            handleError("An error occurred while logging:", e, currentContext);
            throw new CLIException(REASON_OTHER, "An error occurred while logging.");
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
