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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;


public class ReSubmitJobCommand extends AbstractCommand implements Command {

    private final String jobId;

    private String variables;

    private String genericInfos;

    public ReSubmitJobCommand(String... params) throws NullPointerException {
        Objects.requireNonNull(params);

        this.jobId = params[0];
        if (params.length > 1) {
            this.variables = params[1];
        }
        if (params.length > 2) {
            this.genericInfos = params[2];
        }
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        try {
            JobIdData newJobId = currentContext.getRestClient()
                                               .reSubmit(currentContext.getSessionId(),
                                                         jobId,
                                                         JobKeyValueTransformer.transformJsonStringToMap(variables),
                                                         JobKeyValueTransformer.transformJsonStringToMap(genericInfos));
            writeLine(currentContext, "Job('%s') successfully re-submitted as Job('%d')", jobId, newJobId.getId());
            resultStack(currentContext).push(jobId);
        } catch (Exception e) {
            handleError(String.format("An error occurred while re-submitting Job('%s') output %s:",
                                      jobId,
                                      e.getMessage()),
                        e,
                        currentContext);

        }

    }

}
