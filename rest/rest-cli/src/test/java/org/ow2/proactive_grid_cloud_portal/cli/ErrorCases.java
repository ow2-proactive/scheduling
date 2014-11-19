package org.ow2.proactive_grid_cloud_portal.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;


public class ErrorCases {

    private static Server server;
    private static String serverUrl;

    private ByteArrayOutputStream capturedOutput;
    private String inputLines;

    @BeforeClass
    public static void startHttpsServer() throws Exception {
        server = new Server();
        SslContextFactory httpsConfiguration = new SslContextFactory();
        httpsConfiguration.setKeyStorePath(ErrorCases.class.getResource("keystore").getPath());
        httpsConfiguration.setKeyStorePassword("activeeon");
        SslSelectChannelConnector ssl = new SslSelectChannelConnector(httpsConfiguration);
        server.addConnector(ssl);
        server.start();
        serverUrl = "https://localhost:" + ssl.getLocalPort() + "/rest";
    }

    @AfterClass
    public static void stopHttpsServer() throws Exception {
        server.stop();
    }

    @Before
    public void captureInputOutput() throws Exception {
        inputLines = "";
        capturedOutput = new ByteArrayOutputStream();
        PrintStream captureOutput = new PrintStream(capturedOutput);
        System.setOut(captureOutput);
    }

    @Test
    public void ssl_error_is_showed_when_certificate_is_invalid_interactive_mode() throws Exception {
        typeLine("url('" + serverUrl + "')");
        typeLine("login('admin')").typeLine("admin");

        int exitCode = runCli();

        assertEquals(0, exitCode);
        assertThat(capturedOutput.toString(), containsString("SSL error"));
    }

    @Test
    public void ssl_error_is_showed_when_certificate_is_invalid_cli_mode() throws Exception {
        typeLine("admin");

        int exitCode = runCli("-u", serverUrl, "-l", "admin", "-lj");

        assertEquals(1, exitCode);
        assertThat(capturedOutput.toString(), containsString("SSL error"));
    }

    private ErrorCases typeLine(String line) {
        inputLines += line + System.getProperty("line.separator");
        return this;
    }

    private int runCli(String... args) {
        System.setIn(new ByteArrayInputStream(inputLines.getBytes()));
        return new CommonEntryPoint().run(args);
    }

}