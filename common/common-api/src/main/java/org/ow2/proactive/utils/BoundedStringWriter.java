/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;


/**
 * BoundedStringWriter that keeps a limited amount (maxLength) of chars into
 * a sliding (elements are added and removed from the head) buffer and always prints them to an output sink.
 * It ensures the size is never greater than the maximum size given in the constructor.
 * Closing a <tt>BoundedStringWriter</tt> has no effect. The methods in this class
 * can be called after the stream has been closed without generating an
 * <tt>IOException</tt>.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.2
 */
public class BoundedStringWriter extends Writer {

    private final PrintStream outputSink;
    private final StringBuilder content;
    private final int maxSize;

    /**
     * Create a new bounded string writer using the default maximum size.
     */
    public BoundedStringWriter(PrintStream outputSink, final int maxSize) {
        this.outputSink = outputSink;
        this.maxSize = maxSize;
        this.content = new StringBuilder();
    }

    /** {@inheritDoc} */
    @Override
    public void write(char[] c, int off, int len) {
        int nextLen = this.content.length() + len;
        if (nextLen <= this.maxSize) {
            this.content.append(c, off, len);
        } else { // nextLen > this.maxSize    		
            if (len <= this.maxSize) {
                this.content.delete(0, nextLen - this.maxSize);
                this.content.append(c, off, len);
            } else {
                this.content.delete(0, this.maxSize);
                this.content.append(c, len - this.maxSize, this.maxSize);
            }
        }
        outputSink.print(new String(c, off, len));
    }

    /**
     * Return the string builder itself.
     *
     * @return StringBuilder holding the current buffer value.
     */
    public StringBuilder getBuilder() {
        return this.content;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.content.toString();
    }

    /**
     * Closing a <tt>BoundedStringWriter</tt> has no effect. The methods in this
     * class can be called after the stream has been closed without generating
     * an <tt>IOException</tt>.
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * Flush the stream.
     */
    @Override
    public void flush() throws IOException {
    }

}
