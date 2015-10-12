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

package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.CommandFactory;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;

import static org.ow2.proactive_grid_cloud_portal.cli.cmd.HelpCommand.USAGE;


public class SchedHelpCommand extends AbstractCommand implements Command {

    public SchedHelpCommand() {
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        HelpFormatter formatter = new HelpFormatter();
        Writer writer = currentContext.getDevice().getWriter();
        PrintWriter pw = new PrintWriter(writer, true);
        Options options = CommandFactory.getCommandFactory(CommandFactory.Type.SCHEDULER).supportedOptions();
        formatter.printHelp(pw, 110, USAGE, "", options, formatter.getLeftPadding(), formatter
                .getDescPadding(), "", false);
    }
}
