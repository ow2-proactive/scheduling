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
package org.objectweb.proactive.extra.scheduler.common.job;

import org.apache.log4j.Appender;


/**
 * This interface mst be implemented by log storage for scheduler jobs.
 * @author ProActive Team
 * @version 1.0
 * @since ProActive 3.2.1
 */
public interface JobLogs extends java.io.Serializable {

    /** Prefix for job logger */
    public static final String JOB_LOGGER_PREFIX = "logger.scheduler.";

    /** Appender name for jobs */
    public static final String JOB_APPENDER_NAME = "JobLoggerAppender";

    /**
     * Add a sink to the logs. Logs are then redirected into the sink.
     * @param sink the appender to write into.
     */
    public void addSink(Appender sink);

    /**
     * Return the currently stored logs, or null if any.
     * @return a StringBuffer containing stored logs, or null if any.
     */
    public StringBuffer getAllLogs();
}
