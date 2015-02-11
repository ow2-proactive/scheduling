package org.ow2.proactive.scheduler.ext.mapreduce.fs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.FileSelector;
import org.objectweb.proactive.extensions.dataspaces.api.FileType;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;


/**
 * The {@link PADataSpacesFileStatus} class is an adapter of the
 * {@link DataSpacesFileObject}. It extends the {@link FileStatus} class so that
 * it shows the same interface. In our first implementation some functionalities
 * of the parent class are not supported like the properties related to the
 * {@link FsPermission} because that permission are already provided by the
 * ProActive data spaces mechanism.
 *
 * @author The ProActive Team
 *
 */
public class PADataSpacesFileStatus extends FileStatus {

    /**
     * the {@link DataSpacesFileObject} this {@link FileStatus} refers to
     */
    protected DataSpacesFileObject dataSpacesFileObject = null;

    /**
     * the {@link Path} instance that identifies the file this
     * {@link FileStatus} refers to in the Hadoop MapReduce file system
     */
    protected Path path = null;

    /**
     * the {@link DataSpacesFileObject} size
     */
    protected long length = 0;

    /**
     * if the {@link DataSpacesFileObject} is a directory or not
     */
    protected boolean isdir = false;

    /**
     * the block replication factor. In the case of the ProActive data spaces it
     * is 1 to indicate the only existing {@link DataSpacesFileObject}
     */
    protected short block_replication;

    /**
     * the size of a block . In the case of the ProActive data spaces it is
     * equal to the size of the {@link DataSpacesFileObject}
     */
    protected long blocksize;

    /**
     * the modification time of the file identified by the
     * {@link DataSpacesFileObject}. In the case of the ProActive data spaces
     * this information is not available
     */
    protected long modification_time;

    /**
     * the access time of the file identified by the
     * {@link DataSpacesFileObject}. In the case of the ProActive data spaces
     * this information is not available
     */
    protected long access_time;

    /**
     * the permission of the file identified by the {@link DataSpacesFileObject}
     * . In the case of the ProActive data spaces this information is not
     * available
     */
    protected FsPermission permission;

    /**
     * the owner of the file identified by the {@link DataSpacesFileObject}. In
     * the case of the ProActive data spaces this information is not available
     */
    protected String owner;

    /**
     * the group of the file identified by the {@link DataSpacesFileObject}. In
     * the case of the ProActive data spaces this information is not available
     */
    protected String group;

    /**
     * the DataSpacesFileObject toward which the path string we obtain by this
     * {@link FileStatus} can be resolved from
     */
    protected DataSpacesFileObject parentDataSpacesFileObject = null;

    /**
     * This constructor act as the PADataSpacesFileStatus(0, false, 0, 0, 0, 0,
     * null, null, null, null)
     */
    public PADataSpacesFileStatus() {
        this(0, false, 0, 0, 0, 0, null, null, null, null);
    }

    /**
     * This constructor act as the PADataSpacesFileStatus(length, isdir,
     * block_replication, blocksize, modification_time, 0, null, null, null,
     * path)
     *
     * @param length
     *            the length of the file to which this {@link FileStatus} refers
     *            to
     * @param isdir
     *            if the file is a directory
     * @param block_replication
     *            the replication factor of the file
     * @param blocksize
     *            the size of the block of the file
     * @param modification_time
     *            the time of the modification of the file
     * @param path
     *            the {@link Path} that identifies the file in the Hadoop
     *            MapReduce {@link FileSystem}
     */
    public PADataSpacesFileStatus(long length, boolean isdir, int block_replication, long blocksize,
            long modification_time, Path path) {
        this(length, isdir, block_replication, blocksize, modification_time, 0, null, null, null, path);
    }

    /**
     * The constructor
     *
     * @param length
     *            the length of the file to which this {@link FileStatus} refers
     *            to is a directory
     * @param isdir
     *            if the file is a directory
     * @param block_replication
     *            the replication factor of the file
     * @param blocksize
     *            the size of the block of the file
     * @param modification_time
     *            the time of the last modification to the file
     * @param access_time
     *            the time of the last access to the file
     * @param permission
     *            the permissions of the file
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param path
     *            the {@link Path} that identifies the file in the Hadoop
     *            MapReduce {@link FileSystem}
     */
    public PADataSpacesFileStatus(long length, boolean isdir, int block_replication, long blocksize,
            long modification_time, long access_time, FsPermission permission, String owner, String group,
            Path path) {
        super();
        this.length = length;
        this.isdir = isdir;
        this.block_replication = (short) block_replication;
        this.blocksize = blocksize;
        this.modification_time = modification_time;
        this.access_time = access_time;
        this.permission = (permission == null) ? FsPermission.getDefault() : permission;
        this.owner = (owner == null) ? "" : owner;
        this.group = (group == null) ? "" : group;
        this.path = path;
    }

    /**
     * Instantiate a {@link PADataSpacesFileStatus} using the specified
     * {@link DataSpacesFileObject}
     *
     * @param dataSpacesFileObject
     *            the {@link DataSpacesFileObject} to use to instantiate this
     *            {@link PADataSpacesFileStatus}
     * @throws FileSystemException
     * @throws URISyntaxException
     */
    public PADataSpacesFileStatus(DataSpacesFileObject dataSpacesFileObject,
            DataSpacesFileObject parentDataSpacesFileObject) throws FileSystemException, URISyntaxException {
        this();
        this.parentDataSpacesFileObject = parentDataSpacesFileObject;
        setDataSpacesFileObject(dataSpacesFileObject);
    }

