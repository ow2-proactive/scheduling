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

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Random;

import org.ow2.proactive.scheduler.rest.ds.DataSpaceClient;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient;
import org.ow2.proactive.scheduler.rest.ds.LocalDestination;
import org.ow2.proactive.scheduler.rest.ds.LocalDirSource;
import org.ow2.proactive.scheduler.rest.ds.LocalFileSource;
import org.ow2.proactive.scheduler.rest.ds.RemoteDestination;
import org.ow2.proactive.scheduler.rest.ds.RemoteSource;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static functionaltests.RestFuncTHelper.getRestServerUrl;
import static org.junit.Assert.*;
import static org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.Dataspace.USER;

public class DataTransferTest extends AbstractRestFuncTestCase {

    private static final int file_size = 100;

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        init(DataTransferTest.class.getSimpleName());
    }

    @Test
    public void test_upload_single_file() throws Exception {
        IDataSpaceClient client = clientInstance();
        File tmpFile = tmpDir.newFile("tmpfile.tmp");
        Files.write(randomFileContents(), tmpFile);
        LocalFileSource source = new LocalFileSource(tmpFile);
        RemoteDestination dest = new RemoteDestination(USER, "tmpfile.tmp");
        assertTrue(client.upload(source, dest));
        String destDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File destFile = new File(destDirPath, "tmpfile.tmp");
        assertTrue(Files.equal(tmpFile, destFile));
    }


    @Test
    public void test_upload_all_files_in_directory() throws Exception {
        // entire folder
        File tempTextFile = tmpDir.newFile("tempFile.txt");
        Files.write("some random text ...".getBytes(), tempTextFile);

        File tempDir = tmpDir.newFolder("tempDir");
        File tempFile = new File(tempDir, "tempFile.tmp");
        Files.createParentDirs(tempFile);
        Files.write(randomFileContents(), tempFile);

        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpDir.getRoot());
        RemoteDestination dest = new RemoteDestination(USER, "test_upload_all_files_in_directory");

        assertTrue(client.upload(source, dest));

        String destRootUri = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        assertTrue(Files.equal(tempTextFile, new File(destRootUri, "test_upload_all_files_in_directory/tempFile.txt")));
        assertTrue(Files.equal(tempFile, new File(destRootUri, "test_upload_all_files_in_directory/tempDir/tempFile.tmp")));
    }

    @Test
    public void test_upload_selected_files_using_regex() throws Exception {
        // regex
        File tempTextFile = tmpDir.newFile("tempFile.txt");
        Files.write("some random text ...".getBytes(), tempTextFile);

        File tempDir = tmpDir.newFolder("tempDir");
        File tempFile = new File(tempDir, "tempFile.tmp");
        Files.createParentDirs(tempFile);
        Files.write(randomFileContents(), tempFile);

        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpDir.getRoot());
        source.setIncludes(".*\\.(txt)$");
        RemoteDestination dest = new RemoteDestination(USER, "test_upload_selected_files_using_regex");
        assertTrue(client.upload(source, dest));

        String destRootUri = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File[] destRootFiles = new File(destRootUri, "test_upload_selected_files_using_regex").listFiles();
        assertEquals(1, destRootFiles.length);
        assertTrue(Files.equal(tempTextFile, destRootFiles[0]));
    }

    @Test
    public void test_upload_selected_files_using_filenames() throws Exception {
        File tempTextFile = tmpDir.newFile("tempFile.txt");
        Files.write("some random text ...".getBytes(), tempTextFile);

        File tempDir = tmpDir.newFolder("tempDir");
        File tempFile = new File(tempDir, "tempFile.tmp");
        Files.createParentDirs(tempFile);
        Files.write(randomFileContents(), tempFile);

        IDataSpaceClient client = clientInstance();
        LocalDirSource source = new LocalDirSource(tmpDir.getRoot());
        source.setIncludes("tempFile.tmp");
        RemoteDestination dest = new RemoteDestination(USER, "test_upload_selected_files_using_filenames");
        assertTrue(client.upload(source, dest));

        String destRootUri = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File[] destRootFiles = new File(destRootUri, "test_upload_selected_files_using_filenames").listFiles();
        assertEquals(1, destRootFiles.length);
        assertTrue(Files.equal(tempFile, destRootFiles[0]));
    }

    @Test
    public void test_download_file() throws Exception {
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
    public void test_download_all_files_in_directory() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File srcDir = new File(srcDirPath, "test_download_all_files_in_directory");
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
        RemoteSource source = new RemoteSource(USER, "test_download_all_files_in_directory");
        LocalDestination dest = new LocalDestination(destTempDir);
        IDataSpaceClient client = clientInstance();

        assertTrue(client.download(source, dest));

        assertTrue(Files.equal(srcTextFile, new File(destTempDir, "tempFile.txt")));
        assertTrue(Files.equal(srcTempFile, new File(destTempDir, "tempDir/tempFile.tmp")));
    }

    @Test
    public void test_download_selected_files_using_regex() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
        File srcDir = new File(srcDirPath, "test_download_all_files_in_directory");
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
        RemoteSource source = new RemoteSource(USER, "test_download_all_files_in_directory");
        source.includes(".*\\.(txt)$");
        LocalDestination dest = new LocalDestination(destTempDir);
        IDataSpaceClient client = clientInstance();

        assertTrue(client.download(source, dest));

        File[] listFiles = destTempDir.listFiles();

        assertEquals(1, listFiles.length);
        assertTrue(Files.equal(srcTextFile, listFiles[0]));
    }

    @Test
    public void test_list_files() throws Exception {
        String srcDirPath = URI.create(getScheduler().getUserSpaceURIs().get(0)).getPath();
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

        RemoteSource source = new RemoteSource(USER, "test_list_files");
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
    public void test_delete_file() throws Exception {
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

    private byte[] randomFileContents() {
        byte[] fileContents = new byte[file_size];
        (new Random()).nextBytes(fileContents);
        return fileContents;
    }

    private IDataSpaceClient clientInstance() throws Exception {
        DataSpaceClient client = new DataSpaceClient();
        client.init(getRestServerUrl(), getLogin(), getPassword());
        return client;
    }
}
