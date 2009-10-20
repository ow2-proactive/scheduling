//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

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
