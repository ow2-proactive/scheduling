/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ProxyFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SchedulerStateRestEncodingTest extends RestTestServer {

    @BeforeClass
    public static void setUpRest() throws Exception {
        addResource(new SchedulerStateRest());
    }

    @Test
    public void testUTF8JVM() throws Exception {
        changeEncoding("UTF-8");
        callGetJobImageAndVerifyOutput();
    }

    @Test
    public void testCp1252JVM() throws Exception {
        changeEncoding("Cp1252");
        callGetJobImageAndVerifyOutput();
    }

    // this more of a documentation of how to read the output of getJobImage rather than a test
    @Test
    public void testRealImage_UTF8() throws Exception {
        changeEncoding("UTF-8");

        InputStream inputJob = getClass().getResourceAsStream("job_512.zip");
        String jobId = createJobArchive(inputJob);

        String pngAsBase64String = getJobImageAsBase64String(jobId);
        byte[] pngOut = Base64.decodeBase64(pngAsBase64String.getBytes(SchedulerRestInterface.ENCODING));

        assertTrue(Arrays.equals(getImageFromJobArchive("job_512.zip"), pngOut));
    }

    // this more of a documentation of how to read the output of getJobImage rather than a test
    @Test
    public void testRealImage_Cp1252() throws Exception {
        changeEncoding("Cp1252");

        InputStream inputJob = getClass().getResourceAsStream("job_512.zip");
        String jobId = createJobArchive(inputJob);

        String pngAsBase64String = getJobImageAsBase64String(jobId);
        byte[] pngOut = Base64.decodeBase64(pngAsBase64String.getBytes(SchedulerRestInterface.ENCODING));

        assertTrue(Arrays.equals(getImageFromJobArchive("job_512.zip"), pngOut));
    }

    private byte[] getImageFromJobArchive(String jobArchive) throws IOException {
        ZipFile zipFile = new ZipFile(getClass().getResource(jobArchive).getFile());
        ZipEntry entry = zipFile.getEntry("JOB-INF/image.png");
        return IOUtils.toByteArray(zipFile.getInputStream(entry));
    }

    private void callGetJobImageAndVerifyOutput() throws IOException, NotConnectedRestException {
        String pngIn = "ê";
        String jobId = createJobArchive(pngIn);
        String pngAsBase64String = getJobImageAsBase64String(jobId);
        String pngOut = new String(Base64.decodeBase64(pngAsBase64String
                .getBytes(SchedulerRestInterface.ENCODING)), SchedulerRestInterface.ENCODING);

        assertEquals(pngIn, pngOut);
    }

    private String getJobImageAsBase64String(String jobId) throws IOException, NotConnectedRestException {
        String sessionId = SchedulerSessionMapper.getInstance().add(
                new SchedulerProxyUserInterfaceForTests(), "bob");

        SchedulerRestInterface client = ProxyFactory.create(SchedulerRestInterface.class,
                "http://localhost:" + port + "/");
        return client.getJobImage(sessionId, jobId);
    }

    private String createJobArchive(InputStream jobArchive) throws IOException {
        File jobZip = File.createTempFile("job_", ".zip");
        jobZip.deleteOnExit();
        IOUtils.copy(jobArchive, new FileOutputStream(jobZip));
        return jobZip.getName().replaceAll("job_", "").replaceAll(".zip", "");
    }

    private String createJobArchive(String pngIn) throws IOException {
        File jobZip = File.createTempFile("job_", ".zip");
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(jobZip));
        ZipEntry zippedImage = new ZipEntry("JOB-INF/image.png");
        zip.putNextEntry(zippedImage);
        IOUtils.write(pngIn, zip, SchedulerRestInterface.ENCODING);
        zip.closeEntry();
        zip.close();
        jobZip.deleteOnExit();
        return jobZip.getName().replaceAll("job_", "").replaceAll(".zip", "");
    }

    private static void changeEncoding(String s) throws Exception {
        Class<Charset> c = Charset.class;
        Field defaultCharsetField = c.getDeclaredField("defaultCharset");
        defaultCharsetField.setAccessible(true);
        defaultCharsetField.set(null, Charset.forName(s));
    }

}
