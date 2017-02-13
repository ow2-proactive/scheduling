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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.CommandSet;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

import com.google.common.collect.ImmutableList;


public abstract class AbstractJsHelpCommand extends AbstractCommand implements Command {

    public void printHelp(ApplicationContext currentContext, CommandSet.Entry[]... entrySet) throws CLIException {

        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(100);
        formatter.setSpace(2);
        formatter.setTitle(ImmutableList.of("Command", "Description"));
        formatter.addEmptyLine();

        boolean versionEntryAdded = false;

        List<CommandSet.Entry> entries = new ArrayList<>();
        for (CommandSet.Entry[] es : entrySet) {
            for (CommandSet.Entry entry : es) {
                if (entry.jsCommand() != null && entry.jsCommand().equals(CommandSet.VERSION.jsCommand())) {
                    if (!versionEntryAdded) {
                        entries.add(entry);
                        versionEntryAdded = true;
                    }
                } else {
                    entries.add(entry);
                }
            }
        }

        Collections.sort(entries);

        for (CommandSet.Entry entry : entries) {
            if (entry.jsCommand() != null) {
                formatter.addLine(ImmutableList.of(entry.jsCommand(), entry.description()));
            }
        }

        writeLine(currentContext, "%s", StringUtility.objectArrayFormatterAsString(formatter));
    }

}
