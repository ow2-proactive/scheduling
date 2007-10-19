/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.logforwarder;

import java.util.LinkedList;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.net.SocketAppender;
import org.apache.log4j.spi.LoggingEvent;


/**
 * This class defines a appender that bufferizes logs until a sink appender is given ; the buffer is
 * then flushed into the sink. This buffer can be keeped or deleted when sink is activated (see keepLogs).
 * @author cdelbe
 * @since 3.2.1
 */
public class BufferedAppender extends AppenderSkeleton {

    /**
     * Default buffer size.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * Default appender name.
     * @see AppenderSkeleton.getName()
     */
    public static final String DEFAULT_NAME = "BufferSocketAppender";

    /**
     * By default, the buffer is deleted if sink is activated.
     */
    public static final boolean DEFAULT_KEEP_MODE = false;

    // logEvents buffer
    private LinkedList<LoggingEvent> buffer;

    // buffer size
    private int bufferSize;

    // number of fired events ; if nbFiredEvents>bufferSize,
    // buffer is reduced by the head
    private int nbFiredEvents = 0;

    // if true, log buffer is not deleted even if sink is activated
    private boolean keepLogs;

    // sink appender
    private Appender sink;

    /**
     * Create a BufferAppender with default parameters.
     * @see BufferedAppender.DEFAULT_NAME
     * @see BufferedAppender.DEFAULT_BUFFER_SIZE
     * @see BufferedAppender.DEFAULT_KEEP_MODE
     */
    public BufferedAppender() {
        this(DEFAULT_NAME, DEFAULT_BUFFER_SIZE, DEFAULT_KEEP_MODE);
    }

    /**
     * Create a BufferAppender.
     * @param bufferSize the size of the buffer. If the buffer is full,
     * logs events are stored in FIFO manner.
     */
    public BufferedAppender(int bufferSize) {
        this(DEFAULT_NAME, bufferSize, DEFAULT_KEEP_MODE);
    }

    /**
     * Create a BufferAppender.
     * @param name the name of the appender.
     * @see @see AppenderSkeleton.getName()
     */
    public BufferedAppender(String name) {
        this(name, DEFAULT_BUFFER_SIZE, DEFAULT_KEEP_MODE);
    }

    /**
     * Create a BufferAppender.
     * @param name the name of the appender.
     * @param bufferSize the size of the buffer. If the buffer is full,
     * logs events are stored in FIFO manner.
     * @param keepLogs it false, the buffer is deleted if the sink appender is
     * activated
     */
    public BufferedAppender(String name, int bufferSize, boolean keepLogs) {
        this.name = name;
        this.bufferSize = (bufferSize == 0) ? Integer.MAX_VALUE : bufferSize;
        this.buffer = new LinkedList<LoggingEvent>();
        this.keepLogs = keepLogs;
    }

    /**
     * Activate the sink appender ; logs are append to the sink. If this.keepLogs
     * is false, the buffer is deleted.
     * @param sink the sink appender
     */
    public synchronized void activateSink(Appender sink) {
        // activate only if not activated...
        if (this.sink == null) {
            // create socket
            this.sink = sink;
            if (this.nbFiredEvents > this.bufferSize) { // ADD SPECIFIC HEADER FOR CUT LOGS ??
                System.out.println(
                    "::::::::::::::::::::::::::::::::::::::: SIZE EXCEED ::::::::::::::::::::::::::::::::::::::::::::::");
            }

            // flush the buffer into the sink
            for (LoggingEvent e : this.buffer) {
                this.sink.doAppend(e);
            }
            if (!this.keepLogs) {
                // delete buffer
                this.buffer = null;
            }
        }
    }

    /**
     * Return all the logs buffered in this BufferedAppender, null if the buffer has been deleted.
     * @return all the logs buffered in this BufferedAppender, null if the buffer has been deleted.
     */
    public synchronized StringBuffer getAllOutput() {
        if (this.buffer != null) {
            StringBuffer logs = new StringBuffer(this.buffer.size());
            for (LoggingEvent e : this.buffer) {
                logs.append(e.getMessage());
            }
            return logs;
        } else {
            return null;
        }
    }

    /**
     * Flush all the logs buffered in the appender app.
     * @param app the appender to flush into.
     */
    public synchronized void flushBufferInto(Appender app) {
        if (this.buffer != null) {
            // flush the buffer into the appender app
            for (LoggingEvent e : this.buffer) {
                app.doAppend(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected synchronized void append(LoggingEvent event) {
        if (this.sink == null) {
            // fill in buffer since sink is not activated
            this.fillInBuffer(event);
        } else {
            // send into sink
            this.sink.doAppend(event);
            // still fill in buffer if required
            if (this.keepLogs) {
                this.fillInBuffer(event);
            }
        }
    }

    /**
     * Add event into the buffer. If the buffer is full, the first element is removed
     * (FIFO behavior)
     * @param event the log evenet to be added in the buffer.
     */
    private void fillInBuffer(LoggingEvent event) {
        assert (this.buffer.size() <= this.bufferSize);

        if (this.nbFiredEvents > this.bufferSize) {
            this.buffer.removeFirst();
        }
        this.buffer.addLast(event);
        this.nbFiredEvents++;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    @Override
    public synchronized void close() {
        this.buffer = null;
        if (this.sink != null) {
            this.sink.close();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }
}
