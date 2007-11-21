/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package unitTests.uri;

import java.net.URI;
import java.net.URISyntaxException;

import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.util.URIBuilder;
import static junit.framework.Assert.assertTrue;

/**
 *
 * unit test for the URIBuilder
 *
 */
public class URITest {
    @org.junit.Test
    public void checkURI() throws Exception {
        String protocol = "rmi";
        String host = "localhost.localdomain";
        String path = "apath";
        int port = 1258;

        URI uri = URIBuilder.buildURI(host, path, protocol, port, false);

        // checking getters
        assertTrue(URIBuilder.getPortNumber(uri) == port);

        assertTrue(host.equals(URIBuilder.getHostNameFromUrl(uri)));

        assertTrue(path.equals(URIBuilder.getNameFromURI(uri)));

        // check the remove protocol method
        URI u = URIBuilder.removeProtocol(uri);
        assertTrue("//localhost.localdomain:1258/apath".equals(u.toString()));

        // check the setPort method
        int port2 = 5656;
        u = URIBuilder.setPort(uri, port2);
        assertTrue(port2 == u.getPort());

        // check the setProtocol
        u = URIBuilder.setProtocol(uri, "http");
        assertTrue("http://localhost.localdomain:1258/apath".equals(
                u.toString()));

        // validate an URI
        try {
            // wrong protocol
            URIBuilder.checkURI("://localh/s");
            assertTrue(false);
        } catch (URISyntaxException e) {
            assertTrue(true);
        }
    }
}
