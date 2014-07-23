/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;

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
        return fsm.resolveFile(fsm.resolveFile(userspace), pathname);
    }

    public FileObject resolveFileInGlobalspace(String pathname) throws FileSystemException {
        return fsm.resolveFile(fsm.resolveFile(globalspace), pathname);
    }

    public FileObject resolveFile(String dirPath, String pathname) throws FileSystemException {
        return fsm.resolveFile(dirPath + (dirPath.endsWith(File.separator) ? "" : File.separator) + pathname);

    }

    public FileObject createFile(String pathname) throws FileSystemException {
        FileObject fo = fsm.resolveFile(pathname);
        if (!fo.exists()) {
            fo.createFile();
        }
        return fo;
    }

    public static ListFile list(FileObject fo) throws FileSystemException {
        ListFile list = new ListFile();
        List<String> dirList = Lists.newArrayList();
        List<String> fileList = Lists.newArrayList();
        for (FileObject child : fo.getChildren()) {
            FileType type = child.getType();
            switch (type) {
            case FOLDER:
                dirList.add(baseName(child));
                break;
            case FILE:
                fileList.add(baseName(child));
                break;
            default:
                throw new RuntimeException("Unknow : " + type);
            }
        }
        list.setDirectories(dirList);
        list.setFiles(fileList);
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
        List<FileObject> files = Lists.newArrayList();
        FileSelector selector = (isNullOrEmpty(includes) && isNullOrEmpty(excludes)) ? new AllFilesSelector()
                : new RegexFileSelector(includes, excludes);
        root.findFiles(selector, true, files);
        return files;
    }

    public static void copy(InputStream is, FileObject outFile) throws IOException {
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
            return new FileSystem(schedulerProxy.getUserSpaceURIs().get(0), schedulerProxy
                    .getGlobalSpaceURIs().get(0), VFS.getManager());
        }
    }

    private static class AllFilesSelector implements FileSelector {
        @Override
        public boolean includeFile(FileSelectInfo selInfo) throws Exception {
            return FileType.FILE == selInfo.getFile().getName().getType();
        }

        @Override
        public boolean traverseDescendents(FileSelectInfo arg0) throws Exception {
            return true;
        }
    }

    private static class RegexFileSelector implements FileSelector {

        private List<String> includes;
        private List<String> excludes;

        private RegexFileSelector(List<String> includes, List<String> excludes) {
            this.includes = includes;
            this.excludes = excludes;
        }

        @Override
        public boolean includeFile(FileSelectInfo info) throws Exception {
            return !matches(info, excludes) && matches(info, includes);
        }

        @Override
        public boolean traverseDescendents(FileSelectInfo selInfo) throws Exception {
            return true;
        }

        private boolean matches(FileSelectInfo selInfo, List<String> exprs) {
            boolean match = false;
            if (exprs == null || exprs.isEmpty()) {
                // match = false;
            } else if (exprs.contains(fileName(selInfo))) {
                match = true;
            } else {
                for (String expr : exprs) {
                    if (Pattern.matches(expr, filePath(selInfo))) {
                        match = true;
                        break;
                    }
                }
            }
            return match;
        }

        private String fileName(FileSelectInfo selInfo) {
            return selInfo.getFile().getName().getBaseName();
        }

        private String filePath(FileSelectInfo selInfo) {
            return selInfo.getFile().getName().getPath();
        }

    }
}
