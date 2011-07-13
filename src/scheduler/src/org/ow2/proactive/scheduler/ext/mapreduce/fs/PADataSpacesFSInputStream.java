package org.ow2.proactive.scheduler.ext.mapreduce.fs;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.fs.BufferedFSInputStream;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSInputStream;
import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.Seekable;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.FileContent;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;


/**
 * The {@link PADataSpacesFSInputStream} is an adapter of the
 * {@link InputStream} we can retrieve from the
 * {@link FileContent#getInputStream()} where the FileContent is obtained from
 * the {@link DataSpacesFileObject#getContent()} method invocation. The
 * {@link PADataSpacesFSInputStream} class extends the {@link FSInputStream}
 * one. This means that when in the method
 * {@link PADataSpacesFileSystem#open(org.apache.hadoop.fs.Path, Integer)} we
 * need to create a {@link BufferedFSInputStream} we can use the
 * {@link PADataSpacesFSInputStream} instance. And we can use an instance of the
 * {@link PADataSpacesFSInputStream} also when we need to create a
 * {@link FSDataInputStream} via the constructor
 * {@link FSDataInputStream#FSDataInputStream(InputStream)} without having
 * problems due to the fact that that constructor even if declare to need an
 * {@link InputStream} as parameter, actually needs an InputStream that
 * implements the {@link Seekable} and {@link PositionedReadable} interfaces.
 * Letting the {@link PADataSpacesFSInputStream} extends the
 * {@link FSInputStream} we implicitly implement those two required interfaces.
 *
 * @author The ProActive Team
 *
 */
public class PADataSpacesFSInputStream extends FSInputStream {

    /**
     * the {@link DataSpacesFileObject} this stream is built from
     */
    protected DataSpacesFileObject dataSpacesFileObject = null;

    /**
     * the {@link InputStream} retrieved from the {@link DataSpacesFileObject}
     */
    protected InputStream inputStream = null;

    /**
     * the size of the {@link DataSpacesFileObject} this {@link InputStream} is
     * built from
     */
    protected long fileLength = -1;

    /**
     * the current position of this {@link InputStream}
     */
    protected long pos = 0;

    /**
     * if this stream is closed or not
     */
    protected boolean closed = false;

    public PADataSpacesFSInputStream() {
    }

    public PADataSpacesFSInputStream(DataSpacesFileObject dataSpacesFileObject) throws FileSystemException {
        this.dataSpacesFileObject = dataSpacesFileObject;
        inputStream = this.dataSpacesFileObject.getContent().getInputStream();
        fileLength = this.dataSpacesFileObject.getContent().getSize();
    }

    @Override
    public long getPos() throws IOException {
        return pos;
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public void seek(long targetPos) throws IOException {
        if (targetPos > fileLength) {
            throw new IOException("Cannot seek after EOF");
        }
        pos = targetPos;
        inputStream.skip(targetPos);
    }

    @Override
    public boolean seekToNewSource(long targetPos) throws IOException {
        return false;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public int read() throws IOException {
        int byteValue = inputStream.read();
        if (byteValue > 0) {
            pos++;
        }
        return byteValue;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int numberOfReadBytes = inputStream.read(b, off, len);
        if (numberOfReadBytes > 0) {
            pos += numberOfReadBytes;
        }
        return numberOfReadBytes;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public void close() throws IOException {
        dataSpacesFileObject.close();
        inputStream.close();
        closed = true;
    }
}
