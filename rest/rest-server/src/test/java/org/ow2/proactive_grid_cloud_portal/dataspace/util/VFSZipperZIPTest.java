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

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive_grid_cloud_portal.dataspace.FileSystem;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;


/**
 * Unit tests related to {@link org.ow2.proactive_grid_cloud_portal.dataspace.util.VFSZipper.ZIP}.
 *
 * @author ActiveEon Team
 */
public class VFSZipperZIPTest {

    private static final String DEFAULT_ARCHIVE_NAME = "archive.zip";

    private static final int HIERARCHY_DEPTH = 10;

    private static final int HIERARCHY_CODEPOINT_ROOT = 'a';

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path archivePath;

    private Path outputPath;

    private FileSystemManager fileSystemManager;

    private Path temporaryPath;

    @Before
    public void setUp() throws IOException {
        fileSystemManager = VFS.getManager();
        temporaryPath = temporaryFolder.newFolder().toPath();

        archivePath = getArchivePath();
        outputPath = getOutputPath();
    }

    @Test
    public void testZipFolderWithFileHierarchy() throws IOException {
        createFileHierarchy(temporaryPath);
        testZip(temporaryPath);
        assertZipWithFileHierarchy(archivePath);
    }

    @Test
    public void testZipEmptyFolder() throws IOException {
        testZip(temporaryPath);
        assertThat(Files.exists(archivePath)).isTrue();
    }

    @Test
    public void testUnzipArchiveWithFileHierarchy() throws IOException {
        createFileHierarchy(temporaryPath);

        // Zips using third-party library that is tested and known to work as expected
        ZipUtil.pack(temporaryPath.toFile(), archivePath.toFile());

        testUnzip(outputPath);
    }

    @Test
    public void testUnzipEmptyContent() throws IOException, InterruptedException {
        Files.createFile(archivePath);

        FileSystemManager fsManager = VFS.getManager();

        VFSZipper.ZIP.unzip(Files.newInputStream(archivePath), fsManager.resolveFile(outputPath.toUri()));

        assertThat(outputPath.toFile().listFiles()).isNull();
    }

    @Test
    public void testZipUnzipZipUnzip() throws IOException, InterruptedException {
        Path pathToZip = temporaryPath;
        Path resultPath = outputPath;

        createFileHierarchy(temporaryPath);

        for (int i = 0; i < 2; i++) {
            testZip(pathToZip);
            assertZipWithFileHierarchy(archivePath);

            testUnzip(resultPath);
            assertUnzippedFileHierarchy(resultPath);

            pathToZip = resultPath;
            resultPath = temporaryPath.resolve("output" + (i + 1));

            Files.delete(archivePath);
        }
    }

    /**
     * Creates the following file hierarchy from the specified root:
     * .
     * ├── a.txt
     * └── folder0
     *     ├── b.txt
     *     └── folder...
     *         ├── ....txt
     *         └── folderN
     *             └── N_{ascii}.txt
     * <p>
     * where N is the equals to HIERARCHY_DEPTH.
     */
    private void createFileHierarchy(Path root) throws IOException {
        int codepoint = HIERARCHY_CODEPOINT_ROOT;

        createFile(root, codepoint);

        for (int i = 0; i < HIERARCHY_DEPTH; i++) {
            codepoint++;
            Path folder = createFolder(root, "folder" + i);
            createFile(folder, codepoint);
            root = folder;
        }
    }

    private void testZip(Path pathToZip) throws IOException {
        List<FileObject> files = FileSystem.findFiles(fileSystemManager.resolveFile(pathToZip.toUri()), null, null);

        VFSZipper.ZIP.zip(fileSystemManager.resolveFile(pathToZip.toUri()), files, Files.newOutputStream(archivePath));
    }

    private void assertZipWithFileHierarchy(Path archivePath) {
        final int[] nbEntries = { 0 };

        // Reads ZIP content using a third-party library
        ZipUtil.iterate(archivePath.toFile(), new ZipEntryCallback() {
            @Override
            public void process(InputStream in, ZipEntry zipEntry) throws IOException {
                nbEntries[0]++;
            }
        });

        assertThat(nbEntries[0]).isEqualTo(HIERARCHY_DEPTH + 1);
    }

    private void testUnzip(Path resultPath) throws IOException {
        VFSZipper.ZIP.unzip(Files.newInputStream(archivePath), fileSystemManager.resolveFile(resultPath.toUri()));

        assertUnzippedFileHierarchy(outputPath);
    }

    private void assertUnzippedFileHierarchy(Path root) {
        int codepoint = HIERARCHY_CODEPOINT_ROOT;

        Path file = getFile(root, codepoint);
        assertThat(Files.exists(file)).isTrue();

        for (int i = 0; i < HIERARCHY_DEPTH; i++) {
            root = root.resolve("folder" + i);
            assertThat(Files.exists(root)).isTrue();
        }
    }

    private Path getArchivePath() {
        return temporaryPath.getParent().resolve(DEFAULT_ARCHIVE_NAME);
    }

    private Path getOutputPath() throws IOException {
        return temporaryPath.resolve("output");
    }

    private Path createFile(Path root, int codepoint) throws IOException {
        return Files.createFile(getFile(root, codepoint));
    }

    private Path getFile(Path root, int codepoint) {
        return root.resolve(Character.toString((char) codepoint) + ".txt");
    }

    private Path createFolder(Path root, String name) throws IOException {
        Path folder = root.resolve(name);
        Files.createDirectories(folder);
        return folder;
    }

}
