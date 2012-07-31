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

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import java.util.ArrayList;

import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive_grid_cloud_portal.cli.RestCommand;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

public class JSHelpCommand extends AbstractCommand implements Command {

    public JSHelpCommand() {
    }

    @Override
    public void execute() throws Exception {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(100);
        formatter.setSpace(2);
        ArrayList<String> titles = new ArrayList<String>();
        titles.add("Command");
        titles.add("Description");
        formatter.setTitle(titles);
        formatter.addEmptyLine();
        ArrayList<String> row;
        for (RestCommand command : RestCommand.values()) {
            if (command.getJsOpt() != null) {
                row = new ArrayList<String>();
                row.add(command.getJsOpt());
                row.add(command.getDescription());
                formatter.addLine(row);
            }
        }
        writeLine(StringUtility.string(formatter));
    }

}
