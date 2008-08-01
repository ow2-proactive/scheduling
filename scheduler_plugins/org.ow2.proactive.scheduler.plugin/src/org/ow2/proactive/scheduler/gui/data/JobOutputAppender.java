/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.scheduler.gui.data;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.scheduler.gui.views.JobOutput;


/**
 * A job output appender
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobOutputAppender extends AppenderSkeleton {
    private JobOutput jobOutput = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor
     *
     * @param jobOutput the job output
     */
    public JobOutputAppender(JobOutput jobOutput) {
        this.jobOutput = jobOutput;
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

    // -------------------------------------------------------------------- //
    // -------------------- extends AppenderSkeleton ---------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected void append(LoggingEvent event) {
        String msg = null;
        if (this.layout != null) {
            msg = this.layout.format(event);
        } else {
            msg = event.getRenderedMessage();
        }
        if (event.getLevel().equals(Level.DEBUG)) {
            jobOutput.debug(msg);
        } else if (event.getLevel().equals(Level.ERROR)) {
            jobOutput.error(msg);
        } else if (event.getLevel().equals(Level.FATAL)) {
            jobOutput.fatal(msg);
        } else if (event.getLevel().equals(Level.INFO)) {
            jobOutput.info(msg);
        } else if (event.getLevel().equals(Level.OFF)) {
            jobOutput.off();
        } else if (event.getLevel().equals(Level.TRACE)) {
            jobOutput.trace(msg);
        } else if (event.getLevel().equals(Level.WARN)) {
            jobOutput.warn(msg);
        } else {
            jobOutput.log(msg);
        }
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    @Override
    public void close() {
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }
}
