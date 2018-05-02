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
package functionaltests;

import static functionaltests.RestFuncTHelper.getRestServerUrl;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.Dataspace.USER;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.rest.ds.*;
import org.ow2.proactive_grid_cloud_portal.common.FileType;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;

import com.google.common.io.Files;


public class DataTransferTest extends AbstractRestFuncTestCase {

    private static final int FILE_SIZE = 100;

    public static final String TEMP_FILE_TXT_NAME = "tempFile.txt";

    public static final String TEMP_DIR_NAME = "tempDir";

    public static final String TEMP_DIR2_NAME = "tempDir2";

    public static final String TEMP_FILE_TMP_NAME = "tempFile.tmp";

    public static final String TEMP_FILE_ZIP_NAME = "test1.zip";

    public static final String TEMP_FILE_TGZ_NAME = "test1.tgz";

    public static final String TEMP_FILE_TMP_PATH = TEMP_DIR_NAME + "/" + TEMP_FILE_TMP_NAME;

    static URL zipFileUrl = DataTransferTest.class.getResource("/functionaltests/files/test1.zip");

    static URL tgzFileUrl = DataTransferTest.class.getResource("/functionaltests/files/test2.tgz");

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
    }

    @Test
    public void testUploadSingleFile() throws Exception {

        String testFolderName = "testUploadSingleFile";
        System.out.println(testFolderName);

        File tmpFile = tmpDir.newFile(TEMP_FILE_TMP_NAME);
        Files.write(randomFileContents(), tmpFile);

        // use standard client
        IDataSpaceClient client = clientInstance();
        LocalFileSource source = new LocalFileSource(tmpFile);
        RemoteDestination dest = new RemoteDestination(USER, testFolderName + "/" + TEMP_FILE_TMP_NAME);

        assertTrue(client.upload(source, dest));
        String destDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File destFile = new File(destDirPath, testFolderName + "/" + TEMP_FILE_TMP_NAME);
        assertTrue(Files.equal(tmpFile, destFile));

        // use RemoteSpace API
        FileUtils.deleteQuietly(destFile);
        client.getUserSpace().pushFile(tmpFile, testFolderName + "/" + TEMP_FILE_TMP_NAME);
        assertTrue(Files.equal(tmpFile, destFile));
    }

    @Test
    public void testUploadSingleFileInDir() throws Exception {

        String testFolderName = "testUploadSingleFileInDir";
        System.out.println(testFolderName);

        String subFolderName = "subFolder";

        File tmpFolder = tmpDir.newFolder(testFolderName);
        File subFolder = new File(tmpFolder, subFolderName);
        subFolder.mkdirs();
        File tmpFile = new File(subFolder, TEMP_FILE_TMP_NAME);
        Files.write(randomFileContents(), tmpFile);

        // use standard client
        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpFolder);
        source.setIncludes(subFolder.getName() + "/" + TEMP_FILE_TMP_NAME);
        RemoteDestination dest = new RemoteDestination(USER, testFolderName);

        assertTrue(client.upload(source, dest));
        String destDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File destFile = new File(destDirPath, testFolderName + "/" + subFolderName + "/" + TEMP_FILE_TMP_NAME);
        assertTrue(destFile.exists());
        assertTrue(Files.equal(tmpFile, destFile));

        // use RemoteSpace API
        FileUtils.deleteQuietly(destFile);
        client.getUserSpace().pushFile(tmpFile, testFolderName + "/" + subFolderName + "/" + TEMP_FILE_TMP_NAME);
        assertTrue(destFile.exists());
        assertTrue(Files.equal(tmpFile, destFile));
    }

    @Test
    public void testUploadTgzFile() throws Exception {
        testUploadArchiveFile(TEMP_FILE_ZIP_NAME, zipFileUrl, "testUploadZipFile");
    }

    @Test
    public void testUploadZipFile() throws Exception {
        testUploadArchiveFile(TEMP_FILE_TGZ_NAME, tgzFileUrl, "testUploadTgzFile");
    }

    private void testUploadArchiveFile(String archiveFileName, URL archiveFileSource, String testFolderName)
            throws Exception {
        File tmpZipFile = tmpDir.newFile(archiveFileName);
        FileUtils.copyInputStreamToFile(archiveFileSource.openStream(), tmpZipFile);

        // use standard client
        IDataSpaceClient client = clientInstance();
        LocalFileSource source = new LocalFileSource(tmpZipFile);
        RemoteDestination dest = new RemoteDestination(USER, testFolderName + "/" + archiveFileName);

        assertTrue(client.upload(source, dest));
        String destDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File destFile = new File(destDirPath, testFolderName + "/" + archiveFileName);
        assertTrue(Files.equal(tmpZipFile, destFile));

        // use RemoteSpace API
        FileUtils.deleteQuietly(destFile);
        client.getUserSpace().pushFile(tmpZipFile, testFolderName + "/" + archiveFileName);
        assertTrue(Files.equal(tmpZipFile, destFile));
    }

    @Test
    public void testUploadAllFilesInDirectory() throws Exception {
        String testFolderName = "testUploadAllFilesInDirectory";
        System.out.println(testFolderName);
        // entire folder
        TestFilesToUploadCreator testFiles = new TestFilesToUploadCreator().invoke();
        File tempTextFile = testFiles.getTempTextFile();
        File tempFile = testFiles.getTempFile();

        // use standard client
        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpDir.getRoot());
        RemoteDestination dest = new RemoteDestination(USER, testFolderName);

        assertTrue(client.upload(source, dest));
        String destRootUri = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        assertTrue(Files.equal(tempTextFile, new File(destRootUri, testFolderName + "/" + TEMP_FILE_TXT_NAME)));
        assertTrue(Files.equal(tempFile, new File(destRootUri, testFolderName + "/" + TEMP_FILE_TMP_PATH)));

        // use RemoteSpace API
        FileUtils.deleteDirectory(new File(destRootUri, testFolderName));

        client.getUserSpace().pushFile(tmpDir.getRoot(), testFolderName);

        assertTrue(Files.equal(tempTextFile, new File(destRootUri, testFolderName + "/" + TEMP_FILE_TXT_NAME)));
        assertTrue(Files.equal(tempFile, new File(destRootUri, testFolderName + "/" + TEMP_FILE_TMP_PATH)));

    }

    @Test
    public void testUploadSelectedFilesUsingGlobPattern() throws Exception {
        String testFolderName = "testUploadSelectedFilesUsingGlobPattern";
        System.out.println(testFolderName);
        TestFilesToUploadCreator testFiles = new TestFilesToUploadCreator().invoke();
        File tempTextFile = testFiles.getTempTextFile();
        File tempFile = testFiles.getTempFile();

        // use standard client
        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpDir.getRoot());
        source.setIncludes("*.txt");
        RemoteDestination dest = new RemoteDestination(USER, testFolderName);
        assertTrue(client.upload(source, dest));

        String destRootUri = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File[] destRootFiles = new File(destRootUri, testFolderName).listFiles();
        assertEquals(1, destRootFiles.length);
        assertTrue(Files.equal(tempTextFile, destRootFiles[0]));

        // use RemoteSpace API
        FileUtils.deleteDirectory(new File(destRootUri, testFolderName));
        client.getUserSpace().pushFiles(tmpDir.getRoot(), "*.txt", testFolderName);
        destRootFiles = new File(destRootUri, testFolderName).listFiles();
        assertEquals(1, destRootFiles.length);
        assertTrue(Files.equal(tempTextFile, destRootFiles[0]));
    }

    @Test
    public void testUploadSelectedFilesUsingFilenames() throws Exception {

        String testFolderName = "testUploadSelectedFilesUsingFilenames";
        System.out.println(testFolderName);

        TestFilesToUploadCreator testFiles = new TestFilesToUploadCreator().invoke();
        File tempTextFile = testFiles.getTempTextFile();
        File tempFile = testFiles.getTempFile();

        // use standard client
        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpDir.getRoot());
        source.setIncludes("**/" + TEMP_FILE_TMP_NAME);
        RemoteDestination dest = new RemoteDestination(USER, testFolderName);
        assertTrue(client.upload(source, dest));

        String destRootUri = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File[] destRootFiles = new File(destRootUri, testFolderName).listFiles();
        System.out.println("Files in destination: " + Arrays.asList(destRootFiles));
        assertEquals(1, destRootFiles.length);
        assertTrue(destRootFiles[0].isDirectory());
        assertTrue(Files.equal(tempFile, new File(destRootFiles[0], TEMP_FILE_TMP_NAME)));

        // use RemoteSpace API
        FileUtils.deleteDirectory(new File(destRootUri, testFolderName));
        client.getUserSpace().pushFiles(tmpDir.getRoot(), "**/" + TEMP_FILE_TMP_NAME, testFolderName);
        destRootFiles = new File(destRootUri, testFolderName).listFiles();
        assertEquals(1, destRootFiles.length);
        assertTrue(destRootFiles[0].isDirectory());
        assertTrue(Files.equal(tempFile, new File(destRootFiles[0], TEMP_FILE_TMP_NAME)));
    }

    @Test
    public void testDownloadFile() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File srcFile = new File(srcDirPath, TEMP_FILE_TMP_NAME);
        if (srcFile.exists()) {
            assertTrue(srcFile.delete());
        }
        Files.write(randomFileContents(), srcFile);

        File tmpFile = tmpDir.newFile(TEMP_FILE_TMP_NAME);
        if (tmpFile.exists()) {
            assertTrue(tmpFile.delete());
        }

        // use standard client
        IDataSpaceClient client = clientInstance();
        RemoteSource source = new RemoteSource(USER, TEMP_FILE_TMP_NAME);
        LocalDestination dest = new LocalDestination(tmpFile);
        assertTrue(client.download(source, dest));
        assertTrue(Files.equal(srcFile, tmpFile));

        // use RemoteSpace API
        FileUtils.deleteQuietly(tmpFile);
        File downloadedFile = client.getUserSpace().pullFile(TEMP_FILE_TMP_NAME, tmpFile);
        assertTrue(Files.equal(srcFile, downloadedFile));

    }

    @Test
    public void testDownloadZipFile() throws Exception {
        testDownloadArchiveFile(TEMP_FILE_ZIP_NAME, zipFileUrl);

    }

    @Test
    public void testDownloadTgzFile() throws Exception {
        testDownloadArchiveFile(TEMP_FILE_TGZ_NAME, tgzFileUrl);

    }

    private void testDownloadArchiveFile(String archiveFileName, URL archiveSource) throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File srcFile = new File(srcDirPath, archiveFileName);
        if (srcFile.exists()) {
            assertTrue(srcFile.delete());
        }
        FileUtils.copyInputStreamToFile(archiveSource.openStream(), srcFile);

        File tmpFile = tmpDir.newFile(archiveFileName);
        if (tmpFile.exists()) {
            assertTrue(tmpFile.delete());
        }

        // use standard client
        IDataSpaceClient client = clientInstance();
        RemoteSource source = new RemoteSource(USER, archiveFileName);
        LocalDestination dest = new LocalDestination(tmpFile);
        assertTrue(client.download(source, dest));
        assertTrue(Files.equal(srcFile, tmpFile));

        // use RemoteSpace API
        FileUtils.deleteQuietly(tmpFile);
        File downloadedFile = client.getUserSpace().pullFile(archiveFileName, tmpFile);
        assertTrue(Files.equal(srcFile, downloadedFile));
    }

    @Test
    public void testDownloadAllFilesInDirectory() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        String testFolderName = "testDownloadAllFilesInDirectory";
        System.out.println(testFolderName);

        TestFilesToDownloadCreator testFilesToDownloadCreator = new TestFilesToDownloadCreator(srcDirPath,
                                                                                               testFolderName).invoke();
        File srcTextFile = testFilesToDownloadCreator.getSrcTextFile();
        File srcTempFile = testFilesToDownloadCreator.getSrcTempFile();

        File destTempDir = tmpDir.newFolder(TEMP_DIR_NAME);

        // use standard client
        IDataSpaceClient client = clientInstance();
        RemoteSource source = new RemoteSource(USER, testFolderName);
        LocalDestination dest = new LocalDestination(destTempDir);

        assertTrue(client.download(source, dest));

        assertTrue(Files.equal(srcTextFile, new File(destTempDir, TEMP_FILE_TXT_NAME)));
        assertTrue(Files.equal(srcTempFile, new File(destTempDir, TEMP_FILE_TMP_PATH)));

        // use RemoteSpace API
        File destTempDir2 = tmpDir.newFolder(TEMP_DIR2_NAME);
        client.getUserSpace().pullFile(testFolderName, destTempDir2);

        assertTrue(Files.equal(srcTextFile, new File(destTempDir2, TEMP_FILE_TXT_NAME)));
        assertTrue(Files.equal(srcTempFile, new File(destTempDir2, TEMP_FILE_TMP_PATH)));

    }

    @Test
    public void testDownloadSelectedFilesUsingGlobPattern() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();

        String testFolderName = "testDownloadSelectedFilesUsingGlobPattern";
        System.out.println(testFolderName);

        TestFilesToDownloadCreator testFilesToDownloadCreator = new TestFilesToDownloadCreator(srcDirPath,
                                                                                               testFolderName).invoke();
        File srcTextFile = testFilesToDownloadCreator.getSrcTextFile();
        File srcTempFile = testFilesToDownloadCreator.getSrcTempFile();

        File destTempDir = tmpDir.newFolder(TEMP_DIR_NAME);

        // use standard client
        IDataSpaceClient client = clientInstance();
        RemoteSource source = new RemoteSource(USER, testFolderName);
        source.setIncludes("*.txt");
        LocalDestination dest = new LocalDestination(destTempDir);

        assertTrue(client.download(source, dest));

        File[] listFiles = destTempDir.listFiles();

        assertEquals(1, listFiles.length);
        assertTrue(Files.equal(srcTextFile, listFiles[0]));

        // use RemoteSpace API
        File destTempDir2 = tmpDir.newFolder(TEMP_DIR2_NAME);
        client.getUserSpace().pullFiles(testFolderName, "*.txt", destTempDir2);

        listFiles = destTempDir2.listFiles();

        assertEquals(1, listFiles.length);
        assertTrue(Files.equal(srcTextFile, listFiles[0]));
    }

    @Test
    public void testListFilesNonRecursive() throws Exception {
        String testFolderName = "testListFilesNonRecursive";
        System.out.println(testFolderName);
        createFilesInUserSpace(testFolderName);

        // use standard client
        IDataSpaceClient client = clientInstance();
        RemoteSource source = new RemoteSource(USER, testFolderName);
        source.setIncludes("*");

        ListFile listFile = client.list(source);

        List<String> directories = listFile.getDirectoryListing();
        System.out.println("Directories : " + directories);
        assertEquals(1, directories.size());
        assertEquals(TEMP_DIR_NAME, directories.get(0));

        List<String> files = listFile.getFileListing();
        System.out.println("Files : " + files);
        assertEquals(1, files.size());
        assertEquals(TEMP_FILE_TXT_NAME, files.get(0));

        // use RemoteSpace API
        List<String> foundFiles = client.getUserSpace().listFiles(testFolderName, "*");
        System.out.println("Full : " + foundFiles);
        assertEquals(2, foundFiles.size());
        assertArrayEquals(new String[] { TEMP_DIR_NAME, TEMP_FILE_TXT_NAME }, foundFiles.toArray(new String[0]));
    }

    private void createFilesInUserSpace(String testListFilesNonRecursive)
            throws NotConnectedException, PermissionException, IOException {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        new TestFilesToDownloadCreator(srcDirPath, testListFilesNonRecursive).invoke();
    }

    @Test
    public void testListFilesRecursive() throws Exception {
        String testFolderName = "testListFilesRecursive";
        System.out.println(testFolderName);
        createFilesInUserSpace(testFolderName);

        // use standard client
        IDataSpaceClient client = clientInstance();
        RemoteSource source = new RemoteSource(USER, testFolderName);
        source.setIncludes("**");

        ListFile listFile = client.list(source);

        List<String> directories = listFile.getDirectoryListing();
        System.out.println("Directories : " + directories);
        assertEquals(1, directories.size());
        assertEquals(DataTransferTest.TEMP_DIR_NAME, directories.get(0));

        List<String> files = listFile.getFileListing();
        System.out.println("Files : " + files);
        assertEquals(2, files.size());
        assertEquals(TEMP_FILE_TMP_PATH, files.get(0));
        assertEquals(TEMP_FILE_TXT_NAME, files.get(1));

        // use RemoteSpace API
        List<String> foundFiles = client.getUserSpace().listFiles(testFolderName, "**");
        System.out.println("Full : " + foundFiles);
        assertEquals(3, foundFiles.size());
        assertArrayEquals(new String[] { TEMP_DIR_NAME, TEMP_FILE_TMP_PATH, TEMP_FILE_TXT_NAME },
                          foundFiles.toArray(new String[0]));
    }

    @Test
    public void testListFilesRecursiveWithPattern() throws Exception {
        String testFolderName = "testListFilesRecursiveWithPattern";
        System.out.println(testFolderName);
        createFilesInUserSpace(testFolderName);

        // use standard client
        IDataSpaceClient client = clientInstance();
        RemoteSource source = new RemoteSource(USER, testFolderName);
        source.setIncludes("**/*.tmp");

        ListFile listFile = client.list(source);

        List<String> directories = listFile.getDirectoryListing();
        System.out.println("Directories : " + directories);
        assertEquals(0, directories.size());

        List<String> files = listFile.getFileListing();
        System.out.println("Files : " + files);
        assertEquals(1, files.size());
        assertEquals(TEMP_FILE_TMP_PATH, files.get(0));

        // use RemoteSpace API
        List<String> foundFiles = client.getUserSpace().listFiles(testFolderName, "**/*.tmp");
        System.out.println("Full : " + foundFiles);
        assertEquals(1, foundFiles.size());
        assertArrayEquals(new String[] { TEMP_FILE_TMP_PATH }, foundFiles.toArray(new String[0]));
    }

    @Test
    public void testDeleteFile() throws Exception {
        URI srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0));
        File srcFile = new File(new File(srcDirPath), TEMP_FILE_TMP_NAME);
        if (srcFile.exists()) {
            assertTrue(srcFile.delete());
        }
        Files.write(randomFileContents(), srcFile);

        // use standard client
        IDataSpaceClient client = clientInstance();
        RemoteSource source = new RemoteSource(USER, TEMP_FILE_TMP_NAME);
        assertTrue(client.delete(source));
        assertFalse(srcFile.exists());

        // use RemoteSpace API
        Files.write(randomFileContents(), srcFile);
        client.getUserSpace().deleteFile(TEMP_FILE_TMP_NAME);
        assertFalse(srcFile.exists());
    }

    @Test
    public void testCreateFile() throws Exception {
        URI srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0));

        String filename = TEMP_FILE_TMP_NAME;

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

        String testFolderName = "testcreatefolder";
        System.out.println(testFolderName);

        RemoteSource source = new RemoteSource(USER, testFolderName);
        source.setType(FileType.FOLDER);

        IDataSpaceClient client = clientInstance();
        assertTrue(client.create(source));

        File expectedFile = new File(srcDirPath.getPath(), testFolderName);

        assertTrue(expectedFile.exists());
        assertTrue(expectedFile.isDirectory());
    }

    @Test(expected = Exception.class)
    public void testCreateFolderWithoutSpecifyingFileType() throws Exception {
        String testFolderName = "testCreateFolderWithoutSpecifyingFileType";
        System.out.println(testFolderName);

        RemoteSource source = new RemoteSource(USER, testFolderName);
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
        client.init(new ConnectionInfo(getRestServerUrl(), getLogin(), getPassword(), null, true));
        return client;
    }

    private class TestFilesToUploadCreator {
        private File tempTextFile;

        private File tempFile;

        public File getTempTextFile() {
            return tempTextFile;
        }

        public File getTempFile() {
            return tempFile;
        }

        public TestFilesToUploadCreator invoke() throws IOException {
            tempTextFile = tmpDir.newFile(TEMP_FILE_TXT_NAME);
            Files.write("some text ...".getBytes(), tempTextFile);

            File tempDir = tmpDir.newFolder(DataTransferTest.TEMP_DIR_NAME);
            tempFile = new File(tempDir, TEMP_FILE_TMP_NAME);
            Files.createParentDirs(tempFile);
            Files.write(randomFileContents(), tempFile);
            return this;
        }
    }

    private class TestFilesToDownloadCreator {
        private String srcDirPath;

        private String dirName;

        private File srcTextFile;

        private File srcTempFile;

        public TestFilesToDownloadCreator(String srcDirPath, String dirName) {
            this.srcDirPath = srcDirPath;
            this.dirName = dirName;
        }

        public File getSrcTextFile() {
            return srcTextFile;
        }

        public File getSrcTempFile() {
            return srcTempFile;
        }

        public TestFilesToDownloadCreator invoke() throws IOException {
            File srcDir = new File(srcDirPath, dirName);
            if (srcDir.exists()) {
                FileUtils.deleteDirectory(srcDir);
            }

            srcTextFile = new File(srcDir, TEMP_FILE_TXT_NAME);
            Files.createParentDirs(srcTextFile);
            Files.write("some text ...".getBytes(), srcTextFile);

            File srcTempDir = new File(srcDir, TEMP_DIR_NAME);
            srcTempFile = new File(srcTempDir, TEMP_FILE_TMP_NAME);
            Files.createParentDirs(srcTempFile);
            Files.write(randomFileContents(), srcTempFile);
            return this;
        }
    }
}
