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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.dataspace;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import org.apache.commons.vfs2.*;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;

import javax.ws.rs.core.HttpHeaders;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class FileSystem {

    private FileSystemManager fsm;
    private String userspace;
    private String globalspace;

    private FileSystem(String userspace, String globalspace, FileSystemManager fsm) {
        this.userspace = userspace;
        this.globalspace = globalspace;
        this.fsm = fsm;
    }

    public FileObject resolveFileInUserspace(String pathname) throws FileSystemException {
        FileObject answer = fsm.resolveFile(fsm.resolveFile(userspace), pathname);
        answer.refresh();
        return answer;
    }

    public FileObject resolveFileInGlobalspace(String pathname) throws FileSystemException {
        FileObject answer = fsm.resolveFile(fsm.resolveFile(globalspace), pathname);
        answer.refresh();
        return answer;
    }

    public FileObject resolveFile(String dirPath, String pathname) throws FileSystemException {
        FileObject answer = fsm
                .resolveFile(dirPath + (dirPath.endsWith(File.separator) ? "" : File.separator) + pathname);
        answer.refresh();
        return answer;
    }

    public FileObject createFile(String pathname) throws FileSystemException {
        FileObject fo = fsm.resolveFile(pathname);
        fo.refresh();
        if (!fo.exists()) {
            fo.createFile();
        }
        return fo;
    }

    public static ListFile list(FileObject fo, List<String> includes, List<String> excludes) throws FileSystemException {
        fo.refresh();
        ListFile list = new ListFile();
        List<String> dirList = Lists.newArrayList();
        List<String> fileList = Lists.newArrayList();
        List<String> fullList = Lists.newArrayList();
        List<FileObject> foundFileObjects = new LinkedList<>();
        if (isNullOrEmpty(includes) && isNullOrEmpty(excludes)) {
            fo.findFiles(Selectors.SELECT_CHILDREN, false, foundFileObjects);
        } else {
            FileSelector selector = new org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector(includes, excludes);
            fo.findFiles(selector, false, foundFileObjects);
        }

        for (FileObject child : foundFileObjects) {
            FileType type = child.getType();
            FileName childName = child.getName();
            switch (type) {
                case FOLDER:
                    if (!child.equals(fo)) {
                        // exclude root directory from the list
                        String relativePath = fo.getName().getRelativeName(childName);
                        dirList.add(relativePath);
                        fullList.add(relativePath);
                    }
                    break;
                case FILE:
                    String relativePath = fo.getName().getRelativeName(childName);
                    fileList.add(relativePath);
                    fullList.add(relativePath);
                    break;
                default:
                    throw new RuntimeException("Unknown : " + type);
            }
        }
        list.setDirectoryListing(dirList);
        list.setFileListing(fileList);
        list.setFullListing(fullList);
        return list;
    }

    public static Map<String, Object> metadata(FileObject fo) throws FileSystemException {
        Map<String, Object> props = Maps.newHashMap();
        switch (fo.getType()) {
            case FOLDER:
                fillDirProps(fo, props);
                break;
            case FILE:
                fillFileProps(fo, props);
                break;
            default:
                throw new RuntimeException("Unknown location.");
        }
        return props;
    }

    private static void fillDirProps(FileObject fo, Map<String, Object> properties)
            throws FileSystemException {
        properties.put("x-proactive-ds-type", "DIRECTORY");
        properties.put("Last-Modified", new Date(fo.getContent().getLastModifiedTime()));
    }

    private static void fillFileProps(FileObject fo, Map<String, Object> properties)
            throws FileSystemException {
        properties.put("x-proactive-ds-type", "FILE");
        properties.put(HttpHeaders.LAST_MODIFIED, new Date(fo.getContent().getLastModifiedTime()));
        properties.put(HttpHeaders.CONTENT_TYPE, contentType(fo));
        properties.put(HttpHeaders.CONTENT_LENGTH, fo.getContent().getSize());
    }

    public static List<FileObject> findFiles(FileObject root, List<String> includes, List<String> excludes)
            throws FileSystemException {
        root.refresh();
        List<FileObject> files = Lists.newArrayList();
        FileSelector selector = (isNullOrEmpty(includes) && isNullOrEmpty(excludes)) ? new AllFilesSelector()
                : new org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector(includes,
                    excludes);
        root.findFiles(selector, true, files);
        return files;
    }

    public static void copy(InputStream is, FileObject outFile) throws IOException {
        outFile.refresh();
        Closer closer = Closer.create();
        closer.register(is);
        try {
            OutputStream os = outFile.getContent().getOutputStream();
            closer.register(os);
            ByteStreams.copy(is, os);
        } catch (IOException ioe) {
            throw closer.rethrow(ioe);
        } finally {
            closer.close();
        }
    }

    public static void copy(FileObject fo, OutputStream os) throws IOException {
        fo.refresh();
        Closer closer = Closer.create();
        closer.register(os);
        try {
            InputStream is = fo.getContent().getInputStream();
            closer.register(is);
            ByteStreams.copy(is, os);
        } catch (IOException ioe) {
            throw closer.rethrow(ioe);
        } finally {
            closer.close();
        }
    }

    public static boolean isEmpty(FileObject fo) throws FileSystemException {
        fo.refresh();
        FileObject[] children = fo.getChildren();
        return children == null || children.length == 0;
    }

    private static String baseName(FileObject fo) throws FileSystemException {
        return fo.getName().getBaseName();
    }

    private static String contentType(FileObject fo) throws FileSystemException {
        return fo.getContent().getContentInfo().getContentType();
    }

    private static boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    static class Builder {
        public static FileSystem create(SchedulerProxyUserInterface schedulerProxy)
                throws FileSystemException, NotConnectedException, PermissionException {
            return new FileSystem(schedulerProxy.getUserSpaceURIs().get(0),
                schedulerProxy.getGlobalSpaceURIs().get(0), VFSFactory.createDefaultFileSystemManager());
        }
    }

    private static final class AllFilesSelector implements FileSelector {
        @Override
        public boolean includeFile(FileSelectInfo selInfo) throws Exception {
            return FileType.FILE == selInfo.getFile().getName().getType();
        }

        @Override
        public boolean traverseDescendents(FileSelectInfo arg0) throws Exception {
            return true;
        }
    }

}
