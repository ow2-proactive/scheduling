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
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.List;


/**
 * Improves performance of RandomAccessFile.readLine()
 *
 * @author http://www.javaworld.com/javaworld/javatips/jw-javatip26.html
 */
public class RandomAccessFileCustom extends Reader {

    /**
     * The underlying file reader.
     */
    private java.io.RandomAccessFile file;

    /**
     * Used to decode bytes into characters.
     */
    private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
    private final float averageCharsPerByte = decoder.averageCharsPerByte();

    /**
     * Contains buffered bytes before they are transformed into characters.
     */
    private ByteBuffer byteBuffer;

    /**
     * Contains buffered characters.
     */
    private CharBuffer charBuffer;

    /**
     * An array whose elements represent the number of bytes associated with
     * characters ready to be read from charBuffer.
     */
    private List<Integer> characterToBytes = new ArrayList<Integer>();

    /**
     * The size of the input buffer, in characters.
     */
    private int bufferSize;

    /**
     * Creates a random access file stream to read from, and optionally
     * to write to, a file with the specified name. A new
     * {@link FileDescriptor} object is created to represent the
     * connection to the file.
     *
     * <p> The <tt>mode</tt> argument specifies the access mode with which the
     * file is to be opened.  The permitted values and their meanings are as
     * specified for the <a
     * href="#mode"><tt>RandomAccessFile(File,String)</tt></a> constructor.
     *
     * <p>
     * If there is a security manager, its <code>checkRead</code> method
     * is called with the <code>name</code> argument
     * as its argument to see if read access to the file is allowed.
     * If the mode allows writing, the security manager's
     * <code>checkWrite</code> method
     * is also called with the <code>name</code> argument
     * as its argument to see if write access to the file is allowed.
     *
     *
     * @param name   the system-dependent filename
     * @param mode   the access <a href="#mode">mode</a>
     * @exception IllegalArgumentException  if the mode argument is not equal
     *               to one of <tt>"r"</tt>, <tt>"rw"</tt>, <tt>"rws"</tt>, or
     *               <tt>"rwd"</tt>
     * @exception FileNotFoundException
     *            if the mode is <tt>"r"</tt> but the given string does not
     *            denote an existing regular file, or if the mode begins with
     *            <tt>"rw"</tt> but the given string does not denote an
     *            existing, writable regular file and a new regular file of
     *            that name cannot be created, or if some other error occurs
     *            while opening or creating the file
     * @exception SecurityException         if a security manager exists and its
     *               <code>checkRead</code> method denies read access to the file
     *               or the mode is "rw" and the security manager's
     *               <code>checkWrite</code> method denies write access to the file
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager#checkRead(java.lang.String)
     * @see java.lang.SecurityManager#checkWrite(java.lang.String)
     * @revised 1.4
     * @spec JSR-51
     */
    public RandomAccessFileCustom(File file, String mode) throws FileNotFoundException {
        this.file = new java.io.RandomAccessFile(file, mode);
        setBufferSize(80);
    }

    /**
     * Creates a random access file stream to read from, and optionally
     * to write to, a file with the specified name. A new
     * {@link FileDescriptor} object is created to represent the
     * connection to the file.
     *
     * <p> The <tt>mode</tt> argument specifies the access mode with which the
     * file is to be opened.  The permitted values and their meanings are as
     * specified for the <a
     * href="#mode"><tt>RandomAccessFile(File,String)</tt></a> constructor.
     *
     * <p>
     * If there is a security manager, its <code>checkRead</code> method
     * is called with the <code>name</code> argument
     * as its argument to see if read access to the file is allowed.
     * If the mode allows writing, the security manager's
     * <code>checkWrite</code> method
     * is also called with the <code>name</code> argument
     * as its argument to see if write access to the file is allowed.
     *
     *
     * @param name   the system-dependent filename
     * @param mode   the access <a href="#mode">mode</a>
     * @param bufferSize the size of the input buffer
     * @exception IllegalArgumentException  if the mode argument is not equal
     *               to one of <tt>"r"</tt>, <tt>"rw"</tt>, <tt>"rws"</tt>, or
     *               <tt>"rwd"</tt>
     * @exception FileNotFoundException
     *            if the mode is <tt>"r"</tt> but the given string does not
     *            denote an existing regular file, or if the mode begins with
     *            <tt>"rw"</tt> but the given string does not denote an
     *            existing, writable regular file and a new regular file of
     *            that name cannot be created, or if some other error occurs
     *            while opening or creating the file
     * @exception SecurityException         if a security manager exists and its
     *               <code>checkRead</code> method denies read access to the file
     *               or the mode is "rw" and the security manager's
     *               <code>checkWrite</code> method denies write access to the file
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager#checkRead(java.lang.String)
     * @see java.lang.SecurityManager#checkWrite(java.lang.String)
     * @revised 1.4
     * @spec JSR-51
     */
    public RandomAccessFileCustom(File file, String mode, int bufferSize) throws IOException {
        this.file = new java.io.RandomAccessFile(file, mode);
        setBufferSize(bufferSize);
    }

