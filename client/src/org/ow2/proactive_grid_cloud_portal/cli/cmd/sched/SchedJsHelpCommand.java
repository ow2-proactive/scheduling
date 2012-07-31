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

import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.COMMON_COMMANDS;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.SCHED_JS_HELP;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.SCHED_ONLY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.CommandSet;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

public class SchedJsHelpCommand extends AbstractCommand implements Command {

    public SchedJsHelpCommand() {
    }

    @Override
    public void execute() throws Exception {
        printHelp(COMMON_COMMANDS, SCHED_ONLY,
                new CommandSet.Entry[] { SCHED_JS_HELP });
    }

    private void printHelp(CommandSet.Entry[]... entrySet) throws CLIException {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(100);
        formatter.setSpace(2);
        ArrayList<String> titles = new ArrayList<String>();
        titles.add("Command");
        titles.add("Description");
        formatter.setTitle(titles);
        formatter.addEmptyLine();
        ArrayList<String> line;

        List<CommandSet.Entry> entries = new ArrayList<CommandSet.Entry>();
        for (CommandSet.Entry[] es : entrySet) {
            Collections.addAll(entries, es);
        }
        Collections.sort(entries);

        for (CommandSet.Entry entry : entries) {
            if (entry.jsCommand() != null) {
                line = new ArrayList<String>();
                line.add(entry.jsCommand());
                line.add(entry.description());
                formatter.addLine(line);
            }
        }
        writeLine(StringUtility.string(formatter));
    }

}
