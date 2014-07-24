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

import static functionaltests.RestFuncTHelper.getRestServerUrl;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.ISchedulerClient.DataSpace;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class DataTransferTest extends AbstractRestFuncTestCase {

    private static final int file_size = 100;

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        init(DataTransferTest.class.getSimpleName());
    }

    @Test
    public void testUploadFile() throws Exception {
        ISchedulerClient client = clientInstance();
        File tmpFile = tmpDir.newFile("tmpfile.tmp");
        Files.write(randomFileContents(), tmpFile);
        assertTrue(client.upload(tmpFile, DataSpace.USER, "tmpfile.tmp"));
        String destDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File destFile = new File(destDirPath, "tmpfile.tmp");
        assertTrue(Files.equal(tmpFile, destFile));
    }

    @Test
    public void testUploadFiles() throws Exception {
        // entire folder
        File tempTextFile = tmpDir.newFile("tempFile.txt");
        Files.write("some random text ...".getBytes(), tempTextFile);

        File tempDir = tmpDir.newFolder("tempDir");
        File tempFile = new File(tempDir, "tempFile.tmp");
        Files.createParentDirs(tempFile);
        Files.write(randomFileContents(), tempFile);

        ISchedulerClient client = clientInstance();
        assertTrue(client.upload(tmpDir.getRoot(), DataSpace.USER, "test_upload_files"));

        String destRootUri = URI.create(client.getUserSpaceURIs().get(0)).getPath();
        assertTrue(Files.equal(tempTextFile, new File(destRootUri, "test_upload_files/tempFile.txt")));
        assertTrue(Files.equal(tempFile, new File(destRootUri, "test_upload_files/tempDir/tempFile.tmp")));
    }

    @Test
    public void testUploadFiles2() throws Exception {
        // regex
        File tempTextFile = tmpDir.newFile("tempFile.txt");
        Files.write("some random text ...".getBytes(), tempTextFile);

        File tempDir = tmpDir.newFolder("tempDir");
        File tempFile = new File(tempDir, "tempFile.tmp");
        Files.createParentDirs(tempFile);
        Files.write(randomFileContents(), tempFile);

        ISchedulerClient client = clientInstance();
        assertTrue(client.upload(tmpDir.getRoot(), ".*\\.(txt)$", DataSpace.USER, "test_upload_files2"));

        String destRootUri = URI.create(client.getUserSpaceURIs().get(0)).getPath();
        File[] destRootFiles = new File(destRootUri, "test_upload_files2").listFiles();
        assertTrue(destRootFiles != null && destRootFiles.length == 1);
        assertTrue(Files.equal(tempTextFile, destRootFiles[0]));
    }

    @Test
    public void testUploadFiles3() throws Exception {
        File tempTextFile = tmpDir.newFile("tempFile.txt");
        Files.write("some random text ...".getBytes(), tempTextFile);

        File tempDir = tmpDir.newFolder("tempDir");
        File tempFile = new File(tempDir, "tempFile.tmp");
        Files.createParentDirs(tempFile);
        Files.write(randomFileContents(), tempFile);

        ISchedulerClient client = clientInstance();
        assertTrue(client.upload(tmpDir.getRoot(), Lists.newArrayList("tempFile.tmp"), null, DataSpace.USER,
                "test_upload_files3"));

        String destRootUri = URI.create(client.getUserSpaceURIs().get(0)).getPath();
        File[] destRootFiles = new File(destRootUri, "test_upload_files3").listFiles();
        assertTrue(destRootFiles != null && destRootFiles.length == 1);
        assertTrue(Files.equal(tempFile, destRootFiles[0]));
    }

    @Test
    public void testDownloadFile() throws Exception {
        ISchedulerClient client = clientInstance();
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
        assertTrue(client.download(DataSpace.USER, "/tmpfile.tmp", tmpFile.getAbsolutePath()));
        assertTrue(Files.equal(srcFile, tmpFile));
    }

    @Test
    public void testDownloadFiles() throws Exception {
        ISchedulerClient client = clientInstance();
        String srcDirPath = URI.create(client.getUserSpaceURIs().get(0)).getPath();
        File srcDir = new File(srcDirPath, "test_download_files");
        if (srcDir.exists()) {
            FileUtils.deleteDirectory(srcDir);
        }

        File srcTextFile = new File(srcDir, "tempFile.txt");
        Files.createParentDirs(srcTextFile);
        Files.write("some random text ...".getBytes(), srcTextFile);

        File srcTempDir = new File(srcDir, "tempDir");
        File srcTempFile = new File(srcTempDir, "tempFile.tmp");
        Files.createParentDirs(srcTempFile);
        Files.write(randomFileContents(), srcTempFile);

        File destTempDir = tmpDir.newFolder("tempDir");
        assertTrue(client.download(DataSpace.USER, "test_download_files", destTempDir.getAbsolutePath()));

        assertTrue(Files.equal(srcTextFile, new File(destTempDir, "tempFile.txt")));
        assertTrue(Files.equal(srcTempFile, new File(destTempDir, "tempDir/tempFile.tmp")));
    }

    @Test
    public void testDownloadFiles2() throws Exception {
        ISchedulerClient client = clientInstance();
        String srcDirPath = URI.create(client.getUserSpaceURIs().get(0)).getPath();
        File srcDir = new File(srcDirPath, "test_download_files2");
        if (srcDir.exists()) {
            FileUtils.deleteDirectory(srcDir);
        }

        File srcTextFile = new File(srcDir, "tempFile.txt");
        Files.createParentDirs(srcTextFile);
        Files.write("some random text ...".getBytes(), srcTextFile);

        File srcTempDir = new File(srcDir, "tempDir");
        File srcTempFile = new File(srcTempDir, "tempFile.tmp");
        Files.createParentDirs(srcTempFile);
        Files.write(randomFileContents(), srcTempFile);

        File destTempDir = tmpDir.newFolder("tempDir");
        assertTrue(client.download(DataSpace.USER, "test_download_files2", Lists.newArrayList(".*\\.(txt)$"),
                null, destTempDir.getAbsolutePath()));

        File[] listFiles = destTempDir.listFiles();
        assertTrue(listFiles != null && listFiles.length == 1);
        assertTrue(Files.equal(srcTextFile, listFiles[0]));
    }

    @Test
    public void listFiles() throws Exception {
        ISchedulerClient client = clientInstance();
        String srcDirPath = URI.create(client.getUserSpaceURIs().get(0)).getPath();
        File srcDir = new File(srcDirPath, "test_list_files");
        if (srcDir.exists()) {
            FileUtils.deleteDirectory(srcDir);
        }

        File srcTextFile = new File(srcDir, "tempFile.txt");
        Files.createParentDirs(srcTextFile);
        Files.write("some random text ...".getBytes(), srcTextFile);

        File srcTempDir = new File(srcDir, "tempDir");
        File srcTempFile = new File(srcTempDir, "tempFile.tmp");
        Files.createParentDirs(srcTempFile);
        Files.write(randomFileContents(), srcTempFile);

        ListFile listFile = client.listFiles(DataSpace.USER, "test_list_files");
        List<String> directories = listFile.getDirectories();
        assertTrue(directories != null & directories.size() == 1
                && "tempDir".equals(directories.get(0)));
        List<String> files = listFile.getFiles();
        assertTrue(files != null && files.size() == 1 && "tempFile.txt".equals(files.get(0)));
    }

    @Test
    public void testDeleteFile() throws Exception {
        ISchedulerClient client = clientInstance();
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File srcFile = new File(srcDirPath, "tempFile.tmp");
        if (srcFile.exists()) {
            assertTrue(srcFile.delete());
        }
        Files.write(randomFileContents(), srcFile);

        assertTrue(client.deleteFile(DataSpace.USER, "tempFile.tmp"));
        assertTrue(!(new File(srcDirPath, "tempFile.tmp")).exists());
    }

    private byte[] randomFileContents() {
        byte[] fileContents = new byte[file_size];
        (new Random()).nextBytes(fileContents);
        return fileContents;
    }

    private ISchedulerClient clientInstance() throws Exception {
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(getRestServerUrl(), getLogin(), getPassword());
        return client;
    }
}
