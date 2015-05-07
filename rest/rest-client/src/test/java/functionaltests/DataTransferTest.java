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
package functionaltests;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.rest.ds.*;
import org.ow2.proactive_grid_cloud_portal.common.FileType;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Random;

import static functionaltests.RestFuncTHelper.getRestServerUrl;
import static org.junit.Assert.assertFalse;
import static org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.Dataspace.USER;


public class DataTransferTest extends AbstractRestFuncTestCase {

    private static final int FILE_SIZE = 100;

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        init(DataTransferTest.class.getSimpleName());
    }

    @Test
    public void testUploadSingleFile() throws Exception {
        IDataSpaceClient client = clientInstance();
        File tmpFile = tmpDir.newFile("tmpfile.tmp");
        Files.write(randomFileContents(), tmpFile);
        LocalFileSource source = new LocalFileSource(tmpFile);
        RemoteDestination dest = new RemoteDestination(USER, "testUploadSingleFile/tmpfile.tmp");
        assertTrue(client.upload(source, dest));
        String destDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File destFile = new File(destDirPath, "testUploadSingleFile/tmpfile.tmp");
        assertTrue(Files.equal(tmpFile, destFile));
    }

    @Test
    public void testUploadAllFilesInDirectory() throws Exception {
        // entire folder
        File tempTextFile = tmpDir.newFile("tempFile.txt");
        Files.write("some text ...".getBytes(), tempTextFile);

        File tempDir = tmpDir.newFolder("tempDir");
        File tempFile = new File(tempDir, "tempFile.tmp");
        Files.createParentDirs(tempFile);
        Files.write(randomFileContents(), tempFile);

        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpDir.getRoot());
        RemoteDestination dest = new RemoteDestination(USER, "testUploadAllFilesInDirectory");

        assertTrue(client.upload(source, dest));

        String destRootUri = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        assertTrue(Files.equal(tempTextFile, new File(destRootUri,
                "testUploadAllFilesInDirectory/tempFile.txt")));
        assertTrue(Files.equal(tempFile, new File(destRootUri,
            "testUploadAllFilesInDirectory/tempDir/tempFile.tmp")));
    }

    @Test
    public void testUploadSelectedFilesUsingGlobPattern() throws Exception {
        File tempTextFile = tmpDir.newFile("tempFile.txt");
        Files.write("some text ...".getBytes(), tempTextFile);

        File tempDir = tmpDir.newFolder("tempDir");
        File tempFile = new File(tempDir, "tempFile.tmp");
        Files.createParentDirs(tempFile);
        Files.write(randomFileContents(), tempFile);

        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpDir.getRoot());
        source.setIncludes("*.txt");
        RemoteDestination dest = new RemoteDestination(USER, "testUploadSelectedFilesUsingGlobPattern");
        assertTrue(client.upload(source, dest));

        String destRootUri = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File[] destRootFiles = new File(destRootUri, "testUploadSelectedFilesUsingGlobPattern").listFiles();
        assertEquals(1, destRootFiles.length);
        assertTrue(Files.equal(tempTextFile, destRootFiles[0]));
    }

    @Test
    public void testUploadSelectedFilesUsingFilenames() throws Exception {
        File tempTextFile = tmpDir.newFile("tempFile.txt");
        Files.write("some text ...".getBytes(), tempTextFile);

        File tempDir = tmpDir.newFolder("tempDir");
        File tempFile = new File(tempDir, "tempFile.tmp");
        Files.createParentDirs(tempFile);
        Files.write(randomFileContents(), tempFile);

        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpDir.getRoot());
        source.setIncludes("**/tempFile.tmp");
        RemoteDestination dest = new RemoteDestination(USER, "testUploadSelectedFilesUsingFilenames");
        assertTrue(client.upload(source, dest));

        String destRootUri = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File[] destRootFiles = new File(destRootUri, "testUploadSelectedFilesUsingFilenames")
                .listFiles();
        assertEquals(1, destRootFiles.length);
        assertTrue(Files.equal(tempFile, destRootFiles[0]));
    }

    @Test
    public void testDownloadFile() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File srcFile = new File(srcDirPath, "tmpfile.tmp");
        if (srcFile.exists()) {
            assertTrue(srcFile.delete());
        }
        Files.write(randomFileContents(), srcFile);

        File tmpFile = tmpDir.newFile("tmpfile.tmp");
        if (tmpFile.exists()) {
            assertTrue(tmpFile.delete());
        }

        IDataSpaceClient client = clientInstance();
        RemoteSource source = new RemoteSource(USER, "tmpfile.tmp");
        LocalDestination dest = new LocalDestination(tmpFile);
        assertTrue(client.download(source, dest));
        assertTrue(Files.equal(srcFile, tmpFile));
    }

    @Test
    public void testDownloadAllFilesInDirectory() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File srcDir = new File(srcDirPath, "testDownloadAllFilesInDirectory");
        if (srcDir.exists()) {
            FileUtils.deleteDirectory(srcDir);
        }

        File srcTextFile = new File(srcDir, "tempFile.txt");
        Files.createParentDirs(srcTextFile);
        Files.write("some text ...".getBytes(), srcTextFile);

        File srcTempDir = new File(srcDir, "tempDir");
        File srcTempFile = new File(srcTempDir, "tempFile.tmp");
        Files.createParentDirs(srcTempFile);
        Files.write(randomFileContents(), srcTempFile);

        File destTempDir = tmpDir.newFolder("tempDir");
        RemoteSource source = new RemoteSource(USER, "testDownloadAllFilesInDirectory");
        LocalDestination dest = new LocalDestination(destTempDir);
        IDataSpaceClient client = clientInstance();

        assertTrue(client.download(source, dest));

        assertTrue(Files.equal(srcTextFile, new File(destTempDir, "tempFile.txt")));
        assertTrue(Files.equal(srcTempFile, new File(destTempDir, "tempDir/tempFile.tmp")));
    }

    @Test
    public void testDownloadSelectedFilesUsingGlobPattern() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File srcDir = new File(srcDirPath, "testDownloadAllFilesInDirectory");
        if (srcDir.exists()) {
            FileUtils.deleteDirectory(srcDir);
        }

        File srcTextFile = new File(srcDir, "tempFile.txt");
        Files.createParentDirs(srcTextFile);
        Files.write("some text ...".getBytes(), srcTextFile);

        File srcTempDir = new File(srcDir, "tempDir");
        File srcTempFile = new File(srcTempDir, "tempFile.tmp");
        Files.createParentDirs(srcTempFile);
        Files.write(randomFileContents(), srcTempFile);

        File destTempDir = tmpDir.newFolder("tempDir");
        RemoteSource source = new RemoteSource(USER, "testDownloadAllFilesInDirectory");
        source.setIncludes("*.txt");
        LocalDestination dest = new LocalDestination(destTempDir);
        IDataSpaceClient client = clientInstance();

        assertTrue(client.download(source, dest));

        File[] listFiles = destTempDir.listFiles();

        assertEquals(1, listFiles.length);
        assertTrue(Files.equal(srcTextFile, listFiles[0]));
    }

    @Test
    public void testListFiles() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File srcDir = new File(srcDirPath, "testListFiles");
        if (srcDir.exists()) {
            FileUtils.deleteDirectory(srcDir);
        }

        File srcTextFile = new File(srcDir, "tempFile.txt");
        Files.createParentDirs(srcTextFile);
        Files.write("some text ...".getBytes(), srcTextFile);

        File srcTempDir = new File(srcDir, "tempDir");
        File srcTempFile = new File(srcTempDir, "tempFile.tmp");
        Files.createParentDirs(srcTempFile);
        Files.write(randomFileContents(), srcTempFile);

        RemoteSource source = new RemoteSource(USER, "testListFiles");
        IDataSpaceClient client = clientInstance();

        ListFile listFile = client.list(source);

        List<String> directories = listFile.getDirectories();
        assertEquals(1, directories.size());
        assertEquals("tempDir", directories.get(0));

        List<String> files = listFile.getFiles();
        assertEquals(1, files.size());
        assertEquals("tempFile.txt", files.get(0));
    }

    @Test
    public void testDeleteFile() throws Exception {
        URI srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0));
        File srcFile = new File(new File(srcDirPath), "tempFile.tmp");
        if (srcFile.exists()) {
            assertTrue(srcFile.delete());
        }
        Files.write(randomFileContents(), srcFile);

        RemoteSource source = new RemoteSource(USER, "tempFile.tmp");
        IDataSpaceClient client = clientInstance();
        assertTrue(client.delete(source));
        assertFalse((new File(new File(srcDirPath), "tempFile.tmp")).exists());
    }

    @Test
    public void testCreateFile() throws Exception {
        URI srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0));

        String filename = "tempFile.tmp";

        RemoteSource source = new RemoteSource(USER, filename);
        source.setType(FileType.FILE);

        IDataSpaceClient client = clientInstance();
        assertTrue(client.create(source));

        File expectedFile = new File(srcDirPath.getPath(), filename);

        assertTrue(expectedFile.exists());
        assertTrue(expectedFile.isFile());
    }

    @Test
    public void testCreateFolder() throws Exception {
        URI srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0));

        String folderName = "testcreatefolder";

        RemoteSource source = new RemoteSource(USER, folderName);
        source.setType(FileType.FOLDER);

        IDataSpaceClient client = clientInstance();
        assertTrue(client.create(source));

        File expectedFile = new File(srcDirPath.getPath(), folderName);

        assertTrue(expectedFile.exists());
        assertTrue(expectedFile.isDirectory());
    }

    @Test(expected = Exception.class)
    public void testCreateFolderWithoutSpecifyingFileType() throws Exception {
        String folderName = "testcreatefolder";

        RemoteSource source = new RemoteSource(USER, folderName);
        // file is not specified

        IDataSpaceClient client = clientInstance();
        assertTrue(client.create(source));
    }

    private byte[] randomFileContents() {
        byte[] fileContents = new byte[FILE_SIZE];
        new Random().nextBytes(fileContents);
        return fileContents;
    }

    private IDataSpaceClient clientInstance() throws Exception {
        DataSpaceClient client = new DataSpaceClient();
        client.init(getRestServerUrl(), getLogin(), getPassword());
        return client;
    }

}
