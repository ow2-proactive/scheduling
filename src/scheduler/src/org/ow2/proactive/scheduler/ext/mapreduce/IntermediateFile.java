package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.io.serializer.SerializationFactory;
import org.apache.hadoop.io.serializer.Serializer;

/**
 * The class {@link IntermediateFile} has the same behavior of the Hadoop
 * org.apache.hadoop.mapred.IFile class. This means some code was the result of
 * a copy and paste from the code of the hadoop class. But copy and paste is
 * necessary because the org.apache.hadoop.mapred.IFile class is not visible
 * outside its package (so that we cannot reuse it). This class contains a
 * writer to write the output of the MapperPATask in a specified format. This
 * class contains also the a reader that allow us to read the data written in
 * the IntermediateFile format.
 *
 * TODO in a first implementation we do not support neither compression neither
 * checksum. Neither we count the number of written bytes.
 *
 * @author The ProActive Team
 *
 */
public class IntermediateFile {

	/**
	 * Value to use to indicate the end of data in the intermediate file. The
	 * end will be represented by the sequence of two EOF_MARKER values (e.g.,
	 * -1-1).
	 */
	protected static final int EOF_MARKER = -1;

	/**
	 * The class {@link Writer} writes the MapperPATask output to intermediate
	 * files. Again some code is simply a copy and paste of the code that we can
	 * find inside the hadoop class IFile.Writer (that is not visible outside
	 * its package)
	 *
	 * @author The ProActive Team
	 *
	 * @param <K>
	 *            the class of the key to write
	 * @param <V>
	 *            the class of the value to write
	 */
	public static class Writer<K extends Object, V extends Object> {

		/**
		 * Indicate if this writer is the one that has created the
		 * FSDataOutputStream (and so the intermediate file)
		 */
		protected boolean outputStreamOwner = false;

		/**
		 * The counter for the number of bytes written
		 */
		protected long bytesWritten = 0;

		/**
		 * The counter for the number of records written
		 */
		protected long recordsWritten = 0;

		/**
		 * The implementation of the {@link java.io.DataOutputStream} to use to
		 * write data in the intermediate output file
		 */
		protected FSDataOutputStream fsDataOutputStream = null;

		/**
		 * The buffer to use to serialize keys and values
		 */
		protected DataOutputBuffer dataOutputBuffer = null;

		protected Class<K> keyClass = null;
		protected Class<V> valueClass = null;
		protected Serializer<K> keySerializer = null;
		protected Serializer<V> valueSerializer = null;

		public Writer(Configuration configuration, FileSystem fileSystem,
				Path filePath, Class<K> keyClass, Class<V> valueClass)
				throws IOException {
			this(configuration, fileSystem.create(filePath), keyClass,
					valueClass);
			outputStreamOwner = true;
		}

		public Writer(Configuration configuration,
				FSDataOutputStream fsDataOutputStream, Class<K> keyClass,
				Class<V> valueClass) throws IOException {
			this.fsDataOutputStream = fsDataOutputStream;
			this.keyClass = keyClass;
			this.valueClass = valueClass;
			dataOutputBuffer = new DataOutputBuffer();
			SerializationFactory serializationFactory = new SerializationFactory(
					configuration);
			keySerializer = serializationFactory.getSerializer(keyClass);
			keySerializer.open(dataOutputBuffer);
			valueSerializer = serializationFactory.getSerializer(valueClass);
			valueSerializer.open(dataOutputBuffer);
		}

		/**
		 * Close the writer
		 *
		 * @throws IOException
		 */
		public void close() throws IOException {
			// close the serializers
			keySerializer.close();
			valueSerializer.close();

			// Write EOF_MARKER for key/value length
			WritableUtils.writeVInt(fsDataOutputStream, EOF_MARKER);
			WritableUtils.writeVInt(fsDataOutputStream, EOF_MARKER);
			bytesWritten += 2 * WritableUtils.getVIntSize(EOF_MARKER);

			// Flush the stream
			fsDataOutputStream.flush();

			if (outputStreamOwner) {
				fsDataOutputStream.close();
			}
			fsDataOutputStream = null;

			recordsWritten++;
		}

