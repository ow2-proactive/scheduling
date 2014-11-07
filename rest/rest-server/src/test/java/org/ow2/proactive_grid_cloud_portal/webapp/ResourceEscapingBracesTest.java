package org.ow2.proactive_grid_cloud_portal.webapp;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;


public class ResourceEscapingBracesTest {

    private static ServletTester servletTester;

    @BeforeClass
    public static void startServer() throws Exception {
        servletTester = new ServletTester();
        servletTester.setResourceBase(ResourceEscapingBracesTest.class.getResource("doctest").toString());
        servletTester.addServlet(DefaultServlet.class, "/rest_not_fixed/*");
        servletTester.addServlet(RestDocumentationServlet.class, "/rest_fixed/*");
        servletTester.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        servletTester.stop();
    }

    @Test
    public void without_fix() throws Exception {
        HttpTester response = get("/rest_not_fixed/{test}/file.txt");

        assertEquals(404, response.getStatus());
    }

    @Test
    public void curly_braces_are_escaped() throws Exception {
        HttpTester response = get("/rest_fixed/{test}/file.txt");

        assertEquals(200, response.getStatus());
    }

    private HttpTester get(String uri) throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        request.setHeader("Host", "127.0.0.1");
        request.setURI(uri);

        response.parse(servletTester.getResponses(request.generate()));
        return response;
    }
}