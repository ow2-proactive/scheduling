/*
 *  *
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;


/**
 * RestSchedulerPushPullFileTest
 * <p/>
 * This test tries to push and pull a file to the Global and User space
 *
 * @author The ProActive Team
 */
public class RestSchedulerPushPullFileTest extends AbstractRestFuncTestCase {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        init();
    }

    @Before
    public void setUp() throws Exception {
        Scheduler scheduler = RestFuncTHelper.getScheduler();
        SchedulerState state = scheduler.getState();
        List<JobState> jobStates = new ArrayList<>();
        jobStates.addAll(state.getPendingJobs());
        jobStates.addAll(state.getRunningJobs());
        jobStates.addAll(state.getFinishedJobs());
        for (JobState jobState : jobStates) {
            JobId jobId = jobState.getId();
            scheduler.killJob(jobId);
            scheduler.removeJob(jobId);
        }
    }

    @Test
    public void testPushPull() throws Exception {
        Scheduler scheduler = RestFuncTHelper.getScheduler();

        File[] destPaths = { new File("/test/push/pull"), new File("/") };

        String[] spacesNames = { SchedulerConstants.GLOBALSPACE_NAME, SchedulerConstants.USERSPACE_NAME };

        String[] spacesUris = { scheduler.getGlobalSpaceURIs().get(0), scheduler.getUserSpaceURIs().get(0) };
        for (File destPath : destPaths) {
            for (int i = 0; i < spacesNames.length; i++) {
                String spaceName = spacesNames[i];
                String spacePath = (new File(new URI(spacesUris[i]))).getAbsolutePath();
                testIt(spaceName, spacePath, destPath, true);

                // if URL is not encoded then the path appended to the URL will be interpreted
                // as if it is part of the URL. This will produce something like
                // 'http://localhost:8080/rest/scheduler/dataspace/USERSPACE//'
                // that contains a double slash at the end of the URL, which is now detected as invalid
                // by Resteasy. Consequently, it will produce an error on server side (error 500 received on client side)

                // for this reason the next line has been commented and a new test added below
                // testIt(spaceName, spacePath, destPath, false);
            }
        }
    }

    @Test
    public void testFailureWithNonEncodedParametersInUrlPath() throws Exception {
        String destPath = "/";

        String pullListUrl = getResourceUrl("dataspace/" +
                SchedulerConstants.GLOBALSPACE_NAME + "/" + destPath);

        HttpGet reqPullList = new HttpGet(pullListUrl);
        setSessionHeader(reqPullList);

        HttpResponse response = executeUriRequest(reqPullList);

        assertEquals(500, response.getStatusLine().getStatusCode());
    }

    public void testIt(String spaceName, String spacePath, File destPath, boolean encode) throws Exception {
        File testPushFile = RestFuncTHelper.getDefaultJobXmlfile();
        // you can test pushing pulling a big file :
        // testPushFile = new File("path_to_a_big_file");
        File destFile = new File(new File(spacePath, destPath.toString()), testPushFile.getName());
        if (destFile.exists()) {
            destFile.delete();
        }

        // PUSHING THE FILE
        String pushfileUrl = getResourceUrl("dataspace/" +
            spaceName +
            (encode ? URLEncoder.encode(destPath.toString(), "UTF-8") : destPath.toString()
                    .replace("\\", "/")));
        // either we encode or we test human readable path (with no special character inside)

        HttpPost reqPush = new HttpPost(pushfileUrl);
        setSessionHeader(reqPush);
        // we push a xml job as a simple test

        MultipartEntity multipartEntity = new MultipartEntity();
        multipartEntity.addPart("fileName", new StringBody(testPushFile.getName()));
        multipartEntity.addPart("fileContent", new InputStreamBody(FileUtils.openInputStream(testPushFile),
            MediaType.APPLICATION_OCTET_STREAM, null));
        reqPush.setEntity(multipartEntity);
        HttpResponse response = executeUriRequest(reqPush);

        System.out.println(response.getStatusLine());
        assertHttpStatusOK(response);
        Assert.assertTrue(destFile + " exists", destFile.exists());

        Assert.assertTrue("Original file and result are equals for " + spaceName, FileUtils.contentEquals(
                testPushFile, destFile));

        // LISTING THE TARGET DIRECTORY
        String pullListUrl = getResourceUrl("dataspace/" +
            spaceName +
            "/" +
            (encode ? URLEncoder.encode(destPath.toString(), "UTF-8") : destPath.toString()
                    .replace("\\", "/")));

        HttpGet reqPullList = new HttpGet(pullListUrl);
        setSessionHeader(reqPullList);

        HttpResponse response2 = executeUriRequest(reqPullList);

        System.out.println(response2.getStatusLine());
        assertHttpStatusOK(response2);

        InputStream is = response2.getEntity().getContent();
        List<String> lines = IOUtils.readLines(is);
        HashSet<String> content = new HashSet<>(lines);
        System.out.println(lines);
        Assert.assertTrue("Pushed file correctly listed", content.contains(testPushFile.getName()));

        // PULLING THE FILE
        String pullfileUrl = getResourceUrl("dataspace/" +
            spaceName +
            "/" +
            (encode ? URLEncoder.encode(destPath.toString() + "/" + testPushFile.getName(), "UTF-8")
                    : destPath.toString().replace("\\", "/") + "/" + testPushFile.getName()));

        HttpGet reqPull = new HttpGet(pullfileUrl);
        setSessionHeader(reqPull);

        HttpResponse response3 = executeUriRequest(reqPull);

        System.out.println(response3.getStatusLine());
        assertHttpStatusOK(response3);

        InputStream is2 = response3.getEntity().getContent();

        File answerFile = tmpFolder.newFile();
        FileUtils.copyInputStreamToFile(is2, answerFile);

        Assert.assertTrue("Original file and result are equals for " + spaceName, FileUtils.contentEquals(
                answerFile, testPushFile));

        // DELETING THE HIERARCHY
        File rootDir = destPath;
        while (rootDir.getParentFile() != null) {
            rootDir = rootDir.getParentFile();
        }
        String deleteUrl = getResourceUrl("dataspace/" + spaceName + "/" +
            (encode ? URLEncoder.encode(rootDir.toString(), "UTF-8") : rootDir.toString().replace("\\", "/")));
        HttpDelete reqDelete = new HttpDelete(deleteUrl);
        setSessionHeader(reqDelete);

        HttpResponse response4 = executeUriRequest(reqDelete);

        System.out.println(response4.getStatusLine());

        assertHttpStatusOK(response4);

        Assert.assertTrue(destFile + " still exist", !destFile.exists());
    }

}
