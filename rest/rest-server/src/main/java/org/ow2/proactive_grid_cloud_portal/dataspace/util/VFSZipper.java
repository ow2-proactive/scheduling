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
package org.ow2.proactive_grid_cloud_portal.dataspace.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.ow2.proactive_grid_cloud_portal.dataspace.FileSystem;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.utils.Zipper;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;


public class VFSZipper {

    private VFSZipper() {
    }

    public static class GZIP {

        public static void zip(FileObject fo, OutputStream os) throws FileSystemException, IOException {
            Zipper.GZIP.zip(fo.getContent().getInputStream(), os);
        }

        public static void unzip(InputStream is, FileObject outputFile) throws FileSystemException, IOException {
            Zipper.GZIP.unzip(is, outputFile.getContent().getOutputStream());
        }
    }

    public static class ZIP {

        public static void zip(FileObject root, List<FileObject> children, OutputStream os) throws IOException {
            checkNotNull(root);
            checkNotNull(children);
            checkNotNull(os);
            String basePath = root.getName().getPath();
            Closer closer = Closer.create();
            try {
                ZipOutputStream zos = new ZipOutputStream(os);
                closer.register(zos);
                if (children.size() == 1) {
                    FileObject ffo = children.get(0);
                    Zipper.ZIP.writeZipEntry(new ZipEntry(ffo.getName().getBaseName()),
                                             ffo.getContent().getInputStream(),
                                             zos);
                } else {
                    for (FileObject ffo : children) {
                        Zipper.ZIP.writeZipEntry(zipEntry(basePath, ffo), ffo.getContent().getInputStream(), zos);
                    }
                }
            } catch (IOException ioe) {
                throw closer.rethrow(ioe);
            } finally {
                closer.close();
            }
        }

        private static ZipEntry zipEntry(String basePath, FileObject file) {
            String name = file.getName().getPath().substring(basePath.length() + 1);
            return new ZipEntry(name);
        }

        public static void unzip(InputStream is, FileObject outfileObj) throws FileSystemException, IOException {
            Closer closer = Closer.create();
            try {
                ZipInputStream zis = new ZipInputStream(is);
                closer.register(zis);
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    FileObject entryFile = outfileObj.resolveFile(zipEntry.getName());

                    if (zipEntry.isDirectory()) {
                        entryFile.createFolder();
                    } else {
                        if (!entryFile.exists()) {
                            entryFile.createFile();
                        }
                        Zipper.ZIP.unzipEntry(zis, entryFile.getContent().getOutputStream());
                    }

                    zipEntry = zis.getNextEntry();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw closer.rethrow(ioe);
            } finally {
                closer.close();
            }
        }
    }

    public static void zip(FileObject file, OutputStream out) throws IOException {
        Closer closer = Closer.create();
        try {
            closer.register(out);
            InputStream in = file.getContent().getInputStream();
            closer.register(in);
            ByteStreams.copy(in, out);
        } catch (IOException ioe) {
            throw closer.rethrow(ioe);
        } finally {
            closer.close();
        }
    }

    public static void zip(FileObject root, List<FileObject> files, OutputStream out) throws IOException {
        String basePath = root.getName().getPath();
        Closer closer = Closer.create();
        try {
            ZipOutputStream zos = new ZipOutputStream(out);
            closer.register(zos);
            for (FileObject fileToCopy : files) {
                ZipEntry zipEntry = zipEntry(basePath, fileToCopy);
                zos.putNextEntry(zipEntry);
                copyFileContents(fileToCopy, zos);
                zos.flush();
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public static void unzip(InputStream is, FileObject file) throws IOException {
        Closer closer = Closer.create();
        closer.register(is);
        try {
            OutputStream os = file.getContent().getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(os);
            closer.register(zos);
            ByteStreams.copy(is, zos);
        } catch (IOException ioe) {
            throw closer.rethrow(ioe);
        } finally {
            closer.close();
        }
    }

    public static boolean isZipFile(FileObject fo) throws FileSystemException {
        return Zipper.isZipFile(fo.getContent().getInputStream());
    }

    private static ZipEntry zipEntry(String basePath, FileObject fo) {
        String entryPath = fo.getName().getPath().substring(basePath.length() + 1);
        return new ZipEntry(entryPath);
    }

    private static void copyFileContents(FileObject fo, ZipOutputStream zos) throws IOException {
        Closer closer = Closer.create();
        try {
            InputStream inputStream = fo.getContent().getInputStream();
            closer.register(inputStream);
            ByteStreams.copy(inputStream, zos);
        } catch (IOException ioe) {
            throw closer.rethrow(ioe);
        } finally {
            closer.close();
        }
    }

}
