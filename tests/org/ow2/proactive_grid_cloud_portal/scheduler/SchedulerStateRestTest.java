package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;

public class SchedulerStateRestTest {

    private static TJWSEmbeddedJaxrsServer server;
    private static int port;

    @BeforeClass
    public static void startServer() throws IOException {
        server = new TJWSEmbeddedJaxrsServer();
        port = findFreePort();
        server.setPort(port);
        server.setRootResourcePath("/");
        server.start();
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(new SchedulerStateRest());
    }

    @AfterClass
    public static void stopServer() {
        server.stop();
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

    private void callGetJobImageAndVerifyOutput() throws IOException {
        String pngIn = "ê";
        String jobId = createJobArchive(pngIn);

        SchedulerRestInterface client = ProxyFactory.create(SchedulerRestInterface.class, "http://localhost:" + port + "/");
        String pngAsBase64String = client.getJobImage("42", jobId);
        String pngOut = new String(Base64.decodeBase64(pngAsBase64String.getBytes(SchedulerRestInterface.ENCODING)), SchedulerRestInterface.ENCODING);

        assertEquals(pngIn, pngOut);
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

    private static int findFreePort() throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }
}

