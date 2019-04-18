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
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;


public abstract class FileAppender extends WriterAppender {

    public static final String FILE_NAME = "filename";

    private String maxFileSize;

    private String filesLocation;

    public FileAppender() {
        setLayout(new PatternLayout("[%d{ISO8601} %-5p] %m%n"));
        fetchLayoutFromRootLogger();
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
            //            appender = new RollingFileAppender();
            return null;

        }
        return appender;
    }

    private void fetchLayoutFromRootLogger() {
        // trying to get a layout from log4j configuration
        Enumeration<?> en = Logger.getRootLogger().getAllAppenders();
        if (en != null && en.hasMoreElements()) {
            Appender app = (Appender) en.nextElement();
            if (app != null && app.getLayout() != null) {
                if (app instanceof AsyncAppender) {
                    Enumeration<?> attachedAppenders = ((AsyncAppender) app).getAllAppenders();
                    if (attachedAppenders != null && attachedAppenders.hasMoreElements()) {
                        Appender attachedApp = (Appender) attachedAppenders.nextElement();
                        setLayoutUsingAppender(attachedApp);
                    }
                } else {
                    setLayoutUsingAppender(app);
                }
            }
        }
    }

    private void setLayoutUsingAppender(Appender attachedApp) {
        final Layout layout = attachedApp.getLayout();
        if (layout instanceof PatternLayout) {
            Logger.getRootLogger().trace("Retrieved layout from log4j configuration");
            PatternLayout paternLayout = (PatternLayout) layout;
            setLayout(new PatternLayout(paternLayout.getConversionPattern()));
        }
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
