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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;


public class JobOutputAppender extends AppenderSkeleton {

    private JobOutput jobOutput = new JobOutput();

    public JobOutputAppender() throws NotConnectedException, UnknownJobException, PermissionException {
        this.name = "Appender for job output";

        this.setLayout(Log4JTaskLogs.getTaskLogLayout());
    }

    @Override
    protected void append(LoggingEvent event) {
        if (!super.closed) {
            jobOutput.log(this.layout.format(event));
        }
    }

    @Override
    public void close() {
        super.closed = true;
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public String fetchNewLogs() {
        return jobOutput.fetchNewLogs();
    }

    public String fetchAllLogs() {
        return jobOutput.fetchAllLogs();
    }

    public int size() {
        return jobOutput.size();
    }
}
