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
package org.ow2.proactive.scheduler.common.util.logforwarder.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * An OutputStream that flushes out to a Category.
 * @author Jim Moore
 * @see Category
 */
public class LoggingOutputStream extends OutputStream {

    /**
     * System dependent line return char(s).
     */
    static final byte[] lineSepBytes = System.lineSeparator().getBytes();

    /**
     * The default number of bytes in the buffer. =2048
     */
    public static final int DEFAULT_BUFFER_LENGTH = 2048;

    /**
     * Used to maintain the contract of #close().
     */
    protected boolean hasBeenClosed = false;

    /**
     * The internal buffer where data is stored.
     */
    protected byte[] buf;

    /**
     * The number of valid bytes in the buffer. This value is always
     * in the range <tt>0</tt> through <tt>buf.length</tt>; elements
     * <tt>buf[0]</tt> through <tt>buf[count-1]</tt> contain valid
     * byte data.
     */
    protected int count;

    /**
     * Remembers the size of the buffer for speed.
     */
    private int bufLength;

    /**
     * The category to write to.
     */
    protected Logger logger;

    /**
     * The priority to use when writing to the Category.
     */
    protected Level level;

    private LoggingOutputStream() {
        // illegal
    }

    /**
     * Creates the LoggingOutputStream to flush to the given Category.
     * @param log      the Logger to write to
     * @param level the Level to use when writing to the Logger
     * @throws IllegalArgumentException if cat == null or priority == null
     */
    public LoggingOutputStream(Logger log, Level level) throws IllegalArgumentException {
        if (log == null) {
            throw new IllegalArgumentException("cat == null");
        }

        if (level == null) {
            throw new IllegalArgumentException("priority == null");
        }

        this.level = level;
        logger = log;
        bufLength = DEFAULT_BUFFER_LENGTH;
        buf = new byte[DEFAULT_BUFFER_LENGTH];
        count = 0;
    }

    public LoggingOutputStream(Logger log) throws IllegalArgumentException {
        this(log, Level.ALL);
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream. The general contract of
     * <code>close</code>
     * is that it closes the output stream. A closed stream cannot perform
     * output operations and cannot be reopened.
     */
    @Override
    public void close() {
        this.flush(false);
        this.hasBeenClosed = true;
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     *
     * @param b the <code>byte</code> to write
     * @throws IOException if an I/O error occurs. In particular,
     * an <code>IOException</code> may be thrown if the
     * output stream has been closed.
     */
    @Override
    public void write(final int b) throws IOException {
        if (hasBeenClosed) {
            throw new IOException("The stream has been closed.");
        }

        // would this be writing past the buffer?
        if (count == bufLength) {
            // grow the buffer
            final int newBufLength = bufLength + DEFAULT_BUFFER_LENGTH;
            final byte[] newBuf = new byte[newBufLength];

            System.arraycopy(buf, 0, newBuf, 0, bufLength);

            buf = newBuf;
            bufLength = newBufLength;
        }

        buf[count] = (byte) b;
        count++;
    }

    private void flush(boolean waitForNewLine) {
        if (this.count != 0) {
            if (this.count == lineSepBytes.length) {
                boolean isLineSep = checkBufferIsLineSeparator();

                if (isLineSep) {
                    this.reset();
                    return;
                }
            }

            byte[] theBytes = new byte[this.count];
            System.arraycopy(this.buf, 0, theBytes, 0, this.count);
            if (waitForNewLine) {
                for (int c = 0; c < lineSepBytes.length; ++c) {
                    if (this.count < lineSepBytes.length ||
                        theBytes[theBytes.length - lineSepBytes.length + c] != lineSepBytes[c]) {
                        return;
                    }
                }
            }

            this.logger.log(this.level, new String(theBytes));
            this.reset();
        }
    }

    private boolean checkBufferIsLineSeparator() {
        for (int c = 0; c < lineSepBytes.length; ++c) {
            if (this.buf[c] != lineSepBytes[c]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out. The general contract of <code>flush</code> is
     * that calling it is an indication that, if any bytes previously
     * written have been buffered by the implementation of the output
     * stream, such bytes should immediately be written to their
     * intended destination.
     */
    @Override
    public void flush() {
        flush(true);
    }

    private void reset() {
        // not resetting the buffer -- assuming that if it grew then it
        //   will likely grow similarly again
        count = 0;
    }
}
