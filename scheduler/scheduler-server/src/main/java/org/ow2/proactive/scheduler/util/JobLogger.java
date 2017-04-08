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
        MDC.put(FileAppender.FILE_NAME, getJobLogRelativePath(id));
    }

    public static String getJobLogRelativePath(JobId id) {
        return id.value() + "/" + id.value();
    }

}
