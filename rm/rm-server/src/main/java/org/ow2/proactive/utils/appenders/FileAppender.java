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
package org.ow2.proactive.utils.appenders;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


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
            e.printStackTrace();
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
