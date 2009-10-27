//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.genericcommands;

import java.util.List;
import java.util.Set;

import org.ow2.proactive.scheduler.ext.filessplitmerge.JobPostTreatmentManagerHolder;
import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.NotInitializedException;
import org.ow2.proactive.scheduler.ext.filessplitmerge.logging.LoggerManager;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.TextualUI;


public class ExitCmd extends Command {

    public ExitCmd(String cmd, String description) {
        super(cmd, description);
        // TODO Auto-generated constructor stub
    }

    @Override
    public CommandResult execute() {
        return execute(null);
    }

    @Override
    public CommandResult execute(List params) {

        try {
            Set<String> awaitedJobsIds = JobPostTreatmentManagerHolder.getPostTreatmentManager()
                    .getAwaitedJobs();
            if ((awaitedJobsIds != null) && (awaitedJobsIds.size() > 0)) {
                TextualUI.info("This action will not remove the running jobs in the scheduler.", false);
                TextualUI.info("The results of these jobs are still awaited: " + awaitedJobsIds.toString(),
                        false);
                TextualUI
                        .info(
                                "Please run this application later in order to allow it to automatically gather the results of the above jobs.",
                                false);
            }
        } catch (NotInitializedException e) {
            LoggerManager.getLogger().error(e);
        }

        TextualUI.info("Goodbye!\n" + "Thank you for distributing your application with ProActive.", false);

        System.exit(0);
        return null;

    }

}
