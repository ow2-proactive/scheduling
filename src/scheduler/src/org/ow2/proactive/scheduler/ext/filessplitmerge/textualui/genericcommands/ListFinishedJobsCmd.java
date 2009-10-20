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
import org.ow2.proactive.utils.Tools;


public class ListFinishedJobsCmd extends Command {

    public ListFinishedJobsCmd(String cmd, String description) {
        super(cmd, description);
        // TODO Auto-generated constructor stub
    }

    @Override
    public CommandResult execute() {
        return execute(null);
    }

    @Override
    public CommandResult execute(List params) {
        // ///SchedulerData sd = SchedulerData.getLocalView();
        InternalSchedulerEventListener sd;
        String out = "";

        try {
            sd = InternalSchedulerEventListener.getActiveAndLocalReferences()[1];
            List<JobState> jobs = sd.getFinishedJobs();

            out = "*********** Finished Jobs ***************** \n";
            if (jobs.size() > 0) {
                Iterator<JobState> it = jobs.iterator();
                while (it.hasNext()) {
                    JobState job = it.next();

                    out += job.getName() + "---> ";
                    out += job.getStatus().toString() + ". ";
                    out += "Submited at " + Tools.getFormattedDate(job.getJobInfo().getSubmittedTime()) +
                        ". ";
                    out += "Started at " + Tools.getFormattedDate(job.getJobInfo().getStartTime()) + ". ";
                    out += "Finished at " + Tools.getFormattedDate(job.getJobInfo().getFinishedTime()) +
                        ". \n";
                    out += "Description: " + job.getDescription();
                    out += "\n\n";
                }
            } else {
                out += "There are no finished jobs on the scheduler\n";
            }

            out += "********************************************\n";
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            out += "INFO is not available.";
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            out += "INFO is not available.";
        }

        CommandResult cr = new CommandResult(out);
        return cr;
    }

}
