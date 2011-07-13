package org.ow2.proactive.scheduler.ext.mapreduce.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

	static final int BUFFER_SIZE = 100000;
	static final byte[] buffer = new byte[BUFFER_SIZE];

	/**
	 * Copy the specified amount of bytes from the input stream to the output one
	 * @param inputStream the input stream from which the bytes must be copied
	 * @param outputStream the output stream to which the bytes must be written
	 * @param numberOfBytesToRead the amount of bytes to copy. It represents an upper limit
	 * 		of the amount of bytes to copy. It means that the amount of bytes to copy depends
	 * 		on the amount of bytes available
	 * @return long the number of bytes copied from the input stream to the output one
	 */
	public static long copy(InputStream inputStream, OutputStream outputStream, long numberOfBytesToRead) {
		long numberOfCopiedBytes = 0;
		long numberOfReadBytes = 0;
		long numberOfBytesToCopy = 0;
		boolean ended = false;
		while (!ended) {
			synchronized (buffer) {
				try {
					numberOfReadBytes = inputStream.read(buffer);
					if ( (numberOfReadBytes > 0) && (numberOfBytesToRead >= 0)) {
						numberOfBytesToCopy = Math.min( (int) numberOfReadBytes, (int) numberOfBytesToRead);
						outputStream.write(buffer, 0, (int) numberOfBytesToCopy);
						numberOfBytesToRead -= numberOfReadBytes;
						numberOfCopiedBytes += numberOfBytesToCopy;
					} else {
						ended = true;
					}
				} catch (IOException ioe) {
					// thrown by "inputStream.read(buffer);"
					// thrown by "outputStream.write(buffer, 0, numberOfReadBytes);"
					ioe.printStackTrace();
				}
			}
		}

		return numberOfCopiedBytes;
	}
}
