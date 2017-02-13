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
package org.ow2.proactive.utils.appenders;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;


/**
 * 
 * An appender that redirects logging events to different files
 * depending on "filename" property in log4j context.
 * 
 * Is used to put server logs for tasks and jobs into files with 
 * different names.
 *
 */
public class FileAppender extends WriterAppender {

    public static final String FILE_NAME = "filename";

    private String maxFileSize;

    protected String filesLocation;

    public FileAppender() {

        setLayout(new PatternLayout("[%d{ISO8601} %-5p] %m%n"));

        // trying to get a layout from log4j configuration
        Enumeration<?> en = Logger.getRootLogger().getAllAppenders();
        if (en != null && en.hasMoreElements()) {
            Appender app = (Appender) en.nextElement();
            if (app != null && app.getLayout() != null) {
                Logger.getRootLogger().debug("Retrieved layout from log4j configuration");
                setLayout(app.getLayout());
            }
        }
    }

    @Override
    public void append(LoggingEvent event) {
        Object value = MDC.get(FILE_NAME);
        if (value != null) {
            append(value.toString(), event);
        }
    }

    public void append(String fileName, LoggingEvent event) {
        if (filesLocation != null) {
            fileName = filesLocation + File.separator + fileName;
        }
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                FileUtils.forceMkdirParent(file);
                FileUtils.touch(file);
            } catch (IOException e) {
                Logger.getRootLogger().error(e.getMessage(), e);
            }
        }

        try {
            RollingFileAppender appender = new RollingFileAppender(getLayout(), fileName, true);
            appender.setMaxBackupIndex(1);
            if (maxFileSize != null) {
                appender.setMaxFileSize(maxFileSize);
            }
            appender.append(event);
            appender.close();
        } catch (IOException e) {
            Logger.getRootLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public String getFilesLocation() {
        return filesLocation;
    }

    public void setFilesLocation(String filesLocation) {
        this.filesLocation = filesLocation;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}
