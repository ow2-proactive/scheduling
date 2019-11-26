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
package org.ow2.proactive_grid_cloud_portal.scheduler.client.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.common.io.Files;


public class Zipper {

    private static final Logger logger = Logger.getLogger(Zipper.class);

    private static byte[] MAGIC = { 'P', 'K', 0x3, 0x4 };

    private Zipper() {
    }

    public static class GZIP {

        public static void zip(File file, OutputStream os) throws IOException {
            checkNotNull(file);
            checkNotNull(os);
            try (InputStream inputStream = new FileInputStream(file)) {
                GZIP.zip(inputStream, os);
            }
        }

        public static void zip(InputStream is, OutputStream os) throws IOException {
            Closer closer = Closer.create();
            closer.register(is);
            try {
                GZIPOutputStream zos = new GZIPOutputStream(os);
                closer.register(zos);
                ByteStreams.copy(is, zos);
            } catch (IOException ioe) {
                throw closer.rethrow(ioe);
            } finally {
                closer.close();
            }
        }

        public static void unzip(InputStream is, File file) throws IOException {
            checkNotNull(is);
            checkNotNull(file);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                GZIP.unzip(is, outputStream);
            }
        }

        public static void unzip(InputStream is, OutputStream os) throws IOException {
            Closer closer = Closer.create();
            closer.register(os);
            try {
                GZIPInputStream gis = new GZIPInputStream(is);
                closer.register(gis);
                ByteStreams.copy(gis, os);
            } catch (IOException ioe) {
                throw closer.rethrow(ioe);
            } finally {
                closer.close();
            }
        }
    }

    public static class ZIP {

        private static List<File> findFiles(File root, FileSelector selector) {
            List<File> listFiles = new ArrayList<>();
            try {
                FileObject rootObject = VFS.getManager().toFileObject(root);
                FileObject[] fos = rootObject.findFiles(selector);

                for (FileObject fo : fos) {
                    listFiles.add(new File(fo.getName().getPath()));
                }
            } catch (Exception e) {
                logger.error("An error occurred while zipping files: ", e);
            }

            return listFiles;
        }

        private static ImmutableList<File> filterNotEmpty(File root, List<String> includes, List<String> excludes) {
            FileSelector fileSelector = new FileSelector();
            fileSelector.addIncludes(includes);
            fileSelector.addExcludes(excludes);

            return (ImmutableList<File>) findFiles(root, fileSelector);
        }

        private static ImmutableList<File> filterEmpty(File root) {
            FluentIterable<File> fi = Files.fileTreeTraverser().postOrderTraversal(root);
            return fi.filter(new FilesOnlyPredicate()).toList();
        }

        public static void zip(File root, List<String> includes, List<String> excludes, OutputStream os)
                throws IOException {
            logger.trace("Includes list : " + includes.toString());
            logger.trace("Excludes list : " + excludes.toString());
            checkNotNull(root);
            checkNotNull(os);
            ImmutableList<File> fileList = nullOrEmpty(includes) && nullOrEmpty(excludes) ? filterEmpty(root)
                                                                                          : filterNotEmpty(root,
                                                                                                           includes,
                                                                                                           excludes);
            logger.trace("Zipping files :" + fileList);
            zipFiles(fileList, root.getAbsolutePath(), os);
        }

        private static boolean nullOrEmpty(List<String> strings) {
            return strings == null || strings.size() == 0;
        }

        private static void zipFiles(List<File> files, String basepath, OutputStream os) throws IOException {
            Closer closer = Closer.create();
            try {
                ZipOutputStream zos = new ZipOutputStream(os);
                closer.register(zos);
                for (File file : files) {
                    FileInputStream inputStream = new FileInputStream(file);
                    closer.register(inputStream);
                    writeZipEntry(zipEntry(basepath, file), inputStream, zos);
                }
            } catch (IOException ioe) {
                throw closer.rethrow(ioe);
            } finally {
                closer.close();
            }
        }

        public static void writeZipEntry(ZipEntry zipEntry, InputStream is, ZipOutputStream zos) throws IOException {
            Closer closer = Closer.create();
            closer.register(is);
            try {
                logger.trace("Adding zip entry" + zipEntry.toString());
                zos.putNextEntry(zipEntry);
                ByteStreams.copy(is, zos);
                zos.flush();
            } catch (IOException ioe) {
                throw closer.rethrow(ioe);
            } finally {
                closer.close();
            }
        }

        public static void unzip(InputStream is, File outFile) throws IOException {
            Closer closer = Closer.create();
            try {
                ZipInputStream zis = new ZipInputStream(is);
                closer.register(zis);

                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    File entryFile = new File(outFile, zipEntry.getName());
                    File entryContainer = entryFile.getParentFile();
                    if (!entryContainer.exists()) {
                        entryContainer.mkdirs();
                    }
                    if (!entryFile.isDirectory()) {
                        FileOutputStream outputStream = new FileOutputStream(entryFile);
                        closer.register(outputStream);
                        Zipper.ZIP.unzipEntry(zis, outputStream);
                    }
                    zipEntry = zis.getNextEntry();
                }
            } catch (IOException ioe) {
                throw closer.rethrow(ioe);
            } finally {
                closer.close();
            }
        }

        public static void unzipEntry(ZipInputStream zis, OutputStream os) throws IOException {
            Closer closer = Closer.create();
            closer.register(os);
            try {
                ByteStreams.copy(zis, os);
            } catch (IOException ioe) {
                throw closer.rethrow(ioe);
            } finally {
                closer.close();
            }
        }

        private static ZipEntry zipEntry(String basepath, File file) {
            String name = (Strings.isNullOrEmpty(basepath) ||
                           basepath.equals(file.getAbsolutePath())) ? file.getPath()
                                                                    : file.getAbsolutePath()
                                                                          .substring(basepath.length() + 1);
            return new ZipEntry(name);
        }
    }

    public static boolean isZipFile(File file) throws FileNotFoundException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return isZipFile(inputStream);
        } catch (IOException e) {
            throw new FileNotFoundException("Error when reading file " + file + " " + e.getMessage());
        }
    }

    public static boolean isZipFile(InputStream is) {
        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }
        boolean isZipStream = true;
        try {
            is.mark(MAGIC.length);
            for (int i = 0; i < MAGIC.length; i++) {
                if (MAGIC[i] != (byte) is.read()) {
                    isZipStream = false;
                    break;
                }
            }
            is.reset();
        } catch (IOException ioe) {
            isZipStream = false;
        }
        return isZipStream;
    }

    private static class FilesOnlyPredicate implements Predicate<File> {
        @Override
        public boolean apply(File file) {
            boolean answer = !file.isDirectory();
            if (logger.isTraceEnabled()) {
                logger.trace("Analysing file " + file + " : " + answer);
            }
            return !file.isDirectory();
        }
    }
}
