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

import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.ext.filessplitmerge.event.InternalSchedulerEventListener;
import org.ow2.proactive.utils.Tools;


public class ListPendingJobsCmd extends Command {

    public ListPendingJobsCmd(String cmd, String description) {
        super(cmd, description);
        // TODO Auto-generated constructor stub
    }

    @Override
    public CommandResult execute() {
        return execute(null);
    }

    @Override
    public CommandResult execute(List params) {

        //SchedulerData sd = SchedulerData.getLocalView();
        String out = "";

        InternalSchedulerEventListener sd;
        try {
            sd = InternalSchedulerEventListener.getActiveAndLocalReferences()[1];

            List<JobState> jobs = sd.getPendingJobs();

            out = "*********** Pending Jobs ***************** \n";
            if (jobs.size() > 0) {
                Iterator<JobState> it = jobs.iterator();
                while (it.hasNext()) {
                    JobState job = it.next();

                    out += job.getName() + "---> ";
                    out += "number of tasks: " + job.getTotalNumberOfTasks() + ". ";
                    out += "Submited at " + Tools.getFormattedDate(job.getJobInfo().getSubmittedTime()) +
                        ". ";
                    out += "\n";
                }
            } else {
                out += "There are no pending jobs on the scheduler\n";
            }

            out += "******************************************";
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