    /**
     * Sets the buffer size.
     */
    private void setBufferSize(int length) {
        byteBuffer = ByteBuffer.allocate((int) (length / averageCharsPerByte));
        prepareReadBytes();
        charBuffer = CharBuffer.allocate(length);
        prepareReadChars();
    }

    /**
     * Prepares to write to the byte buffer.
     */
    private void prepareWriteBytes() {
        byteBuffer.compact();
    }

    /**
     * Prepare to read from the byte buffer.
     */
    private void prepareReadBytes() {
        byteBuffer.flip();
    }

    /**
     * Prepare to write to the character buffer.
     */
    private void prepareWriteChars() {
        characterToBytes.subList(0, charBuffer.position()).clear();
        charBuffer.compact();
    }

    /**
     * Prepare to read from the character buffer.
     */
    private void prepareReadChars() {
        charBuffer.flip();
    }

    /**
     * Fills the character buffer.
     *
     * @param length the maximum number of characters to decode
     * @return true on success
     */
    private boolean fillBuffer() throws IOException {
        byte[] buffer;
        int offset;
        int maxBytes;

        prepareWriteBytes();
        if (!byteBuffer.hasArray()) {
            buffer = new byte[(int) (bufferSize / averageCharsPerByte)];
            offset = 0;
            maxBytes = buffer.length;
        } else {
            // Suppress compiler errors by initializing the variables
            buffer = null;
            offset = -1;
            maxBytes = -1;
        }
        while (true) {
            if (byteBuffer.hasArray()) {
                buffer = byteBuffer.array();
                offset = byteBuffer.arrayOffset() + byteBuffer.position();
                maxBytes = byteBuffer.remaining();
            }
            int n = file.read(buffer, offset, maxBytes);
            if (n > 0) {
                if (!byteBuffer.hasArray()) {
                    byteBuffer.put(buffer, offset, n);
                } else {
                    byteBuffer.position(byteBuffer.position() + n);
                }
                prepareReadBytes();
                prepareWriteChars();
                do {
                    // Convert at most one character
                    charBuffer.limit(charBuffer.position() + 1);
                    int oldPos = charBuffer.position();
                    CoderResult result = decoder.decode(byteBuffer, charBuffer, true);
                    assert (!result.isError()) : result;
                    n = charBuffer.position() - oldPos;
                    if (n > 0) {
                        characterToBytes.add(n);
                    }
                } while ((n > 0) && (charBuffer.limit() < charBuffer.capacity()));
                prepareReadChars();
                if (charBuffer.remaining() <= 0) {
                    // Continue until we get at least a single character or EOF
                    continue;
                } else {
                    return true;
                }
            } else {
                prepareReadBytes();
                prepareWriteChars();
                int oldPos = charBuffer.position();
                CoderResult result = decoder.decode(byteBuffer, charBuffer, true);
                assert (!result.isError()) : result;
                n = charBuffer.position() - oldPos;
                if (n > 0) {
                    characterToBytes.add(n);
                }
                prepareReadChars();
                return n > 0;
            }
        }
    }

