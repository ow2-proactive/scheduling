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
