//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands;

import java.util.List;


public abstract class Command {

    private String cmd;
    private String description;

    public Command(String cmd, String description) {
        this.cmd = cmd;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This methods reads the input data for the command and execute the comand
     * 
     */
    public abstract CommandResult execute();

    /**
     * This execute the command using the data received in argument
     * @param params
     * @return
     */
    public CommandResult execute(List params) {
        return new CommandResult("Command not implemented");
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

}
