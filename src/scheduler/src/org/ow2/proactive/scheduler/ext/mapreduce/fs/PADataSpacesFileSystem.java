package org.ow2.proactive.scheduler.ext.mapreduce.fs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.BufferedFSInputStream;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.FileSelector;
import org.objectweb.proactive.extensions.dataspaces.api.FileType;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.ext.mapreduce.PAHadoopJobConfiguration;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceFrameworkProperties;

/**
 * The {@link PADataSpacesFileSystem} is the ProActive implementation of the
 * Hadoop Abstract File System {@link FileSystem}. The
 * {@link PADataSpacesFileSystem} is a {@link DataSpacesFileObject} that expose
 * the Hadoop Abstract File System interface (or we can say the
 * PADataSpacesFileSystem wrap a DataSpacesFileObject and implement the
 * interface of the Hadoop Abstract File System, {@link FileSystem}). We must
 * notice that outside we choose not to expose the real URI of the
 * DataSpacesFileObject but only its virtual URI. (the difference between the
 * real URI and the virtual URI of a DataSpacesFileObject is the following:
 * <ul>
 * <li>Real URI:
 * file:///tmp/scratch/PA_JVM52068472/__PA__HalfbodiesNode1592397106
 * /1234431/-3d7e79cb-
 * 12ea53661e8--7fe6--56092ae50fa1399b--3d7e79cb-12ea53661e8--8000/user/usernName/nas/home/mapreduce/data/current_in
 * p u t</li>
 * <li>Virtual URI:
 * vfs:///1234431/scratch/PA_JVM52068472/__PA__HalfbodiesNode1592397106
 * /-3d7e79cb-
 * 12ea53661e8--7fe6--56092ae50fa1399b--3d7e79cb-12ea53661e8--8000/user/userName/nas/home/mapreduce/data/current_in
 * p u t</li>
 * </ul>
 * where the actual path in the file system is:
 * "/user/userName/nas/home/mapreduce/data/current_input")
 *
 * When executing the ProActive MapReduce workflow each task
 * will create an instance of this class to read data from or write data to its
 * INPUT, LOCAL, OUTPUT FileSystem.
 *
 * The current implementation of {@link PADataSpacesFileSystem} does not support the
 * {@link org.apache.hadoop.fs.FileSystem#Cache}
 *
 * TODO the {@link Path} instances that this implementation of the
 * {@link FileSystem} will resolve follow the format
 * "pads:/mapreduce/data/current_input". If we convert this string in a URI and
 * we invoke the "URI.getPath()" method we obtain
 * "/mapreduce/data/current_input". This resulting path will be the one we have
 * to resolve against the {@link DataSpacesFileObject} this {@link FileSystem}
 * is built on. We must notice that instantiating a {@link Path} object from the
 * "pads:/mapreduce/data/current_input" and calling the method "Path.toString()"
 * we obtain the orignary string "pads:/mapreduce/data/current_input". This
 * means Hadoop thinks the Path is absolute and do not resolve it against the
 * path we can retrieve from the Java System Property "user.dir".
 *
 * @author The ProActive Team
 *
 */
public class PADataSpacesFileSystem extends FileSystem {

	protected String ROOT = "_ROOT_";

	/**
	 * The {@link DataSpacesFileObject} this file system is built on
	 */
	protected DataSpacesFileObject dataSpacesFileObject = null;

	/**
	 * The working directory. In the case of the data spaces is the String
	 * representation of the {@link DataSpacesFileObject} this
	 * {@link PADataSpacesFileSystem} is built from.
	 */
	protected Path workingDir = null;

	/**
	 * The statistics for this {@link FileSystem}
	 */
	protected Statistics statistics = null;

	/**
	 * Build a new {@link PADataSpacesFileSystem} detached from any
	 * {@link DataSpacesFileObject}
	 */
	public PADataSpacesFileSystem() {
	}

	/**
	 * Build a new {@link PADataSpacesFileSystem} on the specified
	 * {@link DataSpacesFileObject}
	 *
	 * @param dataSpacesFileObject
	 *            the {@link DataSpacesFileObject} this file system must be
	 *            built on
	 * @throws URISyntaxException
	 */
	public PADataSpacesFileSystem(DataSpacesFileObject dataSpacesFileObject)
			throws URISyntaxException {
		this();
		setDataSpacesFileObject(dataSpacesFileObject);
	}

