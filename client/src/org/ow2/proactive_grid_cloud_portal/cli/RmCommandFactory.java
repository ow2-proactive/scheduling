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
 * %$ACTIVEEON_INITIAL_DEV$
 */

package org.ow2.proactive_grid_cloud_portal.cli;

import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.INFRASTRUCTURE;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.OUTPUT;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.POLICY;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.RM_HELP;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.ow2.proactive_grid_cloud_portal.cli.CommandSet.Entry;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RmImodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HierarchicalMap;

class RmCommandFactory extends CommandFactory {

    private static final Map<String, CommandSet.Entry> rmSupportedCmdMap = new HierarchicalMap<String, CommandSet.Entry>(
            CommandFactory.supportedCommandMap());

    static {
        for (CommandSet.Entry entry : CommandSet.RM_ONLY) {
            rmSupportedCmdMap.put(opt(entry), entry);
        }
    }

    @Override
    public List<Command> getCommandList(CommandLine cli) {
        Map<String, Command> commands = commandMapInstance(cli,
                rmSupportedCmdMap);
        List<Command> commandList = getCommandList(cli, commands);
        if (cli.hasOption(opt(RM_HELP))) {
            return commandList;
        }
        if (commands.containsKey(opt(INFRASTRUCTURE))) {
            commandList.add(commands.remove(opt(INFRASTRUCTURE)));
        }
        if (commands.containsKey(opt(POLICY))) {
            commandList.add(commands.remove(opt(POLICY)));
        }

        if (commands.isEmpty()) {
            commandList.add(new RmImodeCommand());
        } else {
            Command output = commands.remove(opt(OUTPUT));
            commandList.addAll(commands.values());
            if (output != null) {
                commandList.add(output);
            }
        }
        return commandList;
    }

    @Override
    public Options supportedOptions() {
        return createOptions(rmSupportedCmdMap.values());
    }

    @Override
    public Command commandForOption(Option option) {
        return getCommandForOption(option, rmSupportedCmdMap);
    }

    @Override
    public CommandSet.Entry[] supportedCommandEntries() {
        Collection<Entry> entries = rmSupportedCmdMap.values();
        return entries.toArray(new CommandSet.Entry[entries.size()]);
    }

    @Override
    public void addCommandEntry(Entry entry) {
        rmSupportedCmdMap.put(opt(entry), entry);
    }
}