		public void append(K key, V value) throws IOException {
			if (key.getClass() != keyClass)
				throw new IOException("wrong key class: " + key.getClass()
						+ " is not " + keyClass);
			if (value.getClass() != valueClass)
				throw new IOException("wrong value class: " + value.getClass()
						+ " is not " + valueClass);

			// Append the 'key'
			keySerializer.serialize(key);
			int keyLength = dataOutputBuffer.getLength();
			if (keyLength < 0) {
				throw new IOException("Negative key-length not allowed: "
						+ keyLength + " for " + key);
			}

			// Append the 'value'
			valueSerializer.serialize(value);
			int valueLength = dataOutputBuffer.getLength() - keyLength;
			if (valueLength < 0) {
				throw new IOException("Negative value-length not allowed: "
						+ valueLength + " for " + value);
			}

			/*
			 * Write the record out
			 * -----------------------------------------------------------
			 * |keyLength | valueLength | serializedKey | serializedValue|
			 * -----------------------------------------------------------
			 */
			WritableUtils.writeVInt(fsDataOutputStream, keyLength);
			WritableUtils.writeVInt(fsDataOutputStream, valueLength);
			fsDataOutputStream.write(dataOutputBuffer.getData(), 0,
					dataOutputBuffer.getLength());

			// Reset the buffer used to serialize keys and values
			dataOutputBuffer.reset();

			// Update bytes written
			bytesWritten += keyLength + valueLength
					+ WritableUtils.getVIntSize(keyLength)
					+ WritableUtils.getVIntSize(valueLength);
			++recordsWritten;
		}

		public void append(DataInputBuffer key, DataInputBuffer value)
				throws IOException {
			int keyLength = key.getLength() - key.getPosition();
			if (keyLength < 0) {
				throw new IOException("Negative key-length not allowed: "
						+ keyLength + " for " + key);
			}

			int valueLength = value.getLength() - value.getPosition();
			if (valueLength < 0) {
				throw new IOException("Negative value-length not allowed: "
						+ valueLength + " for " + value);
			}

			WritableUtils.writeVInt(fsDataOutputStream, keyLength);
			WritableUtils.writeVInt(fsDataOutputStream, valueLength);
			fsDataOutputStream.write(key.getData(), key.getPosition(),
					keyLength);
			fsDataOutputStream.write(value.getData(), value.getPosition(),
					valueLength);

			// Update bytes written
			bytesWritten += keyLength + valueLength
					+ WritableUtils.getVIntSize(keyLength)
					+ WritableUtils.getVIntSize(valueLength);
			++recordsWritten;
		}

		/**
		 * Return the number of bytes written by this writer
		 *
		 * @return the number of bytes written by this method
		 */
		public long getRawLength() {
			return bytesWritten;
		}

		/**
		 * We must notice we do not support compression so the value returned by
		 * this method is equal to the value returned by the method
		 * getrawLength()
		 *
		 * @return the number of bytes
		 *
		 *         TODO change this method when the Compression is supported by
		 *         the framework
		 */
		public long getCompressedLength() {
			return getRawLength();
		}
	}

	/**
	 * The class {@link Reader} reads data from intermediate files written using
	 * the corresponding {@link Writer}
	 *
	 * TODO We must notice that in a first implementation we do not provide
	 * support for compression, checksum and record counting
	 *
	 * @author The ProActive Team
	 *
	 * @param <K>
	 *            the class of the key to read
	 * @param <V>
	 *            the class of teh value to read
	 */
	public static class Reader<K extends Object, V extends Object> {

		/**
		 * The dafault size the buffer to be used to bufferize the data read
		 * from the input stream
		 */
		protected static final int DEFAULT_BUFFER_SIZE = 128 * 1024;

		/**
		 * The max length, in bytes, of the keyLength or valueLength field could
		 * have when reading a record for the input stream (so that the record
		 * is read as a stream of bytes)
		 */
		protected static final int MAX_VINT_SIZE = 9;

		/**
		 * The input stream to read data from. It actually is an instance of
		 * {@link FSDataInputStream}.
		 */
		protected InputStream inputStream = null;

		/**
		 * The actual buffer to use to bufferize when reading data from the
		 * input stream
		 */
		protected byte[] buffer = null;

		/**
		 * The size the buffer to be used to bufferize the data read from input
		 * stream must have
		 */
		protected int bufferSize = DEFAULT_BUFFER_SIZE;

		/**
		 * The length, in bytes, of the file to read data from
		 */
		protected long fileLength = 0;

		/**
		 * The wrapper of the buffer used to bufferize the data read from the
		 * input stream. The use of this wrapper saves some memory
		 *
		 * @see DataInputBuffer for more details
		 */
		protected DataInputBuffer dataInputBuffer = null;

		/**
		 * The counter for the number of bytes read
		 */
		protected long bytesRead = 0;

		/**
		 * The counter for the number of records read
		 */
		protected long numRecordsRead = 0;

		/**
		 * The identifier of the record read at a given point in the execution
		 */
		protected long recNo = 1;

		/**
		 * The boolean keep track of the fact a EOF is encountered (true) or not
		 * (false)
		 */
		protected boolean eof = false;

