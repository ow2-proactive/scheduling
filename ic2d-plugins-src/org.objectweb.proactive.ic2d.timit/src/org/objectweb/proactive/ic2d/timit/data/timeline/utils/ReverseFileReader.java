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
package org.objectweb.proactive.ic2d.timit.data.timeline.utils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;


/**
 * Reads a text file backwards, relative to the left of the file pointer
 * instead of to the right of it.
 *
 * Lines are read from bottom to top. Character buffers are read right to left.
 * The returned text reads correctly from left-to-right (i.e. it is
 * not reversed) but the file pointer keeps on moving up the file instead of
 * down it.
 *
 * @author Gili Tzabari
 */
public class ReverseFileReader extends Reader {

    /**
     * Maps a line number to the file offset of the beginning of that line.
     */
    private Map<Integer, Long> lineToPosition;

    /**
     * The file to read from.
     */
    private final RandomAccessFileCustom file;

    /**
     * The current line number.
     */
    private int line;
    private int maxLine;

    /**
     * Creates a new ReverseFileReader.
     */
    public ReverseFileReader(File file) throws IOException {
        this.file = new RandomAccessFileCustom(file, "r", 250);
    }

    /**
     * Initializes lineToPosition.
     */
    public final void init() throws IOException {
        if (lineToPosition == null) {
            lineToPosition = new HashMap<Integer, Long>();
            lineToPosition.put(0, 0L);
        }
        line = 0;

        while (file.readLine() != null)
            lineToPosition.put(++line, file.getFilePointer());

        lineToPosition.remove(line);
        --line;
        this.maxLine = line;
    }

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return     A String containing the contents of the line, not including
     *             any line-termination characters, or null if the end of the
     *             stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    public final String readLine() throws IOException {
        if (lineToPosition == null) {
            init();
        }
        final Long index = lineToPosition.get(line);
        if (index == null) {
            return null;
        } else {
            file.seek(index);
            --line;
            assert (line >= -1) : line;
            return file.readLine();
        }
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        final long pos = file.getFilePointer();
        if (len > pos) {
            len = (int) pos;
        }
        file.seek(pos - len);
        int result = file.read(cbuf, off, len);
        file.seek(pos - len);
        return result;
    }

    public void close() throws IOException {
        file.close();
    }

    public void rewind() {
        this.line = this.maxLine;
    }

    public void rewind(int newLine) {
        this.line = newLine;
    }

    public int getMaxLine() {
        return this.maxLine;
    }

    public int getLine() {
        return this.line;
    }
}
