package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ChecksumException;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;


public class SpillRecord {

	/**
	 * Backing store
	 */
	  protected final ByteBuffer buf;

	  /**
	   * View of backing storage as longs
	   */
	  protected final LongBuffer entries;

	  /**
	   * The size of each record in the index file for the map-outputs.
	   */
	  protected static final int mapperOutputIndexRecordLength = 24;


	  public SpillRecord(int numPartitions) {
	    buf = ByteBuffer.allocate(
	        numPartitions * mapperOutputIndexRecordLength);
	    entries = buf.asLongBuffer();
	  }


	  public SpillRecord(Path indexFileName, Configuration configuration) throws IOException {
	    this(indexFileName, configuration, new CRC32());
	  }


	  public SpillRecord(Path indexFileName, Configuration configuration, Checksum crc)
	      throws IOException {

	    final FileSystem fileSystem = FileSystem.get(configuration);
	    final FSDataInputStream in = fileSystem.open(indexFileName);
	    try {
	      final long length = fileSystem.getFileStatus(indexFileName).getLen();
	      final int partitions = (int) length / mapperOutputIndexRecordLength;
	      final int size = partitions * mapperOutputIndexRecordLength;

	      buf = ByteBuffer.allocate(size);
	      if (crc != null) {
	        crc.reset();
	        CheckedInputStream chk = new CheckedInputStream(in, crc);
	        IOUtils.readFully(chk, buf.array(), 0, size);
	        if (chk.getChecksum().getValue() != in.readLong()) {
	          throw new ChecksumException("Checksum error reading spill index: " +
	                                indexFileName, -1);
	        }
	      } else {
	        IOUtils.readFully(in, buf.array(), 0, size);
	      }
	      entries = buf.asLongBuffer();
	    } finally {
	      in.close();
	    }
	  }


	  /**
	   * Create a new {@link SpillRecord}. The SpillRecord instance is created from the file
	   * 	whose {@link Path} is specified as argument. That Path is resolved against the
	   * 	FileSystem instance specified as argument.
	   * @param indexFilePath the {@link Path} of the file from which the SpillRecord must
	   * 	be created
	   * @param fileSystem the {@link FileSystem} instance to use to resolve the {@link Path}
	   * @throws IOException
	   */
	  public SpillRecord(Path indexFilePath, FileSystem fileSystem) throws IOException {

		    final FSDataInputStream in = fileSystem.open(indexFilePath);

		  try {
		      final long length = fileSystem.getFileStatus(indexFilePath).getLen();
		      final int partitions = (int) length / mapperOutputIndexRecordLength;
		      final int size = partitions * mapperOutputIndexRecordLength;

		      buf = ByteBuffer.allocate(size);
		        IOUtils.readFully(in, buf.array(), 0, size);
		      entries = buf.asLongBuffer();
		    } finally {
		      in.close();
		    }
	}


	/**
	   * Return number of IndexRecord entries in this spill.
	   */
	  public int size() {
	    return entries.capacity() / (mapperOutputIndexRecordLength / 8);
	  }

	  /**
	   * Get spill offsets for given partition.
	   */
	  public IndexRecord getIndex(int partition) {
	    final int pos = partition * mapperOutputIndexRecordLength / 8;
	    return new IndexRecord(entries.get(pos), entries.get(pos + 1),
	                           entries.get(pos + 2));
	  }

	  /**
	   * Set spill offsets for given partition.
	   */
	  public void putIndex(IndexRecord rec, int partition) {
	    final int pos = partition * mapperOutputIndexRecordLength / 8;
	    entries.put(pos, rec.startOffset);
	    entries.put(pos + 1, rec.rawLength);
	    entries.put(pos + 2, rec.partLength);
	  }


	  /**
	   * Write this spill record to the location provided.
	   */
	  public void writeToFile(Path loc, FileSystem fileSystem)
	      throws IOException {
	    writeToFile(loc, fileSystem, new CRC32());
	  }

//	  /**
//	   * Write this spill record to the location provided.
//	   */
//	  public void writeToFile(Path loc, Configuration configuration)
//	      throws IOException {
//	    writeToFile(loc, configuration, new CRC32());
//	  }

	  public void writeToFile(Path loc, FileSystem fileSystem, Checksum crc)
      throws IOException {
//    final FileSystem fileSystem = FileSystem.get(configuration);
    CheckedOutputStream chk = null;
    final FSDataOutputStream out = fileSystem.create(loc);
    try {
      if (crc != null) {
        crc.reset();
        chk = new CheckedOutputStream(out, crc);
        chk.write(buf.array());
        out.writeLong(chk.getChecksum().getValue());
      } else {
        out.write(buf.array());
      }
    } finally {
      if (chk != null) {
        chk.close();
      } else {
        out.close();
      }
    }
  }

//	  public void writeToFile(Path loc, Configuration configuration, Checksum crc)
//	      throws IOException {
//	    final FileSystem fileSystem = FileSystem.get(configuration);
//	    CheckedOutputStream chk = null;
//	    final FSDataOutputStream out = fileSystem.create(loc);
//	    try {
//	      if (crc != null) {
//	        crc.reset();
//	        chk = new CheckedOutputStream(out, crc);
//	        chk.write(buf.array());
//	        out.writeLong(chk.getChecksum().getValue());
//	      } else {
//	        out.write(buf.array());
//	      }
//	    } finally {
//	      if (chk != null) {
//	        chk.close();
//	      } else {
//	        out.close();
//	      }
//	    }
//	  }
}

class IndexRecord {
	  long startOffset;
	  long rawLength;
	  long partLength;

	  public IndexRecord() { }

	  public IndexRecord(long startOffset, long rawLength, long partLength) {
	    this.startOffset = startOffset;
	    this.rawLength = rawLength;
	    this.partLength = partLength;
	  }
	}
