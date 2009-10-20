//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands;

import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.ext.filessplitmerge.event.InternalSchedulerEventListener;


public class ListRunningJobsCmd extends Command {

    public ListRunningJobsCmd(String cmd, String description) {
        super(cmd, description);
        // TODO Auto-generated constructor stub
    }

    @Override
    public CommandResult execute(List params) {
        String out = "";

        InternalSchedulerEventListener sd;
        try {
            sd = InternalSchedulerEventListener.getActiveAndLocalReferences()[1];
            List<JobState> jobs = sd.getRunningJobs();

            out = "*********** Running Jobs ***************** \n";
            if (jobs.size() > 0) {
                Iterator<JobState> it = jobs.iterator();
                while (it.hasNext()) {
                    JobState job = it.next();
                    out += job.getName() + "---> ";
                    out += " finished tasks: " + job.getNumberOfFinishedTasks() + " ";
                    out += " running tasks: " + job.getNumberOfRunningTasks() + " ";
                    out += " pending tasks: " + job.getNumberOfPendingTasks() + " ";
                    out += "\n";
                }
            } else {
                out += "There are no running jobs on the scheduler\n";
            }

            out += "*******************************************\n";

        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
            out += "INFO is not available.";
        } catch (NodeException e) {
            e.printStackTrace();
            out += "INFO is not available.";
        }

        CommandResult cr = new CommandResult(out);
        return cr;
    }

    @Override
    public CommandResult execute() {
        return execute(null);
    }

}
