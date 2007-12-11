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
package org.objectweb.proactive.extensions.scheduler.util.logforwarder;

import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;


/**
 * This class defines a appender that bufferizes logs until a sink appender is given ; the buffer is
 * then flushed into the sink. This buffer can be keeped or deleted when a sink is activated (see keepBuffer).
 * @author cdelbe
 * @since 3.2.1
 */
public class BufferedAppender extends AppenderSkeleton {
    // TODO cdelbe : should implement AppenderAttachable (see AsyncAppender ?)

    /**
     * Default buffer size.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * By default, the buffer is deleted if sink is activated.
     */
    public static final boolean DEFAULT_KEEP_MODE = false;

    // logEvents buffer
    private transient LinkedList<LoggingEvent> buffer;

    // buffer size
    private int bufferSize;

    // number of fired events ; if nbFiredEvents>bufferSize,
    // buffer is reduced by the head
    private int nbFiredEvents = 0;

    // if true, log buffer is not deleted even if sink is activated
    private boolean keepBuffer;

    // sinks appender
    private final transient Vector<Appender> sinks;

    /**
     * Create a BufferAppender with default parameters.
     * @see BufferedAppender.DEFAULT_BUFFER_SIZE
     * @see BufferedAppender.DEFAULT_KEEP_MODE
     */
    public BufferedAppender() {
        this(null, DEFAULT_BUFFER_SIZE, DEFAULT_KEEP_MODE);
    }

    /**
     * Create a BufferAppender.
     * @param bufferSize the size of the buffer. If the buffer is full,
     * logs events are stored in FIFO manner.
     */
    public BufferedAppender(int bufferSize) {
        this(null, bufferSize, DEFAULT_KEEP_MODE);
    }

    /**
     * Create a BufferAppender.
     * @param keepBuffer it false, the buffer is deleted if the sink appender is
     * activated
     */
    public BufferedAppender(boolean keepBuffer) {
        this(null, DEFAULT_BUFFER_SIZE, keepBuffer);
    }

    /**
     * Create a BufferAppender.
     * @param name the name of the appender.
     * @param keepBuffer it false, the buffer is deleted if the sink appender is
     * activated
     */
    public BufferedAppender(String name, boolean keepBuffer) {
        this(name, DEFAULT_BUFFER_SIZE, keepBuffer);
    }

    /**
     * Create a BufferAppender.
     * @param name the name of the appender.
     * @param bufferSize the size of the buffer. If the buffer is full,
     * logs events are stored in FIFO manner.
     * @param keepBuffer it false, the buffer is deleted if the sink appender is
     * activated
     */
    public BufferedAppender(String name, int bufferSize, boolean keepBuffer) {
        this.name = name;
        this.bufferSize = (bufferSize == 0) ? Integer.MAX_VALUE : bufferSize;
        this.buffer = new LinkedList<LoggingEvent>();
        this.keepBuffer = keepBuffer;
        this.sinks = new Vector<Appender>();
    }

    /**
     * Activate the sink appender ; logs are append to the sink. If this.keepLogs
     * is false, the buffer is deleted.
     * @param sink the sink appender
     */
    public synchronized void addSink(Appender sink) {
        // flush the buffer into the sink
        for (LoggingEvent e : this.buffer) {
            sink.doAppend(e);
        }

        // add sink to the sinks
        this.sinks.add(sink);

        // still fill in buffer if required
        if (!this.keepBuffer) {
            this.buffer = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected synchronized void append(LoggingEvent event) {
        if (this.sinks.size() == 0) {
            // fill in buffer since no sink is activated
            this.fillInBuffer(event);
        } else {
            for (Appender s : this.sinks) {
                // send into sink
                s.doAppend(event);
            }

            // still fill in buffer if required
            if (this.keepBuffer) {
                this.fillInBuffer(event);
            }
        }
    }

    /**
     * Add event into the buffer. If the buffer is full, the first element is removed
     * (FIFO behavior)
     * @param event the log event to be added in the buffer.
     */
    private void fillInBuffer(LoggingEvent event) {
        assert (this.buffer.size() <= this.bufferSize);

        if (this.nbFiredEvents > this.bufferSize) {
            this.buffer.removeFirst();
        }

        this.buffer.addLast(event);
        this.nbFiredEvents++;
    }

    /**
     * Return a clone of the current logging event buffer.
     * @return a cloned linked list containing all logged events.
     */
    public synchronized LinkedList<LoggingEvent> getBuffer() {
        return (LinkedList<LoggingEvent>) this.buffer.clone();
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    @Override
    public synchronized void close() {
        if (!this.keepBuffer) {
            this.buffer = null;
        }

        if (this.sinks.size() != 0) {
            for (Appender s : this.sinks) {
                // close sink
                s.close();
            }
        }

        this.closed = true;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }
}
