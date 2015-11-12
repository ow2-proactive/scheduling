/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.RestConstants;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractIModeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestMapPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;

public class ListJobCommand extends AbstractCommand implements Command {

    private String[] filterings;

    private boolean offsetFlag = false;
    private int offset = -1;
    private int beginIndex = 0;

    public ListJobCommand() {
    }

    public ListJobCommand(String... filtering) {
        this.filterings = filtering;
        processFilteringOps();
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        try {
            if (isIteractiveMode(currentContext)) {
                int numOfJobsPerPage = getNumOfJobsPerPage(currentContext);
                int numOfJobs = getNumberOfJobs(beginIndex, offset, currentContext);
                if (numOfJobs < numOfJobsPerPage) {
                    printJobsList(beginIndex, numOfJobs, currentContext);
                } else {
                    int pages = (numOfJobs / numOfJobsPerPage) + ((numOfJobs % numOfJobsPerPage) == 0 ? 0 : 1);
                    int iterCount = 0;
                    boolean hasQuit = false;
                    // all pages except the last one
                    for (; iterCount < pages - 1; iterCount++) {
                        printJobsList(beginIndex + (iterCount * numOfJobsPerPage), numOfJobsPerPage, currentContext);
                        String response = readLine(currentContext,
                                "Page(%d/%d). Press ENTER to continue. 'q' to quit. ", (iterCount + 1), pages);
                        if ("q".equalsIgnoreCase(response)) {
                            hasQuit = true;
                            break;
                        }
                    }
                    if (!hasQuit) {
                        // last page
                        printJobsList(beginIndex + (iterCount * numOfJobsPerPage), numOfJobs - (iterCount * numOfJobsPerPage),
                                currentContext);
                    }
                }
            } else {
                printJobsList(beginIndex, (offsetFlag) ? offset : RestConstants.DFLT_PAGINATION_SIZE,
                        currentContext);
            }
        } catch (Exception e) {
            handleError("An error occurred while retrieving job list:", e, currentContext);
        }
    }

    private void processFilteringOps() {
        if (filterings != null) {
            try {
                for (String nameValue : filterings) {
                    if (nameValue.startsWith("latest=")) {
                        setOffset(valueAsInt(nameValue));
                    } else if (nameValue.startsWith("from=")) {
                        beginIndex = valueAsInt(nameValue);
                    } else if (nameValue.startsWith("limit=")) {
                        setOffset(valueAsInt(nameValue));
                    }
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid filtering option(s).", e);
            }
        }
    }

    private void setOffset(int offset) {
        this.offset = offset;
        this.offsetFlag = true;
    }

    private static boolean isIteractiveMode(ApplicationContext currentContext) {
        return currentContext.getProperty(AbstractIModeCommand.IMODE, Boolean.TYPE) != null;
    }

    private static void printJobsList(int index, int offset, ApplicationContext currentContext)
            throws PermissionRestException, NotConnectedRestException, IOException {
        RestMapPage<Long, ArrayList<UserJobData>> page = currentContext.getRestClient().getScheduler()
                .revisionAndJobsInfo(currentContext.getSessionId(), index, offset, false, true, true, true);
        Map<Long, ArrayList<UserJobData>> stateMap = page.getMap();
        List<UserJobData> jobs = stateMap.values().iterator().next();
        currentContext.getDevice().writeLine("%s", StringUtility.jobsAsString(jobs));
    }

    private static int getNumberOfJobs(int index, int offset, ApplicationContext currentContext)
            throws NotConnectedRestException, PermissionRestException {
        return currentContext.getRestClient().getScheduler()
                .jobs(currentContext.getSessionId(), index, offset).getList().size();
    }

    private static Integer valueAsInt(String nameValue) {
        return Integer.valueOf(nameValue.split("=")[1]);
    }

    private static int getNumOfJobsPerPage(ApplicationContext currentContext) {
        int columns = currentContext.getDevice().getWidth();
        int lines = currentContext.getDevice().getHeight();
        int numOfJobsPerPage = 0;
        if (lines > 0) {
            if (columns < 81) {
                /*
                 * 5 => 2 lines for table headers, 1 blank after, 1 for user
                 * opt, 1 blank before 2 => requires two lines per job
                 */
                numOfJobsPerPage = (lines - 5) / 2;
            } else {
                /*
                 * 4 => 1 lines for table headers, ...
                 */
                numOfJobsPerPage = lines - 4;
            }
        }
        if (numOfJobsPerPage < 1) {
            // if terminal height, width not available
            numOfJobsPerPage = RestConstants.DFLT_PAGINATION_SIZE;
        }
        return numOfJobsPerPage;
    }
}
