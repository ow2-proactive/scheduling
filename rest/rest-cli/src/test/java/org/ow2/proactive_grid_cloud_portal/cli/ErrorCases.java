package org.ow2.proactive_grid_cloud_portal.cli;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import jline.WindowsTerminal;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hamcrest.core.IsEqual;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class ErrorCases {

    private static final String EXPECTED_ERROR_MSG =
            "sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target";

    private static Server server;
    private static String serverUrl;

    private ByteArrayOutputStream capturedOutput;
    private String inputLines;

    @BeforeClass
    public static void startHttpsServer() throws Exception {    	
        server = new Server();

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(ErrorCases.class.getResource("keystore").getPath());
        sslContextFactory.setKeyStorePassword("activeeon");

        HttpConfiguration httpConfig = new HttpConfiguration();
        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        ServerConnector sslConnector = new ServerConnector(server,
                new ConnectionFactory[]{
                        new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                        new HttpConnectionFactory(httpsConfig)});

        server.addConnector(sslConnector);
        server.start();
        serverUrl = "https://localhost:" + sslConnector.getLocalPort() + "/rest";
    }

    private static void skipIfHeadlessEnvironment() {
    	Assume.assumeThat(Boolean.valueOf(System.getProperty("java.awt.headless")), IsEqual.equalTo(false));
	}

	@AfterClass
    public static void stopHttpsServer() throws Exception {
        server.stop();
    }

    @Before
    public void captureInputOutput() throws Exception {
        System.setProperty(WindowsTerminal.DIRECT_CONSOLE, "false"); // to be able to type input on Windows
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
        assertThat(capturedOutput.toString(), containsString(EXPECTED_ERROR_MSG));
    }

    @Test
    public void ssl_error_is_showed_when_certificate_is_invalid_cli_mode() throws Exception {
        typeLine("admin");

        int exitCode = runCli("-u", serverUrl, "-l", "admin", "-lj");

        assertEquals(1, exitCode);
        assertThat(capturedOutput.toString(), containsString(EXPECTED_ERROR_MSG));
    }

    private ErrorCases typeLine(String line) {
        inputLines += line + System.lineSeparator();
        return this;
    }

    private int runCli(String... args) {
        System.setIn(new ByteArrayInputStream(inputLines.getBytes()));
        return new CommonEntryPoint().run(args);
    }

}