//============================================================================
// Name        : ProActive Embarrassingly Parallel Framework 
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands;

import java.util.List;


public class ListAllJobsCmd extends Command {

    public ListAllJobsCmd(String cmd, String description) {
        super(cmd, description);
        // TODO Auto-generated constructor stub
    }

    @Override
    public CommandResult execute() {
        return execute(null);
    }

    @Override
    public CommandResult execute(List params) {

        String pendingJobs = new ListPendingJobsCmd("", "").execute().getOutput();
        String runningJobs = new ListRunningJobsCmd("", "").execute().getOutput();
        String finishedJobs = new ListFinishedJobsCmd("", "").execute().getOutput();
        String out = pendingJobs + "\n" + runningJobs + "\n" + finishedJobs + "\n";

        CommandResult cr = new CommandResult(out);
        return cr;
    }

}
