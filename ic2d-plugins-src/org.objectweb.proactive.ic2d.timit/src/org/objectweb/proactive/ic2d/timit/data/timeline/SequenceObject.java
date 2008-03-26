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
package org.objectweb.proactive.ic2d.timit.data.timeline;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;
import org.objectweb.proactive.ic2d.timit.data.timeline.utils.ReverseFileReader;
import org.objectweb.proactive.ic2d.timit.editparts.timeline.SequenceEditPart;


/**
 * Each Active object has its own sequence
 *
 * @author The ProActive Team
 */
public class SequenceObject implements NotificationListener {
    protected static final String DEFAULT_LOG_FILE_DIRECTORY = "TimItIC2D_Output";
    private TimeLineChartObject parent;
    private SequenceEditPart ep;
    private final Logger currentLogger;
    private boolean isRecording;
    protected final String name;
    protected final ObjectName objectName;
    private String outputFileName;
    private long firstTimeStampValue;
    protected long lastTimeStampValue;
    private Stamp currentServiceStamp;
    private Stamp currentWBNStamp;
    private FileAppender app;
    private ReverseFileReader reverseFileReader;

    public SequenceObject(String name, ObjectName objectName, TimeLineChartObject parent) {
        this.name = name;
        this.objectName = objectName;
        this.firstTimeStampValue = 0;
        this.lastTimeStampValue = 0;
        this.parent = parent;
        this.parent.addChild(this);
        // Create the logger
        this.currentLogger = Logger.getLogger(this.name);
        // Instantiate a layout and an appender, assign layout to appender
        // programmatically
        OutputFormatLayout outputFormatLayout = new OutputFormatLayout();
        try {
            app = new FileAppender(outputFormatLayout, DEFAULT_LOG_FILE_DIRECTORY + "/" + this.name, false);
            this.outputFileName = app.getFile();

            // Assign appender to the logger programmatically
            currentLogger.addAppender(app);
            currentLogger.setAdditivity(false); // avoid printing logs to console
            currentLogger.setLevel(Level.INFO); // only info level is used
            File f = new File(this.outputFileName);
            reverseFileReader = new ReverseFileReader(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles time stamps (in microseconds only)
     */
    public final void handleNotification(final Notification notifications, final Object handback) {
        if (!isRecording) {
            return;
        }
        Collection<Notification> notifCollections = (Collection<Notification>) notifications.getUserData();
        for (Notification notification : notifCollections) {
            String type = notification.getType();
            long notificationTimeStamp = notification.getTimeStamp();
            if (this.firstTimeStampValue == 0) {
                this.firstTimeStampValue = notificationTimeStamp;
            } else {
                this.lastTimeStampValue = (notificationTimeStamp - this.firstTimeStampValue);
            }

            // Service time stamp START
            if (type.equals(NotificationType.servingStarted)) {
                if (this.currentServiceStamp == null) {
                    this.currentServiceStamp = new Stamp();
                }
                this.currentServiceStamp.state = State.SERVING_REQUEST;
                this.currentServiceStamp.startTime = this.lastTimeStampValue;
                // Service time stamp STOP
            } else if (type.equals(NotificationType.voidRequestServed) ||
                type.equals(NotificationType.replySent)) {
                if (this.currentServiceStamp != null) {
                    this.currentServiceStamp.endTime = this.lastTimeStampValue;
                    this.currentLogger.info(this.currentServiceStamp);
                }

                // WaitByNecessity time stamp START 
            } else if (type.equals(NotificationType.waitByNecessity)) {
                if (this.currentWBNStamp == null) {
                    this.currentWBNStamp = new Stamp();
                }
                this.currentWBNStamp.state = State.WAITING_BY_NECESSITY;
                this.currentWBNStamp.startTime = this.lastTimeStampValue;
                // WaitByNecessity time stamp STOP
            } else if (type.equals(NotificationType.receivedFutureResult)) {
                if (this.currentWBNStamp != null) {
                    this.currentWBNStamp.endTime = this.lastTimeStampValue;
                    this.currentLogger.info(this.currentWBNStamp);
                }
            }
        }
    }

    public final void startRecord() {
        this.isRecording = true;
    }

    public final void stopRecord() {
        this.isRecording = false;
        this.currentServiceStamp = null;
        this.currentWBNStamp = null;
        try {
            this.reverseFileReader.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void rewind() {
        this.reverseFileReader.rewind();
    }

    public void clear() {
        this.isRecording = false;
        this.currentServiceStamp = null;
        this.currentWBNStamp = null;
        // Close the appender, remove it from logger and close the reader
        this.app.close();
        this.currentLogger.removeAllAppenders();
        try {
            this.reverseFileReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public final Stamp getNextLoggedStampReversed() {
        if (this.isRecording) {
            return null;
        }
        try {
            String line = reverseFileReader.readLine();

            if (line == null) {
                this.reverseFileReader.rewind();
                return null;
            }

            String[] splittedLine = line.split(" ");
            int stateOrdinal = Integer.parseInt(splittedLine[0]);
            long startTime = Long.parseLong(splittedLine[1]);
            long stopTime = Long.parseLong(splittedLine[2]);
            Stamp s = new Stamp(State.values()[stateOrdinal], startTime);
            s.endTime = stopTime;
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SequenceEditPart getEp() {
        return ep;
    }

    public void setEp(SequenceEditPart ep) {
        this.ep = ep;
    }

    public class OutputFormatLayout extends Layout {
        StringBuilder sbuf = new StringBuilder(128);

        @Override
        public final void activateOptions() {
        }

        @Override
        public final String format(LoggingEvent event) {
            sbuf.setLength(0);
            return sbuf.append(event.getRenderedMessage()).append(LINE_SEP).toString();
        }

        @Override
        public final boolean ignoresThrowable() {
            return true;
        }
    }
}
