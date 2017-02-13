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
package org.ow2.proactive_grid_cloud_portal.cli;

import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.ow2.proactive_grid_cloud_portal.cli.CommandSet.Entry;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;

import com.google.common.collect.ObjectArrays;


class CommonCommandFactory extends CommandFactory {

    {
        for (Entry entry : ObjectArrays.concat(CommandSet.SCHEDULER_ONLY, CommandSet.RM_ONLY, Entry.class)) {
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
