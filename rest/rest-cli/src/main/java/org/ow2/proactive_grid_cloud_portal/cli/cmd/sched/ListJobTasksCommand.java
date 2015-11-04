/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import java.util.List;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;


/**
 * @author  the activeeon team.
 */
public class ListJobTasksCommand extends AbstractJobTagPaginatedCommand implements Command {

    private ListJobTasksCommand() {
        super("undefined");
    }
    
    /*
     * Utility class to build ListJobTasksCommand objects.
     */
    public static class LJTCommandBuilder {
        
        private ListJobTasksCommand obj;
        
        private LJTCommandBuilder() {
            obj = new ListJobTasksCommand();
        }
        
        public static LJTCommandBuilder newInstance() {
            return new LJTCommandBuilder();
        }
        
        public LJTCommandBuilder jobId(String jobId) {
            obj.jobId = jobId;
            return this;
        }
        
        public LJTCommandBuilder offset(String offset) {
            obj.offset = Integer.valueOf(offset);
            return this;
        }
        
        public LJTCommandBuilder limit(String limit) {
            obj.limit = Integer.valueOf(limit);
            return this;
        }
        
        public LJTCommandBuilder tag(String tag) {
            obj.tag = tag;
            return this;
        }
        
        public ListJobTasksCommand instance() {
            return obj;
        }
        
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
        try {
            List<String> tasks = null;
            if (this.tag != null) {
                if (this.limit == 0) {
                    tasks = scheduler.getJobTasksIdsByTag(currentContext.getSessionId(), jobId, tag)
                            .getList();
                } else {
                    tasks = scheduler.getJobTasksIdsByTagPaginated(currentContext.getSessionId(), jobId, tag,
                            offset, limit).getList();
                }
            } else {
                if (this.limit == 0) {
                    tasks = scheduler.getTasksNames(currentContext.getSessionId(), jobId).getList();
                } else {
                    tasks = scheduler
                            .getTasksNamesPaginated(currentContext.getSessionId(), jobId, offset, limit)
                            .getList();
                }
            }

            resultStack(currentContext).push(tasks);

            if (!currentContext.isSilent()) {
                writeLine(currentContext, "%s", tasks);
            }
        } catch (Exception e) {
            String message = null;
            if (this.tag == null) {
                message = String.format("An error occurred while retrieving %s tasks:", job());
            } else {
                message = String.format("An error occurred while retrieving %s tasks filtered by tag %s:",
                        job(), tag);
            }
            handleError(message, e, currentContext);
        }
    }
}
