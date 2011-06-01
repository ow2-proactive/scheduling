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
package org.ow2.proactive_grid_cloud_portal;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;


/**
 * A job output appender
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobOutputAppender extends AppenderSkeleton {

    private JobOutput jobOutput = null;
    private SchedulerSession ss;
    private String jobId;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor
     *
     * @param jobOutput the job output
     * @throws PermissionException 
     * @throws UnknownJobException 
     * @throws NotConnectedException 
     */
    public JobOutputAppender(SchedulerSession ss, String jobId, AppenderProvider ap, JobOutput jobOutput) throws NotConnectedException, UnknownJobException, PermissionException {
        this.name = "Appender for job output";
        this.ss = ss;
        this.jobOutput = jobOutput;
        this.jobId =  jobId;
        
        this.setLayout(Log4JTaskLogs.getTaskLogLayout());
        Logger log = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + this.jobId);
        log.setAdditivity(false);
        log.setLevel(Level.ALL);
        log.addAppender(this);
        ss.setJobOutputAppender(this);
        ss.getScheduler().listenJobLogs(jobId, ap);
        
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * To obtains the job output
     *
     * @return the job output
     */
    public JobOutput getJobOutput() {
        return jobOutput;
    }

    public void terminate () {
        close();
        Logger log = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + jobId);
        log.removeAppender(this);
    }
    
    // -------------------------------------------------------------------- //
    // -------------------- extends AppenderSkeleton ---------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected void append(LoggingEvent event) {
        if (!super.closed) {
            jobOutput.getCl().add(
                    this.layout != null ? this.layout.format(event) : event.getRenderedMessage());
        }
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    @Override
    public void close() {
        super.closed = true;
        jobOutput = null;
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }

    public String getJobId() {
        return jobId;
    }
}