    @Override
    public int read() throws IOException {
        if (charBuffer.remaining() <= 0) {
            if (!fillBuffer()) {
                return -1;
            }
        }
        return charBuffer.get();
    }

    public int read(char[] cBuf, int off, int len) throws IOException {
        int index = 0;
        int result = 0;
        while (len > 0) {
            if (charBuffer.remaining() <= 0) {
                if (!fillBuffer()) {
                    if (result <= 0) {
                        return -1;
                    } else {
                        return result;
                    }
                }
            }
            int amount = Math.min(charBuffer.remaining(), len);
            charBuffer.get(cBuf, off, amount);
            off += amount;
            len -= amount;
            result += amount;
        }
        return result;
    }

    /**
     * Returns the current offset in this file.
     *
     * @return     the offset from the beginning of the file, in bytes,
     *             at which the next read or write occurs.
     * @exception  IOException  if an I/O error occurs.
     */
    public long getFilePointer() throws IOException {
        int charactersInBytes = 0;
        for (int i = charBuffer.position(); i < charBuffer.limit(); ++i)
            charactersInBytes += characterToBytes.get(i);
        return file.getFilePointer() - byteBuffer.remaining() - charactersInBytes;
    }

    /**
     * Sets the file-pointer offset, measured from the beginning of this
     * file, at which the next read or write occurs.  The offset may be
     * set beyond the end of the file. Setting the offset beyond the end
     * of the file does not change the file length.  The file length will
     * change only by writing after the offset has been set beyond the end
     * of the file.
     *
     * @param      pos   the offset position, measured in bytes from the
     *                   beginning of the file, at which to set the file
     *                   pointer.
     * @exception  IOException  if <code>pos</code> is less than
     *                          <code>0</code> or if an I/O error occurs.
     */
    public void seek(long pos) throws IOException {
        int offset = (int) (pos - getFilePointer());
        if (offset == 0) {
            return;
        }
        if (((offset > 0) && (offset <= charBuffer.remaining())) ||
            ((offset < 0) && (-offset <= charBuffer.position()))) {
            charBuffer.position(charBuffer.position() + offset);
        } else {
            byteBuffer.clear();
            byteBuffer.limit(0);
            charBuffer.clear();
            charBuffer.limit(0);
            characterToBytes.clear();
            file.seek(pos);
            decoder.reset();
        }
    }

    /**
     * Reads the next line of text from this file.  This method successively
     * reads bytes from the file, starting at the current file pointer,
     * until it reaches a line terminator or the end
     * of the file.  Each byte is converted into a character by taking the
     * byte's value for the lower eight bits of the character and setting the
     * high eight bits of the character to zero.  This method does not,
     * therefore, support the full Unicode character set.
     *
     * <p> A line of text is terminated by a carriage-return character
     * (<code>'\r'</code>), a newline character (<code>'\n'</code>), a
     * carriage-return character immediately followed by a newline character,
     * or the end of the file.  Line-terminating characters are discarded and
     * are not included as part of the string returned.
     *
     * <p> This method blocks until a newline character is read, a carriage
     * return and the byte following it are read (to see if it is a newline),
     * the end of the file is reached, or an exception is thrown.
     *
     * @return     the next line of text from this file, or null if end
     *             of file is encountered before even one byte is read.
     * @exception  IOException  if an I/O error occurs.
     */
    public String readLine() throws IOException {
        if ((charBuffer.remaining() <= 0) && !fillBuffer()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        while (true) {
            char ch = charBuffer.get();
            if (ch != '\n') {
                result.append(ch);
            } else {
                final int length = result.length();
                if ((length > 0) && (result.charAt(length - 1) == '\r')) {
                    result.delete(length - 1, length);
                }
                return result.toString();
            }
            if ((charBuffer.remaining() <= 0) && !fillBuffer()) {
                return result.toString();
            }
        }
    }

    /**
     * Returns the length of this file.
     *
     * @return     the length of this file, measured in bytes.
     * @exception  IOException  if an I/O error occurs.
     */
    public long length() throws IOException {
        return file.length();
    }

    public void close() throws IOException {
        file.close();
    }
}