	/**
	 * Set the {@link DataSpacesFileObject} this file system must use
	 *
	 * @param dataSpacesFileObject
	 *            the {@link DataSpacesFileObject} this file system must use
	 */
	public void setDataSpacesFileObject(
			DataSpacesFileObject dataSpacesFileObject)
			throws URISyntaxException {
		this.dataSpacesFileObject = dataSpacesFileObject;
		/*
		 * We have to notice that the working directory must be a path with the
		 * scheme and the authority part because then the value of the working
		 * directory will be used in the method Path.makeQualified(FileSystem
		 * fs). In that method the constructor Path(Path
		 * fs.getWorkingDirectory(), Path this) is used. In that constructor the
		 * scheme and authority part of the fs.getWorkingDirectory() Path are
		 * used. In particular the working directory must be something like
		 * "pads:/working/directory". Hence what we do in the following code is
		 * to substitute the scheme part of the virtual URI of the
		 * DataSpacesFileObject wrapped by this instance of the
		 * PADataSpacesFileSystem with the scheme, "pads", that will tell Hadoop
		 * that the FileSystem implementation to use is the
		 * PADataSpacesFileSystem
		 */
		URI dataSpacesFileObjectURI = new URI(
				dataSpacesFileObject.getVirtualURI());
		this.setWorkingDirectory(new Path(
				new URI(
						PAMapReduceFrameworkProperties
								.getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_FILE_SYSTEM_IMPLEMENTATION_SCHEME
										.getKey()), dataSpacesFileObjectURI
								.getAuthority(), dataSpacesFileObjectURI
								.getPath(), null, null).toString()));
		this.statistics = getStatistics(dataSpacesFileObject.getVirtualURI(),
				getClass());
	}

	/**
	 * Retrieve the {@link DataSpacesFileObject} this file system is built on.
	 * Return null if this {@link PADataSpacesFileSystem} is detached from any
	 * {@link DataSpacesFileObject}
	 *
	 * @return the {@link DataSpacesFileObject} this file system is built on,
	 *         null if this {@link PADataSpacesFileSystem} is detached from any
	 *         {@link DataSpacesFileObject}
	 */
	public DataSpacesFileObject getDataSpacesFileObject() {
		return dataSpacesFileObject;
	}

	@Override
	public FSDataOutputStream append(Path f, int bufferSize,
			Progressable progress) throws IOException {
		/*
		 * retrieve the name of the file from the Path instance and use it to
		 * retrieve the DataSpacesFileObject corresponding to that file
		 */
		String fPathString = getRelativePathString(f);
		if ((fPathString != null) && (!fPathString.trim().isEmpty())) {
			DataSpacesFileObject fDataSpacesFileObject = dataSpacesFileObject
					.resolveFile(fPathString);
			if ((fDataSpacesFileObject != null)
					&& (fDataSpacesFileObject.exists())
					&& (fDataSpacesFileObject.isWritable())) {
				return new FSDataOutputStream(new BufferedOutputStream(
						fDataSpacesFileObject.getContent().getOutputStream(),
						bufferSize), new Statistics(f.getName()),
						fDataSpacesFileObject.getContent().getSize());
			} else {
				throw new IOException("Cannot read data from file '"
						+ fPathString + "'");
			}
		}
		throw new IOException("Cannot read data from file '" + fPathString
				+ "'");
	}

	@Override
	public FSDataOutputStream create(Path f, FsPermission permission,
			boolean overwrite, int bufferSize, short replication,
			long blockSize, Progressable progress) throws IOException {
		String fString = getRelativePathString(f);
		if ((fString != null) && (!fString.trim().isEmpty())) {
			/*
			 * we must check that the ancestor of the file ( fString ) do not
			 * exist because otherwise we get an
			 * org.apache.commons.vfs.FileSystemException and the file will be
			 * not created. Hence we must not try to create the already existing
			 * directories. To do this we can navigate in the file path until we
			 * encounter a not existing directory and invoke the
			 * DataSpacesFileObject.create( String ) method at that directory
			 * and not before.
			 */
			DataSpacesFileObject parentDataSpacesFileObject = null;
			boolean foundNotExistingAncestorDirectory = false;
			String[] elements = fString.split(File.separator);
			int existingAncestorDirectories = 0;
			if (elements.length > 0) {
				String currentDirectory = null;
				for (int i = 0; (i < (elements.length - 1))
						&& (!foundNotExistingAncestorDirectory); i++) {
					if (i == 0) {
						currentDirectory = elements[i];
					} else {
						currentDirectory += File.separator + elements[i];
					}

					if ((dataSpacesFileObject.resolveFile(currentDirectory)
							.exists())) {
						existingAncestorDirectories++;
						parentDataSpacesFileObject = dataSpacesFileObject
								.resolveFile(currentDirectory);
					} else {
						foundNotExistingAncestorDirectory = true;
					}
				}
			}

			DataSpacesFileObject fDataSpacesFileObject = null;

			/*
			 * indexOfNotExistingAncestorDirectory is the first directory to
			 * create but if its value is equal to 0 then all the directories on
			 * the fString path must be created
			 */
			if (existingAncestorDirectories == 0) {
				fDataSpacesFileObject = dataSpacesFileObject
						.resolveFile(fString);
			} else {
				String toCreate = elements[existingAncestorDirectories];
				for (int i = existingAncestorDirectories + 1; i < elements.length; i++) {
					toCreate += File.separator + elements[i];
				}

				/*
				 * we must delete the last existing ancestor (the
				 * parentDataSpacesFileObject that will be a file) because we
				 * must recreate it as a folder and then the file to create will
				 * be created in that folder
				 */
				parentDataSpacesFileObject.delete();
				parentDataSpacesFileObject.createFolder();

				fDataSpacesFileObject = parentDataSpacesFileObject
						.resolveFile(toCreate);
			}

			if (fDataSpacesFileObject.exists()) {
				if (!overwrite) {
					throw new IOException("The file '" + f.getName()
							+ "' already exists and cannot be overwritten");
				} else {
					// the file exists and we must overwrite it, so we delete it
					// and
					// re-create it
					fDataSpacesFileObject.delete();
					fDataSpacesFileObject.createFile();
					if (! fDataSpacesFileObject.exists()) {
						/*
						 * this control is added to try to check if a process reach the limit of the number of files
						 * it can create
						 */
						throw new FileSystemException("The file '" + fDataSpacesFileObject.getVirtualURI() + "' cannot be created");
					} else {
						if (fDataSpacesFileObject.isWritable()) {
							return new FSDataOutputStream(new BufferedOutputStream(
									fDataSpacesFileObject.getContent()
											.getOutputStream(), bufferSize),
									new Statistics(f.getName()),
									fDataSpacesFileObject.getContent().getSize());
						} else {
							throw new IOException("The file '" + f.getName()
									+ "' exists but you cannot write into it");
						}
					}
				}
			} else {
				fDataSpacesFileObject.createFile();
				if ( ! fDataSpacesFileObject.exists() ) {
					/*
					 * this control is added to try to check if a process reach the limit of the number of files
					 * it can create
					 */
					throw new FileSystemException("The file '" + fDataSpacesFileObject.getVirtualURI() + "' cannot be created");
				} else {
					if (fDataSpacesFileObject.isWritable()) {
						return new FSDataOutputStream(new BufferedOutputStream(
								fDataSpacesFileObject.getContent()
										.getOutputStream(), bufferSize),
								new Statistics(f.getName()), fDataSpacesFileObject
										.getContent().getSize());
					} else {
						throw new IOException("The file '" + f.getName()
								+ "' exists but you cannot write into it");
					}
				}
			}
		}
		throw new IOException("Cannot create the file '" + f.getName() + "'");
	}

	@Override
	public boolean delete(Path f) throws IOException {
		return this.delete(f, true);
	}

	@Override
	public boolean delete(Path f, boolean recursive) throws IOException {
		String fPathRelativeString = getRelativePathString(f);
		DataSpacesFileObject fDataSpacesFileObject = dataSpacesFileObject
				.resolveFile(fPathRelativeString);
		if (fDataSpacesFileObject.exists()) {
			/*
			 * if fDataSpacesFileObject is a file then recursive can be either
			 * true or false if fDataSpacesFileObject is a directory then
			 * recursive must be true otherwise we must throw an exception
			 */
			FileType fileType = fDataSpacesFileObject.getType();
			if (fileType.equals(FileType.FOLDER)) {
				// in this case fDataSpacesFileObject is a directory
				if (!recursive) {
					throw new IOException("Cannot delete the directory '"
							+ fPathRelativeString + "' not recursively");
				}
				// recursive deletion
				List<DataSpacesFileObject> childDataSpacesFileObjectList = fDataSpacesFileObject
						.findFiles(FileSelector.EXCLUDE_SELF);
				if (fDataSpacesFileObject.delete(FileSelector.SELECT_ALL) == (childDataSpacesFileObjectList
						.size() + 1)) {
					return true;
				}
			} else if (fileType.equals(FileType.FILE)) {
				// in this case fDataSpacesFileObject is a file
				return fDataSpacesFileObject.delete();
			}
		} else {
			throw new IOException("The path '" + fPathRelativeString
					+ "' to delete does not exist in the FileSystem '"
					+ getWorkingDirectory() + "'");
		}
		return false;
	}

	@Override
	public FileStatus getFileStatus(Path f) throws IOException {
		String fPathString = getRelativePathString(f);
		if ((fPathString != null) && (!fPathString.trim().isEmpty())) {
			DataSpacesFileObject fDataSpacesFileObject = dataSpacesFileObject
					.resolveFile(fPathString);
			if (fDataSpacesFileObject.exists()) {
				PADataSpacesFileStatus paDataSpacesFileStatus = null;
				try {
					paDataSpacesFileStatus = new PADataSpacesFileStatus(
							fDataSpacesFileObject, dataSpacesFileObject);
				} catch (URISyntaxException e) {
					/*
					 * thrown by paDataSpacesFileStatus = new
					 * PADataSpacesFileStatus(fDataSpacesFileObject,
					 * dataSpacesFileObject); but we cannot throw it because
					 * otherwise the signature of this method will be different
					 * from the one of the overridden method
					 */
					e.printStackTrace();
				}
				return paDataSpacesFileStatus;
			}
		}

		throw new FileNotFoundException("The file " + fPathString
				+ " does not exist in the FileSystem '"
					+ getWorkingDirectory() + "'");
	}

	@Override
	public URI getUri() {
		try {
			return new URI(dataSpacesFileObject.getVirtualURI());
		} catch (URISyntaxException e) {
			return null;
		}
	}

	@Override
	public Path getWorkingDirectory() {
		return workingDir;
	}

	@Override
	public FileStatus[] listStatus(Path f) throws IOException {
		DataSpacesFileObject fDataSpacesFileObject = null;
		List<DataSpacesFileObject> childDataSpacesFileObjectList = null;
		String fRelativePathString = getRelativePathString(f);
		if ((fRelativePathString == null)
				|| (fRelativePathString.trim().isEmpty())) {
			fDataSpacesFileObject = dataSpacesFileObject;
			childDataSpacesFileObjectList = fDataSpacesFileObject
					.findFiles(FileSelector.EXCLUDE_SELF);
		} else {
			fDataSpacesFileObject = dataSpacesFileObject
					.resolveFile(fRelativePathString);
			childDataSpacesFileObjectList = fDataSpacesFileObject
					.findFiles(FileSelector.SELECT_ALL);
		}
		if (fDataSpacesFileObject.exists()
				&& fDataSpacesFileObject.getType().equals(FileType.FOLDER)) {
			FileStatus[] fileStatusArray = new FileStatus[childDataSpacesFileObjectList
					.size()];
			int i = 0;
			for (DataSpacesFileObject currentChildDataSpacesFileObject : childDataSpacesFileObjectList) {
				try {
					fileStatusArray[i] = new PADataSpacesFileStatus(
							currentChildDataSpacesFileObject,
							dataSpacesFileObject);
				} catch (URISyntaxException e) {
					/*
					 * thrown by fileStatusArray[i] = new
					 * PADataSpacesFileStatus(currentChildDataSpacesFileObject,
					 * dataSpacesFileObject); but we cannot throw it because
					 * otherwise the signature of this method will be different
					 * from the one of the overridden method
					 */
					e.printStackTrace();
				}
				i++;
			}
			return fileStatusArray;
		} else {
			return null;
		}
	}

	@Override
	public boolean mkdirs(Path f, FsPermission permission) throws IOException {
		String fString = getRelativePathString(f);
		if ((fString != null) && (!fString.trim().isEmpty())) {
			DataSpacesFileObject fDataSpacesFileObject = dataSpacesFileObject
					.resolveFile(fString);
			fDataSpacesFileObject.createFile();
			if (exists(new Path(fString))) {
				// the file is created so we return true
				return true;
			}
		}
		return false;
	}

	@Override
	public FSDataInputStream open(Path f, int bufferSize) throws IOException {
		String fString = getRelativePathString(f);
		if ((fString != null) && (!fString.trim().isEmpty())) {
			DataSpacesFileObject fDataSpacesFileObject = dataSpacesFileObject
					.resolveFile(fString);
			// fDataSpacesFileObject.createFile();
			/*
			 * Create a stream to read the file (if the file exists and is readable)
			 *  and it throws an exception in the other cases (like they do in the
			 *  org.apache.hadoop.fs.RawLocalFileSystem.open(Path, int) method)
			 */
			if ((exists(new Path(fString)))
					&& (fDataSpacesFileObject.isReadable())) {
				return new FSDataInputStream(new BufferedFSInputStream(
						new PADataSpacesFSInputStream(fDataSpacesFileObject),
						bufferSize));
			} else {
				throw new FileNotFoundException(fDataSpacesFileObject.getVirtualURI());
			}
		}
		return null;
	}

	@Override
	public boolean rename(Path src, Path dst) throws IOException {
		String srcPathString = getRelativePathString(src);
		String dstPathString = getRelativePathString(dst);

		if ((srcPathString != null) && (!srcPathString.trim().isEmpty())
				&& (dstPathString != null) && (!dstPathString.trim().isEmpty())) {
			DataSpacesFileObject srcDataSpacesFileObject = dataSpacesFileObject
					.resolveFile(srcPathString);
			DataSpacesFileObject dstDataSpacesFileObject = dataSpacesFileObject
					.resolveFile(dstPathString);

			/*
			 * first check if the destination file exists, if so delete it and
			 * all its descendants and re-create it
			 */
			Path dstRelativePath = new Path(dstPathString);
			if (exists(dstRelativePath)) {
				dstDataSpacesFileObject.delete(FileSelector.SELECT_ALL);
			}
			dstDataSpacesFileObject.createFile();

			if (dstDataSpacesFileObject.isWritable()) {
				// copy the content of the source file into the destination one
				dstDataSpacesFileObject.copyFrom(srcDataSpacesFileObject,
						FileSelector.SELECT_ALL);

				if (dstDataSpacesFileObject.getContent().getSize() > 0) {
					/*
					 * we delete the source file and we return true only if in
					 * the destination file there is some bytes.
					 */
					srcDataSpacesFileObject.delete(FileSelector.SELECT_ALL);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void setWorkingDirectory(Path newDir) {
		workingDir = newDir;
	}

	@Override
	public FSDataOutputStream append(Path f) throws IOException {
		/*
		 * We must override the super class corresponding method because that
		 * method will retrieve some configuration parameter and we know that in
		 * the ProActive MapReduce API/framework the way to retrieve the
		 * configuration parameters is different
		 */
		return append(
				f,
				getConf()
						.getInt(PAMapReduceFrameworkProperties
								.getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE
										.getKey()),
								PAMapReduceFrameworkProperties
										.getPropertyAsInteger(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE_DEFAULT_VALUE
												.getKey())), null);
	}

	@Override
	public void close() throws IOException {
		/*
		 * We overridden the super class corresponding method to be able to do
		 * nothing when this method is called
		 */
	}

	@Override
	public void copyFromLocalFile(boolean delSrc, boolean overwrite, Path src,
			Path dst) throws IOException {
		/*
		 * using the data spaces the copy is always local, that is to say from
		 * the src file in this PADataSpacesFileSystem to the dst file that is
		 * stored in this PADataSpacesFileSystem too
		 */
		FileUtil.copy(this, src, this, dst, delSrc, overwrite, getConf());
	}

	@Override
	public void copyFromLocalFile(boolean delSrc, boolean overwrite,
			Path[] srcs, Path dst) throws IOException {
		/*
		 * using the data spaces the copy is always local, that is to say from
		 * the src file in this PADataSpacesFileSystem to the dst file that is
		 * stored in this PADataSpacesFileSystem too
		 */
		FileUtil.copy(this, srcs, this, dst, delSrc, overwrite, getConf());
	}

	@Override
	public void copyToLocalFile(boolean delSrc, Path src, Path dst)
			throws IOException {
		/*
		 * using the data spaces the copy is always local, that is to say from
		 * the src file in this PADataSpacesFileSystem to the dst file that is
		 * stored in this PADataSpacesFileSystem too
		 */
		FileUtil.copy(this, src, this, dst, delSrc, getConf());
	}

	@Override
	public FSDataOutputStream create(Path f, boolean overwrite)
			throws IOException {
		/*
		 * we have to customize this method because the method of the super
		 * class FileSystem.create(Path f, boolean overwrite) retrieve some
		 * configuration parameters from the Hadoop configuration.
		 */
		return create(
				f,
				overwrite,
				getConf()
						.getInt(PAMapReduceFrameworkProperties
								.getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE
										.getKey()),
								PAMapReduceFrameworkProperties
										.getPropertyAsInteger(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE_DEFAULT_VALUE
												.getKey())),
				getDefaultReplication(), getDefaultBlockSize());
	}

	@Override
	public FSDataOutputStream create(Path f, Progressable progress)
			throws IOException {
		/*
		 * we have to customize this method because the method of the super
		 * class FileSystem.create(Path f, boolean overwrite) retrieve some
		 * configuration parameters from the Hadoop configuration.
		 */
		return create(
				f,
				true,
				getConf()
						.getInt(PAMapReduceFrameworkProperties
								.getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE
										.getKey()),
								PAMapReduceFrameworkProperties
										.getPropertyAsInteger(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE_DEFAULT_VALUE
												.getKey())),
				getDefaultReplication(), getDefaultBlockSize());
	}

	@Override
	public FSDataOutputStream create(Path f, short replication,
			Progressable progress) throws IOException {
		/*
		 * we have to customize this method because the method of the super
		 * class FileSystem.create(Path f, boolean overwrite) retrieve some
		 * configuration parameters from the Hadoop configuration.
		 */
		return create(
				f,
				true,
				getConf()
						.getInt(PAMapReduceFrameworkProperties
								.getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE
										.getKey()),
								PAMapReduceFrameworkProperties
										.getPropertyAsInteger(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE_DEFAULT_VALUE
												.getKey())),
				getDefaultReplication(), getDefaultBlockSize(), progress);
	}

	@Override
	public FSDataOutputStream create(Path f, short replication)
			throws IOException {
		/*
		 * we have to customize this method because the method of the super
		 * class FileSystem.create(Path f, boolean overwrite) retrieve some
		 * configuration parameters from the Hadoop configuration.
		 */
		return create(
				f,
				true,
				getConf()
						.getInt(PAMapReduceFrameworkProperties
								.getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE
										.getKey()),
								PAMapReduceFrameworkProperties
										.getPropertyAsInteger(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE_DEFAULT_VALUE
												.getKey())), replication,
				getDefaultBlockSize());
	}

	@Override
	public boolean createNewFile(Path f) throws IOException {
		/*
		 * We need to override this method because the corresponding method of
		 * the super class (FileSystem.createNewFile(Path)) retrieve some
		 * configuration parameters. Since in the ProActive MapReduce
		 * API/framework the retrieving of the configuration parameters is a
		 * little bit different than in Hadoop, we must override the super class
		 * method FileSystem.createNewFile(Path)
		 */
		String fPathRelativeString = getRelativePathString(f);
		if ((fPathRelativeString != null)
				&& (!fPathRelativeString.trim().isEmpty())) {
			// we are forced to create a new Path
			Path fRelative = new Path(fPathRelativeString);
			if (exists(fRelative)) {
				return false;
			} else {
				create(
						fRelative,
						false,
						getConf()
								.getInt(PAMapReduceFrameworkProperties
										.getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE
												.getKey()),
										PAMapReduceFrameworkProperties
												.getPropertyAsInteger(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE_DEFAULT_VALUE
														.getKey()))).close();
				return true;
			}
		}
		return false;
	}

	@Override
	public BlockLocation[] getFileBlockLocations(FileStatus file, long start,
			long len) throws IOException {
		/*
		 * we must override this method because we must control the way the file
		 * block locations are created. In fact in the case of the
		 * PADataSpacesFileSystem there are no file blocks location. In that
		 * case a file is made up of a single block (whose size will be equal to
		 * the size of the whole file) that resides in the DataSpacesFileObject
		 * the PADataSpacesFileSystem is built from
		 */
		if (file == null) {
			return null;
		}

		if ((start < 0) || (len < 0)) {
			throw new IllegalArgumentException("Invalid start or len parameter");
		}

		if (file.getLen() < start) {
			return new BlockLocation[0];

		}
		String[] name = { PAMapReduceFrameworkProperties
				.getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_FILE_SYSTEM_LOCALHOST_NAME
						.getKey()) };
		String[] host = { PAMapReduceFrameworkProperties
				.getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_FILE_SYSTEM_LOCALHOST_NAME
						.getKey()) };
		return new BlockLocation[] { new BlockLocation(name, host, 0,
				file.getLen()) };
	}

	@Override
	public Path getHomeDirectory() {
		return new Path(dataSpacesFileObject.getVirtualURI());
	}

	@Override
	public long getUsed() throws IOException {
		List<DataSpacesFileObject> dataSpacesFileObjectList = dataSpacesFileObject
				.findFiles(FileSelector.SELECT_ALL);
		if (dataSpacesFileObjectList != null) {
			FileStatus[] fileStatusArray = new FileStatus[dataSpacesFileObjectList
					.size()];
			int i = 0;
			for (DataSpacesFileObject dataSpacesFileObject : dataSpacesFileObjectList) {
				try {
					fileStatusArray[i] = new PADataSpacesFileStatus(
							dataSpacesFileObject, dataSpacesFileObject);
				} catch (URISyntaxException e) {
					/*
					 * thrown by fileStatusArray[i] = new
					 * PADataSpacesFileStatus(dataSpacesFileObject,
					 * dataSpacesFileObject); but we cannot throw it because
					 * otherwise the signature of this method will be different
					 * from the one of the overridden method
					 */
					e.printStackTrace();
				}
				i++;
			}

			long used = 0;
			for (FileStatus fileStatus : fileStatusArray) {
				used += fileStatus.getLen();
			}
			return used;
		}
		return 0;
	}

	@Override
	public void initialize(URI name, Configuration conf) throws IOException {
		// TODO Auto-generated method stub
		super.initialize(name, conf);

		/*
		 * Setting the various property in the ProActive MapReduce Framework
		 * configuration file we were able to create the instance of the
		 * FileSystem we want: the PADataSpacesFileSystem. The only thing we
		 * cannot do is to specify the instance of the DataSpacesFileObject that
		 * FileSystem must use. We cannot pass it in the Configuration because
		 * the Configuration allow us to pass only key-value pairs where both
		 * key and value are String, while we need to pass an instance of the
		 * DataSpacesFileObject that is not serializable. However we have a
		 * solution to do that: in fact we are passing the DataSpacesFileSystem
		 * instance among objects in the same ProActive node and not among
		 * objects that resides on different ProActive nodes. This means the
		 * serialization of the DataSpacesFileObject is not needed and hence the
		 * fact the DataSpacesFileObject is not serializable is not a
		 * limitation.
		 */
		if (conf instanceof PAHadoopJobConfiguration) {
			DataSpacesFileObject dataSpacesFileObject = ((PAHadoopJobConfiguration) conf)
					.getDataSpacesFileObject();
			if (dataSpacesFileObject != null) {
				try {
					this.setDataSpacesFileObject(dataSpacesFileObject);
				} catch (URISyntaxException e) {
					/*
					 * We must catch the URISyntaxException because we cannot
					 * add it as a throwing clause because then the signature of
					 * this method will be not compatible with the one of the
					 * method of the parent class. TODO We must notice, maybe,
					 * we have to modify the kind of thrown exception
					 */
					throw new IOException(
							"Cannot initialize the PADataSpacesFileSystem because an URISyntaxException occurred");
				}
			} else {
				// TODO maybe we must modify the type of the thrown exception
				throw new IOException(
						"Cannot initialize the PADataSpacesFileSystem on a null DataSpacesFileObject");
			}
		} else {
			// TODO maybe we must modify the type of the thrown exception
			throw new IOException(
					"Cannot initialize the PADataSpacesFileSystem with a non FakeHadoopConfiguration instance: "
							+ conf.getClass().getName());
		}
	}

	@Override
	public FileStatus[] listStatus(Path f, PathFilter filter)
			throws IOException {
		/*
		 * we need to customize this method because the method of the parent
		 * class FileSystem.listStatus(Path[] files, PathFilter filter) calls
		 * iteratively the method of the parent class
		 * FileSystem.listStatus(ArrayList<FileStatus> results, Path f,
		 * PathFilter filter). But that method is private and not visible by
		 * this subclass... so we need to customize this method to call the
		 * method PADataSpacesFileStatus.listStatus(ArrayList<FileStatus), Path
		 * f, PathFilter filter)
		 */
		ArrayList<FileStatus> fileStatusList = new ArrayList<FileStatus>();
		listStatus(fileStatusList, f, filter);
		return fileStatusList.toArray(new FileStatus[fileStatusList.size()]);
	}

	/**
	 * Filter files/directories in the given path using the user-supplied path
	 * filter. Results are added to the given array <code>results</code>.
	 *
	 * @param results
	 *            the array in which the resulting FileStatus must be added
	 * @param f
	 *            the path in which the files and directories must be filtered
	 * @param filter
	 *            the filter to use
	 * @throws IOException
	 */
	protected void listStatus(ArrayList<FileStatus> results, Path f,
			PathFilter filter) throws IOException {
		/*
		 * We must notice maybe we can use the PathFilter because we can invoke
		 * the PADataSpacesFileStatus.getPath()
		 *
		 * We must also notice that this method represent a reimplementation of
		 * the super class method
		 * "private void listStatus(ArrayList<FileStatus>, Path, PathFilter)"
		 * that is "private" and so not visible from this sub-class
		 */
		FileStatus[] listing = listStatus(f);
		if (listing != null) {
			for (int i = 0; i < listing.length; i++) {
				if (filter.accept(listing[i].getPath())) {
					results.add(listing[i]);
				}
			}
		}
	}

	@Override
	public FileStatus[] listStatus(Path[] files, PathFilter filter)
			throws IOException {
		/*
		 * we need to customize this method because the method of the parent
		 * class FileSystem.listStatus(Path[] files, PathFilter filter) calls
		 * iteratively the method of the parent class
		 * FileSystem.listStatus(ArrayList<FileStatus> results, Path f,
		 * PathFilter filter). But that method is private and not visible by
		 * this subclass... so we need to customize this method to call the
		 * method PADataSpacesFileStatus.listStatus(ArrayList<FileStatus), Path
		 * f, PathFilter filter)
		 */
		ArrayList<FileStatus> fileStatusList = new ArrayList<FileStatus>();
		for (int i = 0; i < files.length; i++) {
			listStatus(fileStatusList, files[i], filter);
		}
		return fileStatusList.toArray(new FileStatus[fileStatusList.size()]);
	}

	@Override
	public Path makeQualified(Path path) {
		/*
		 * as a qualified path object we return the string representation of the
		 * Path (scheme, authority and path)
		 */
		return new Path(path.toUri().toString());
	}

	@Override
	public FSDataInputStream open(Path f) throws IOException {
		/*
		 * we need to customize this method because the parent class method
		 * FileSystem.open(Path f) before the invocation of the method
		 * FileSystem.open(Path f, int bufferSize) (that is an abstract method
		 * that we implemented) retrieve the configuration parameter
		 * corresponding to the size of the buffer to use to bufferize the
		 * stream of input data coming from the Path (when reading from it). In
		 * the ProActive MapReduce API/framework the retrieving of the
		 * configuration parameter corresponding to the size of the buffer is a
		 * little bit different. This means we must override the parent class
		 * method.
		 */
		int bufferSize = getConf()
				.getInt(PAMapReduceFrameworkProperties.getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE
						.getKey()),
						PAMapReduceFrameworkProperties
								.getPropertyAsInteger(PAMapReduceFrameworkProperties.HADOOP_IO_FILE_BUFFER_SIZE_DEFAULT_VALUE
										.getKey()));
		return open(f, bufferSize);
	}

	@Override
	public Path startLocalOutput(Path fsOutputFile, Path tmpLocalFile)
			throws IOException {
		/*
		 * Using the ProActive data spaces we write directly into the final
		 * location (fsOutputFile) so we simply have to create a
		 * DataSpacesFileObject with the same name of the fsOutputFile. The
		 * return value of this method is the Path fsOutputFile.
		 */
		String fsOutputFilePathString = getRelativePathString(fsOutputFile);
		DataSpacesFileObject fsOutputFileDataSpacesFileObject = dataSpacesFileObject
				.resolveFile(fsOutputFilePathString);
		fsOutputFileDataSpacesFileObject.createFile();
		if (fsOutputFileDataSpacesFileObject.exists()) {
			return fsOutputFile;
		}
		return null;
	}

	/**
	 * Verify if the given {@link Path} refers to an hidden file or not
	 *
	 * @param f
	 *            the path
	 * @return true if the path refers to an hidden file, false otherwise
	 * @throws FileSystemException
	 */
	public boolean isHidden(Path f) throws FileSystemException {
		String fPathString = getRelativePathString(f);
		if ((fPathString == null) || (fPathString.trim().isEmpty())) {
			/*
			 * TODO Ask to Christian. The following means that the
			 * PADataSpacesFileSystem can be build on a hidden
			 * DataSpacesFileObject.
			 */
			return dataSpacesFileObject.isHidden();
		}
		DataSpacesFileObject fDataSpacesFileObject = dataSpacesFileObject
				.resolveFile(fPathString);
		return fDataSpacesFileObject.isHidden();
	}

	/**
	 * Relativize the specified {@link Path} in such a way it can be resolved
	 * against this instance of the PADataSpacesFileSystem.
	 *
	 * @param path
	 *            the path to relativize
	 * @return String the string representation of the relativized path. An
	 *         empty or null string means we must consider the root of this
	 *         PADataSpacesFileSystem instance
	 */
	protected String getRelativePathString(Path path) {
		/*
		 * We know this is a tricky code but it is the only way to solve the
		 * problem linked to the execution of the instruction
		 * "FSDataOutputStream fileOut = fs.create(file, false);" in the
		 * TextOutputFormat (that is the default OutputFormat used by Hadoop).
		 *
		 * What we do here is to delete from the string representation of the
		 * Path argument the virtual URI string representation (if contained in
		 * the String representation of the Path) of the DataSpacesFileObject
		 * this PADataSpacesFileSystem wraps. Then from the remaining string we
		 * must delete the initial "/", if present, to get the relative Path.
		 *
		 * We must notice that when we have "Path path = new Path( "/" );" and
		 * we try to create a file corresponding to that Path using this
		 * instance of the PADataSpacesFileSystem we get an error, which is
		 * raised up by the wrapped DataSpacesFileObject
		 * (org.objectweb.proactive.extensions.dataspaces.exceptions
		 * .FileSystemException: Cannot resolve an absolute path).
		 *
		 * We must also notice that if we have "Path path = new Path( "/" );"
		 * and we relativize that Path (erasing the "/" from the String that
		 * represent the Path) we get an empty String. We cannot instantiate a
		 * Path from an empty string. This means that we cannot have a method
		 * "protected Path getRelativePath(Path)" because in that case we cannot
		 * return an instance of a Path. But we could return a null Path... so
		 * in our implementation a null Path would mean "consider the root
		 * directory" of the PADataSpacesFileSystem instance. But then in a
		 * method of the PADataSpacesFileSystem class that take a Path instance
		 * as argument, if we receive a null Path we have to consider the root
		 * directory or we have to conclude we have an error because we get a
		 * null Path as parameter? Since we want to conclude we got an error we
		 * choose the following signature for the method
		 * "String getRelativePathString(Path)" and we choose to perform some
		 * check on the returned String: if it is empty we conclude we have to
		 * consider the root of the file system.
		 */
		if (path == null) {
			return null;
		}
		String workingDirectoryString = getWorkingDirectory().toUri().getPath();
		URI pathUri = path.toUri();
		String pathUriPath = null;
		String pathUriScheme = pathUri.getScheme();
		String pathUriAuthority = pathUri.getAuthority();

		if (((pathUriScheme != null) && (!pathUriScheme.trim()
				.equalsIgnoreCase("")))
				|| ((pathUriAuthority != null) && (!pathUriAuthority.trim()
						.equalsIgnoreCase("")))) {
			pathUriPath = pathUri.getPath();
			int workingDirectoryStringIndex = pathUriPath
					.indexOf(workingDirectoryString);
			if (workingDirectoryStringIndex >= 0) {
				// we must notice the "+ 1" delete the initial "/" character
				pathUriPath = pathUriPath.substring(workingDirectoryStringIndex
						+ workingDirectoryString.length() + 1);
			}
		} else {
			/*
			 * we must notice the "substring(1)" delete the initial "/"
			 * character
			 */
			pathUriPath = pathUri.getPath();
			if (pathUriPath.startsWith(File.separator)) {
				pathUriPath = pathUriPath.substring(1);
			}
		}
		return pathUriPath;
	}
}
