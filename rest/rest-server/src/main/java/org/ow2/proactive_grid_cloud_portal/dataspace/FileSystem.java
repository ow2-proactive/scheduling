/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.dataspace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.vfs2.*;
import org.objectweb.proactive.extensions.dataspaces.api.UserCredentials;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;


public class FileSystem {

    public static final String X_PROACTIVE_DS_TYPE = "x-proactive-ds-type";

    public static final String X_PROACTIVE_DS_PERMISSIONS = "x-proactive-ds-permissions";

    private static final Collator COLLATOR = Collator.getInstance(Locale.getDefault());

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
        FileObject answer = fsm.resolveFile(dirPath + (dirPath.endsWith(File.separator) ? "" : File.separator) +
                                            pathname);
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

    public static ListFile list(FileObject fo, List<String> includes, List<String> excludes)
            throws FileSystemException {
        fo.refresh();
        ListFile answer = new ListFile();
        List<String> dirList = Lists.newArrayList();
        List<String> fileList = Lists.newArrayList();
        List<String> fullList = Lists.newArrayList();
        List<FileObject> foundFileObjects = new LinkedList<>();
        if (isNullOrEmpty(includes) && isNullOrEmpty(excludes)) {
            fo.findFiles(Selectors.SELECT_CHILDREN, false, foundFileObjects);
        } else {
            FileSelector selector = new org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector(includes,
                                                                                                                excludes);
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
        Collections.sort(dirList, filenameComparator());
        Collections.sort(fileList, filenameComparator());
        Collections.sort(fullList, filenameComparator());
        answer.setDirectoryListing(dirList);
        answer.setFileListing(fileList);
        answer.setFullListing(fullList);
        return answer;
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

    private static void fillDirProps(FileObject fo, Map<String, Object> properties) throws FileSystemException {
        properties.put(X_PROACTIVE_DS_TYPE, "DIRECTORY");
        properties.put("Last-Modified", new Date(fo.getContent().getLastModifiedTime()));
        properties.put(X_PROACTIVE_DS_PERMISSIONS, getPermissionsString(fo));
    }

    private static String getPermissionsString(FileObject fo) throws FileSystemException {
        String permissionString = "";
        if (fo.isReadable()) {
            permissionString += "r";
        } else {
            permissionString += "-";
        }
        if (fo.isWriteable()) {
            permissionString += "w";
        } else {
            permissionString += "-";
        }
        if (fo.isExecutable()) {
            permissionString += "x";
        } else {
            permissionString += "-";
        }
        return permissionString;
    }

    private static void fillFileProps(FileObject fo, Map<String, Object> properties) throws FileSystemException {
        properties.put(X_PROACTIVE_DS_TYPE, "FILE");
        properties.put(HttpHeaders.LAST_MODIFIED, new Date(fo.getContent().getLastModifiedTime()));
        properties.put(HttpHeaders.CONTENT_TYPE, contentType(fo));
        properties.put(HttpHeaders.CONTENT_LENGTH, fo.getContent().getSize());
        properties.put(X_PROACTIVE_DS_PERMISSIONS, getPermissionsString(fo));
    }

    public static List<FileObject> findFiles(FileObject root, List<String> includes, List<String> excludes)
            throws FileSystemException {
        root.refresh();
        List<FileObject> files = Lists.newArrayList();
        FileSelector selector = (isNullOrEmpty(includes) &&
                                 isNullOrEmpty(excludes)) ? new AllFilesSelector()
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

    private static Comparator<String> filenameComparator() {
        return (s1, s2) -> {
            int lowerCaseCompare = COLLATOR.compare(s1.toLowerCase(), s2.toLowerCase());
            if (lowerCaseCompare != 0) {
                return lowerCaseCompare;
            }
            // lowercase comes before uppercase
            for (int i = 0; i < s1.length(); i++) {
                if (Character.isLowerCase(s1.charAt(i)) && Character.isUpperCase(s2.charAt(i))) {
                    return -1;
                } else if (Character.isUpperCase(s1.charAt(i)) && Character.isLowerCase(s2.charAt(i))) {
                    return 1;
                }
            }
            return 0;
        };
    }

    static class Builder {
        public static FileSystem create(Session session)
                throws FileSystemException, NotConnectedException, PermissionException {
            CredData credData = session.getCredData();
            return new FileSystem(session.getScheduler().getUserSpaceURIs().get(0),
                                  session.getScheduler().getGlobalSpaceURIs().get(0),
                                  VFSFactory.createDefaultFileSystemManager(new UserCredentials(credData.getLogin(),
                                                                                                credData.getPassword(),
                                                                                                credData.getDomain(),
                                                                                                credData.getKey())));
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