		public Reader(Configuration configuration, FileSystem fileSystem,
				Path filePath) throws IOException {
			this(configuration, fileSystem.open(filePath), fileSystem
					.getFileStatus(filePath).getLen());
		}

		public Reader(Configuration configuration,
				FSDataInputStream fsDataInputStream, long length) {
			inputStream = fsDataInputStream;
			if (configuration != null) {
				bufferSize = configuration
						.getInt(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE
								.getKey(), DEFAULT_BUFFER_SIZE);
			}
			fileLength = length;
			dataInputBuffer = new DataInputBuffer();
		}

		/**
		 * Read upto len bytes into buf starting at offset off.
		 *
		 * @param buf
		 *            buffer
		 * @param off
		 *            offset
		 * @param len
		 *            length of buffer
		 * @return the no. of bytes read
		 * @throws IOException
		 */
		private int readData(byte[] buf, int off, int len) throws IOException {
			int bytesRead = 0;
			while (bytesRead < len) {
				int n = inputStream.read(buf, off + bytesRead, len - bytesRead);
				if (n < 0) {
					return bytesRead;
				}
				bytesRead += n;
			}
			return len;
		}

		void readNextBlock(int minSize) throws IOException {
			if (buffer == null) {
				buffer = new byte[bufferSize];
				dataInputBuffer.reset(buffer, 0, 0);
			}
			buffer = rejigData(buffer,
					(bufferSize < minSize) ? new byte[minSize << 1] : buffer);
			bufferSize = buffer.length;
		}

		private byte[] rejigData(byte[] source, byte[] destination)
				throws IOException {
			// Copy remaining data into the destination array
			int bytesRemaining = dataInputBuffer.getLength()
					- dataInputBuffer.getPosition();
			if (bytesRemaining > 0) {
				System.arraycopy(source, dataInputBuffer.getPosition(),
						destination, 0, bytesRemaining);
			}

			// Read as much data as will fit from the underlying stream
			int n = readData(destination, bytesRemaining,
					(destination.length - bytesRemaining));
			dataInputBuffer.reset(destination, 0, (bytesRemaining + n));

			return destination;
		}

		public boolean next(DataInputBuffer key, DataInputBuffer value)
				throws IOException {
			// Sanity check
			if (eof) {
				throw new EOFException("Completed reading " + bytesRead);
			}

			// Check if we have enough data to read lengths
			if ((dataInputBuffer.getLength() - dataInputBuffer.getPosition()) < (2 * MAX_VINT_SIZE)) {
				readNextBlock(2 * MAX_VINT_SIZE);
			}

			// Read key and value lengths
			int oldPos = dataInputBuffer.getPosition();
			int keyLength = WritableUtils.readVInt(dataInputBuffer);
			int valueLength = WritableUtils.readVInt(dataInputBuffer);
			int pos = dataInputBuffer.getPosition();
			bytesRead += pos - oldPos;

			// Check for EOF
			if (keyLength == EOF_MARKER && valueLength == EOF_MARKER) {
				eof = true;
				return false;
			}

			// Sanity check
			if (keyLength < 0) {
				throw new IOException("Rec# " + recNo
						+ ": Negative key-length: " + keyLength);
			}
			if (valueLength < 0) {
				throw new IOException("Rec# " + recNo
						+ ": Negative value-length: " + valueLength);
			}

			final int recordLength = keyLength + valueLength;

			// Check if we have the raw key/value in the buffer
			if ((dataInputBuffer.getLength() - pos) < recordLength) {
				readNextBlock(recordLength);

				// Sanity check
				if ((dataInputBuffer.getLength() - dataInputBuffer
						.getPosition()) < recordLength) {
					throw new EOFException("Rec# " + recNo
							+ ": Could read the next " + " record");
				}
			}

			// Setup the key and value
			pos = dataInputBuffer.getPosition();
			byte[] data = dataInputBuffer.getData();
			key.reset(data, pos, keyLength);
			value.reset(data, (pos + keyLength), valueLength);

			// Position for the next record
			long skipped = dataInputBuffer.skip(recordLength);
			if (skipped != recordLength) {
				throw new IOException("Rec# " + recNo
						+ ": Failed to skip past record " + "of length: "
						+ recordLength);
			}

			// Record the bytes read
			bytesRead += recordLength;

			++recNo;
			++numRecordsRead;

			return true;
		}

		public void close() throws IOException {
			// Close the underlying stream
			inputStream.close();

			// Release the buffer
			dataInputBuffer = null;
			buffer = null;
		}

		public long getLength() {
			return fileLength;
		}

		public long getPosition() throws IOException {
			return ((FSDataInputStream) inputStream).getPos();
		}
	}

}
