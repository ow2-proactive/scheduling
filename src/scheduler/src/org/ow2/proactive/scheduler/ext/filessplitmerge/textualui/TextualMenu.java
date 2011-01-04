/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.Command;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands.CommandResult;


public class TextualMenu {

    public List<Command> commands;
    private String menuName;

    public TextualMenu(String menuName, List<Command> cmds) {
        this.menuName = menuName;
        this.commands = cmds;
    }

    public TextualMenu(String menuName) {
        this.menuName = menuName;

    }

    public void addCommand(Command c) {
        if (commands == null) {
            commands = new LinkedList<Command>();
        }
        commands.add(c);
    }

    public String list() {
        StringBuilder out = new StringBuilder("\n" + menuName + "\n\n");
        if (this.commands == null)
            return "this is an empty menu";

        Iterator<Command> it = commands.iterator();
        while (it.hasNext()) {
            Command cmd = it.next();
            out.append(String.format(" %1$-18s\t " + cmd.getDescription() + "\n", cmd.getCmd()));
        }

        out.append(">");
        return out.toString();

    }

    public CommandResult handleCommand(String command) {
        Command cmd = this.getCommand(command);
        if (cmd == null)
            return null;

        CommandResult cr = cmd.execute();
        return cr;

    }

    private Command getCommand(String command) {
        Iterator<Command> it = commands.iterator();
        while (it.hasNext()) {
            Command cmd = it.next();
            if (cmd.getCmd().trim().toLowerCase().equals(command.trim().toLowerCase()))
                return cmd;
        }

        return null;

    }

    public void setMenuName(String name) {
        this.menuName = name;
    }

}
