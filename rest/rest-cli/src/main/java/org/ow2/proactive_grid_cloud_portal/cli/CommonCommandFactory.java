/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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

package org.ow2.proactive_grid_cloud_portal.cli;

import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.ow2.proactive_grid_cloud_portal.cli.CommandSet.Entry;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.ImodeCommand;

import com.google.common.collect.ObjectArrays;

import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.INFRASTRUCTURE;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.OUTPUT;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.POLICY;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.SCHED_HELP;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.RM_HELP;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.COMMON_HELP;


class CommonCommandFactory extends CommandFactory {

    {
        for (Entry entry : ObjectArrays.concat(CommandSet.SCHED_ONLY, CommandSet.RM_ONLY, Entry.class)) {
            cmdMap.put(opt(entry), entry);
        }
    }

    @Override
    public List<Command> getCommandList(CommandLine cli, ApplicationContext currentContext) {
        Map<String, Command> commands = commandMapInstance(cli, cmdMap);
        List<Command> commandList = super.getCommandList(cli, commands, currentContext);
        return commandList;
    }

}
