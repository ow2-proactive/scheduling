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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


public abstract class FileAppender extends WriterAppender {

    public static final String FILE_NAME = "filename";

    private String maxFileSize;

    private String filesLocation;

    private static EnhancedPatternLayout configuredLayout = null;

    public FileAppender() {
        if (configuredLayout == null) {
            configuredLayout = new EnhancedPatternLayout(PAResourceManagerProperties.LO4J_FILE_APPENDER_PATTERN.getValueAsString());
        }
        setLayout(configuredLayout);
    }

    @Override
    public void append(LoggingEvent event) {
        Object value = MDC.get(FILE_NAME);
        if (value != null) {
            append(value.toString(), event);
        }
    }

    abstract public void append(String cacheKey, LoggingEvent event);

    RollingFileAppender createAppender(String cacheKey) {
        RollingFileAppender appender;
        String fileName = cacheKey;
        if (filesLocation != null) {
            fileName = filesLocation + File.separator + fileName;
        }
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                FileUtils.forceMkdirParent(file);
                FileUtils.touch(file);
            }

            appender = new RollingFileAppender(getLayout(), fileName, true);
            appender.setMaxBackupIndex(1);
            appender.setImmediateFlush(true);
            if (maxFileSize != null) {
                appender.setMaxFileSize(maxFileSize);
            }
        } catch (Exception e) {
            Logger.getRootLogger()
                  .error("Error when creating logger : " + cacheKey + " logging will be disabled for this context", e);
            return null;
        }
        return appender;
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public void setMaxFileSize(String valueAsString) {
        this.maxFileSize = valueAsString;
    }

    public void setFilesLocation(String logsLocation) {
        this.filesLocation = logsLocation;
    }
}
