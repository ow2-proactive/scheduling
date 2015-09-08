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
package org.ow2.proactive.scheduler.util;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.utils.appenders.FileAppender;


public class JobLogger {

    private static final String PREFIX = "job ";
    private static final JobLogger instance = new JobLogger();
    private Logger logger = Logger.getLogger(JobLogger.class);

    private JobLogger() {
    }

    public static JobLogger getInstance() {
        return instance;
    }

    public void info(JobId id, String message) {
        updateMdcWithTaskLogFilename(id);
        logger.info(PREFIX + id + " " + message);
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void debug(JobId id, String message) {
        updateMdcWithTaskLogFilename(id);
        logger.debug(PREFIX + id + " " + message);
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void warn(JobId id, String message) {
        updateMdcWithTaskLogFilename(id);
        logger.warn(PREFIX + id + " " + message);
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void error(JobId id, String message) {
        updateMdcWithTaskLogFilename(id);
        logger.error(PREFIX + id + " " + message);
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void error(JobId id, String message, Throwable th) {
        updateMdcWithTaskLogFilename(id);
        logger.error(PREFIX + id + " " + message, th);
        MDC.remove(FileAppender.FILE_NAME);
    }

    private void updateMdcWithTaskLogFilename(JobId id) {
        MDC.put(FileAppender.FILE_NAME, getJobLogFilename(id));
    }

    public static String getJobLogFilename(JobId id) {
        return id.value();
    }

}
