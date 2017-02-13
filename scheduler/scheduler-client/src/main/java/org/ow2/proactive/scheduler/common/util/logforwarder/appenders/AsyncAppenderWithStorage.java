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
package org.ow2.proactive.scheduler.common.util.logforwarder.appenders;

import java.util.LinkedList;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.spi.LoggingEvent;


/**
 * Class defines a log4j AsyncAppender that is able to store all appended event additionally
 * to bufferize and send them to the added appenders.
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class AsyncAppenderWithStorage extends AsyncAppender {

    /**
     * Default buffer size. Infinite by default.
     */
    public static final int DEFAULT_STORAGE_SIZE = Integer.MAX_VALUE;

    // logEvents buffer
    private final transient LinkedList<LoggingEvent> storage;

    // buffer size
    private final int storageSize;

    // number of fired events ; if nbFiredEvents>bufferSize,
    // buffer is reduced by the head
    private long nbFiredEvents = 0;

    /**
     * Create a AsyncAppenderWithStorage with default parameters.
     * See DEFAULT_BUFFER_SIZE for details.
     */
    public AsyncAppenderWithStorage() {
        this(null, DEFAULT_STORAGE_SIZE);
    }

    /**
     * Create a AsyncAppenderWithStorage.
     * @param bufferSize the size of the buffer. If the buffer is full,
     * logs events are stored in FIFO manner.
     */
    public AsyncAppenderWithStorage(int bufferSize) {
        this(null, bufferSize);
    }

    /**
     * Create a AsyncAppenderWithStorage.
     * @param name the name of the appender.
     * activated
     */
    public AsyncAppenderWithStorage(String name) {
        this(name, DEFAULT_STORAGE_SIZE);
    }

    /**
     * Create a AsyncAppenderWithStorage.
     * @param name the name of the appender.
     * @param storageSize the size of the buffer. If the buffer is full,
     * logs events are stored in FIFO manner.
     * activated
     */
    public AsyncAppenderWithStorage(String name, int storageSize) {
        super();
        this.name = name;
        this.storageSize = storageSize;
        this.storage = new LinkedList<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public synchronized void append(LoggingEvent event) {
        super.append(event);
        this.fillInStorage(event);
    }

    /**
     * When an appender is added, then the content of the storage is append into
     * the added appender.
     * @see org.apache.log4j.AsyncAppender#addAppender(org.apache.log4j.Appender)
     */
    @Override
    public synchronized void addAppender(final Appender newAppender) {
        super.addAppender(newAppender);
        // flush the buffer into the sink
        appendStoredEvents(newAppender);
    }

    /**
     * Append stored events to the given appender.
     */
    public synchronized void appendStoredEvents(Appender appender) {
        for (LoggingEvent e : this.storage) {
            appender.doAppend(e);
        }
    }

    /**
     * Add event into the storage. If the storage is full, the first element is removed
     * (FIFO behavior)
     * @param event the log event to be added in the storage.
     */
    private void fillInStorage(LoggingEvent event) {
        if (this.storageSize > 0) {
            if (this.nbFiredEvents > this.storageSize) {
                this.storage.removeFirst();
            }
            this.storage.addLast(event);
        }
        this.nbFiredEvents++;
    }

    /**
     * Return a clone of the current logging event storage.
     * @return a cloned linked list containing all logged events.
     */
    @SuppressWarnings("unchecked")
    public synchronized LinkedList<LoggingEvent> getStorage() {
        return (LinkedList<LoggingEvent>) this.storage.clone();
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    @Override
    public synchronized void close() {
        super.close();
        this.closed = true;
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }

}
