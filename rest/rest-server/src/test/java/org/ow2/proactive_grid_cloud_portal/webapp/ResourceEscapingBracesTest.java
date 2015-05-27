package org.ow2.proactive_grid_cloud_portal.webapp;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ResourceEscapingBracesTest {

    private static Server server;

    @BeforeClass
    public static void startServer() throws Exception {
        // using 0 forces Jetty to pick a port that is free at random
        server = new Server(0);

        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.setResourceBase(ResourceEscapingBracesTest.class.getResource("doctest").toString());
        handler.addServlet(DefaultServlet.class, "/rest_not_fixed/*");
        handler.addServlet(RestDocumentationServlet.class, "/rest_fixed/*");

        server.setHandler(handler);

        server.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
    }

    @Test
    public void without_fix() throws Exception {
        ContentResponse response = get(server.getURI() + "rest_not_fixed/%7Btest%7D/file.txt");

        assertEquals(404, response.getStatus());
    }

    @Test
    public void curly_braces_are_escaped() throws Exception {
        ContentResponse response = get(server.getURI() + "rest_fixed/%7Btest%7D/file.txt");

        assertEquals(200, response.getStatus());
    }

    private ContentResponse get(String uri) throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        Request request = httpClient.newRequest(uri);
        request.header("Host", "127.0.0.1");
        request.method(HttpMethod.GET);
        request.version(HttpVersion.HTTP_1_0);

        return request.send();
    }

}