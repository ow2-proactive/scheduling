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
package org.ow2.proactive_grid_cloud_portal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.plugins.server.tjws.TJWSServletServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import Acme.Serve.Serve;


public class RestTestServer {
    protected static int port;

    private static TJWSEmbeddedJaxrsServer server;

    protected static ByteArrayOutputStream serverLogs = new ByteArrayOutputStream();

    @BeforeClass
    public static void startServer() throws IOException, NoSuchFieldException, IllegalAccessException {
        bypassProActiveLogger();
        preventProActiveToChangeSecurityManager();
        server = new TJWSEmbeddedJaxrsServer();
        silentServerError();

        port = findFreePort();
        server.setPort(port);
        server.setRootResourcePath("/");
        server.start();
    }

    private static void bypassProActiveLogger() {
        System.setProperty("log4j.configuration", "log4j.properties");
    }

    private static void preventProActiveToChangeSecurityManager() {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
    }

    /**
     * Use reflection to access private fields of the underlying server.
     */
    private static void silentServerError() throws NoSuchFieldException, IllegalAccessException {
        Field f = TJWSServletServer.class.getDeclaredField("server");
        f.setAccessible(true);
        TJWSServletServer.FileMappingServe serve = (TJWSServletServer.FileMappingServe) f.get(server);

        Field streamField = Serve.class.getDeclaredField("logStream");
        streamField.setAccessible(true);

        streamField.set(serve, new PrintStream(serverLogs));
    }

    protected static void addResource(Object restResource) {
        server.getDeployment().getDispatcher().getRegistry().addSingletonResource(restResource);
    }

    @AfterClass
    public static void stopServer() {
        server.stop();
    }

    private static int findFreePort() throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }

}
