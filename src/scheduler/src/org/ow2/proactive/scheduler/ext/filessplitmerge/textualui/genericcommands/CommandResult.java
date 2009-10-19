//============================================================================
// Name        : ProActive Embarrassingly Parallel Framework 
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands;

import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.TextualMenu;


public class CommandResult {

    private String output;
    private TextualMenu tm;

    public CommandResult(String output) {
        this.output = output;
    }

    public CommandResult(String output, TextualMenu tm) {
        this.output = output;
        this.tm = tm;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public TextualMenu getTm() {
        return tm;
    }

    public void setTm(TextualMenu tm) {
        this.tm = tm;
    }

}