    /**
     * Set the {@link DataSpacesFileObject} this {@link PADataSpacesFileStatus}
     * refers to
     *
     * @param dataSpacesFileObject
     *            the {@link DataSpacesFileObject} this
     *            {@link PADataSpacesFileStatus} refers to
     * @throws FileSystemException
     * @throws URISyntaxException
     */
    protected void setDataSpacesFileObject(DataSpacesFileObject dataSpacesFileObject)
            throws FileSystemException, URISyntaxException {
        this.dataSpacesFileObject = dataSpacesFileObject;

        FileType fileType = this.dataSpacesFileObject.getType();
        if (fileType.equals(FileType.FOLDER)) {
            List<DataSpacesFileObject> childDataSpacesFileObjectList = this.dataSpacesFileObject
                    .findFiles(FileSelector.EXCLUDE_SELF);
            /*
             * in this case the specified DataSpacesFileobject is a directory,
             * so its size will be equal to the sum of size of all its children,
             * We have to notice that if we want to get the size of a
             * DataSpacesFileObject that is a directory we get an error as
             * "org.apache.commons.vfs2.FileSystemException: Could not determine the size of "
             * file:///..." because it is not a file
             */
            for (DataSpacesFileObject currentDataSpacesFileObject : childDataSpacesFileObjectList) {
                try {
                    length += currentDataSpacesFileObject.getContent().getSize();
                } catch (FileSystemException fse) {
                    // do nothing
                }
            }
            isdir = true;
        } else if (fileType.equals(FileType.FILE)) {
            length = this.dataSpacesFileObject.getContent().getSize();
            isdir = false;
        } else {
            length = 0;
            isdir = false;
        }
        block_replication = (short) 1;
        blocksize = length;
        modification_time = this.dataSpacesFileObject.getContent().getLastModifiedTime();
        access_time = modification_time;

        /*
         * the String that represent a path and that is stored in the "path"
         * attribute of this FileStatus will be relative to an instance of a
         * DataSpacesFileObject, the parentDataSpacesFileObject. Hence in the
         * following code statement we relativize the string representation of
         * a data space against its parent. Then the relativized string is used
         * to create the path that must be stored in the "path" attribute.
         * We must notice that in the following code statement the parentPathString
         * can end with the "/" character so we must check if the last character is
         * the "/" (the File.separator) and in that case we must do substring starting
         * at "parentPathString + parentPathString.length()" (because in that case
         * the "/" is alread y included in the parentPathString); otherwise we must
         * do substring at "parentPathString + parentPathString.length() + 1".
         */
        String parentPathString = new URI(parentDataSpacesFileObject.getRealURI()).toString();
        String pathString = new URI(this.dataSpacesFileObject.getRealURI()).toString();

        if (parentPathString.endsWith(File.separator)) {
            pathString = pathString.substring(pathString.indexOf(parentPathString) +
                parentPathString.length());
        } else {
            pathString = pathString.substring(pathString.indexOf(parentPathString) +
                parentPathString.length() + File.separator.length());
        }

        path = new Path(pathString);
    }

    /**
     * Retrieve the {@link DataSpacesFileObject} this
     * {@link PADataSpacesFileStatus} is an adapter
     *
     * @return {@link DataSpacesFileObject} the dataSpacesFileObject
     */
    protected DataSpacesFileObject getDataSpacesFileObject() {
        return dataSpacesFileObject;
    }

    @Override
    public int compareTo(Object o) {
        return (dataSpacesFileObject.getRealURI().compareTo(((PADataSpacesFileStatus) o)
                .getDataSpacesFileObject().getRealURI()));
    }

    @Override
    public boolean equals(Object o) {
        return (dataSpacesFileObject.getRealURI().equalsIgnoreCase(((PADataSpacesFileStatus) o)
                .getDataSpacesFileObject().getRealURI()));
    }

    @Override
    public long getAccessTime() {
        return access_time;
    }

    @Override
    public long getBlockSize() {
        return blocksize;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public long getLen() {
        return length;
    }

    @Override
    public long getModificationTime() {
        return modification_time;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public FsPermission getPermission() {
        return permission;
    }

    @Override
    public short getReplication() {
        return block_replication;
    }

    @Override
    public int hashCode() {
        return dataSpacesFileObject.hashCode();
    }

    @Override
    public boolean isDir() {
        return isdir;
    }

    @Override
    protected void setGroup(String group) {
        this.group = group;
    }

    @Override
    protected void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    protected void setPermission(FsPermission permission) {
        this.permission = permission;
    }

    /**
     * The method of {@link Serializable} interface to implement to let the
     * {@link PADataSpacesFileStatus} have special handling during the
     * deserialization process.
     *
     * @param in
     *            {@link ObjectInputStream} to deserialize this object from
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        readFields(in);
    }

    /**
     * The method of the {@link Serializable} interface to implement to let the
     * {@link PADataSpacesFileStatus} have special handling during the
     * serialization process
     *
     * @param out
     *            {@link ObjectOutputStream} to serialize this object into
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        write(out);
    }
}
