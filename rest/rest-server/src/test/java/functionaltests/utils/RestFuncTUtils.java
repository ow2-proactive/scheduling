/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * %$ACTIVEEON_INITIAL_DEV$
 */

package functionaltests.utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.security.PublicKey;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


public class RestFuncTUtils {

    private RestFuncTUtils() {
    }

    public static void destroy(Process process) throws Exception {
        process.destroy();
        close(process.getOutputStream());
        close(process.getInputStream());
        close(process.getErrorStream());
        process.waitFor();
        System.out.println(String.format("Process ended with exit code %d. ", process.exitValue()));
    }

    private static void close(Closeable stream) {
        try {
            stream.close();
        } catch (IOException ioe) {
            System.err.println("An error occurred while closing the stream.");
        }
    }

    public static final String getJavaPathFromSystemProperties() {
        return (new StringBuilder()).append(System.getProperty("java.home")).append(File.separator).append(
                "bin").append(File.separator).append("java").toString();
    }

    public static Credentials createCredentials(String login, String password, PublicKey pubKey)
            throws Exception {
        return Credentials.createCredentials(new CredData(CredData.parseLogin(login), CredData
                .parseDomain(login), password), pubKey);
    }

    public static String buildJvmParameters() {
        StringBuilder jvmParameters = new StringBuilder();
        jvmParameters.append(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine());
        jvmParameters.append(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
        return jvmParameters.toString();
    }

    public static void cleanupRMActiveObjectRegistry() throws Exception {
        cleanupActiveObjectRegistry(PAResourceManagerProperties.RM_NODE_NAME.getValueAsString(),
                RMConstants.NAME_ACTIVE_OBJECT_RMCORE, RMConstants.NAME_ACTIVE_OBJECT_RMADMIN,
                RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION, RMConstants.NAME_ACTIVE_OBJECT_RMUSER,
                RMConstants.NAME_ACTIVE_OBJECT_RMMONITORING);
    }

    public static void cleanupActiveObjectRegistry(String... namesToRemove) throws Exception {
        String url = "//" + ProActiveInet.getInstance().getHostname();

        RemoteObjectFactory factory = AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory();
        for (URI uri : factory.list(new URI(url))) {
            for (String name : namesToRemove) {
                if (uri.getPath().endsWith(name)) {
                    System.out.println("Unregistering object with URI: " + uri);
                    factory.unregister(uri);
                    break;
                }
            }
        }
    }

    public static String getClassPath(Class<?> clazz) throws Exception {
        String name = (new StringBuilder()).append('/').append(clazz.getName().replace('.', '/')).append(
                ".class").toString();
        String osName = name.replace('/', File.separator.charAt(0));
        URL resource = clazz.getResource(name);
        String absolutePath = (new File(resource.toURI())).getAbsolutePath();
        return absolutePath.substring(0, absolutePath.indexOf(osName));
    }

    public static int findFreePort() throws Exception {
        ServerSocket s = new ServerSocket(0);
        s.close();
        return s.getLocalPort();

    }

}
