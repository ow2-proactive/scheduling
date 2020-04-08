/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestMapPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;


public class ListJobCommand extends AbstractCommand implements Command {

    private String[] filterings;

    private int offset = -1;

    private int beginIndex = 0;

    public ListJobCommand(String... filtering) {
        this.filterings = filtering;
        processFilteringOps();
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        try {
            int numOfJobs = getNumberOfJobs(beginIndex, offset, currentContext);
            printJobsList(beginIndex, numOfJobs, currentContext);
        } catch (Exception e) {
            handleError("An error occurred while retrieving job list:", e, currentContext);
        }
    }

    private void processFilteringOps() {
        if (filterings != null) {
            try {
                for (String nameValue : filterings) {
                    if (nameValue.startsWith("latest=") || nameValue.startsWith("limit=")) {
                        setOffset(valueAsInt(nameValue));
                    } else if (nameValue.startsWith("from=")) {
                        beginIndex = valueAsInt(nameValue);
                    }
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid filtering option(s).", e);
            }
        }
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    private void printJobsList(int index, int offset, ApplicationContext currentContext)
            throws RestException, IOException {
        RestMapPage<Long, ArrayList<UserJobData>> page = currentContext.getRestClient()
                                                                       .getScheduler()
                                                                       .revisionAndJobsInfo(currentContext.getSessionId(),
                                                                                            index,
                                                                                            offset,
                                                                                            false,
                                                                                            true,
                                                                                            true,
                                                                                            true);
        Map<Long, ArrayList<UserJobData>> stateMap = page.getMap();
        List<UserJobData> jobs = stateMap.values().iterator().next();
        currentContext.getDevice().writeLine("%s", StringUtility.jobsAsString(jobs));
    }

    private int getNumberOfJobs(int index, int offset, ApplicationContext currentContext) throws Exception {
        return currentContext.getRestClient()
                             .getScheduler()
                             .jobs(currentContext.getSessionId(), index, offset)
                             .getList()
                             .size();
    }

    private Integer valueAsInt(String nameValue) {
        return Integer.valueOf(nameValue.split("\\s*=\\s*")[1]);
    }

}
