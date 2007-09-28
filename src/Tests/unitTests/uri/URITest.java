package unitTests.uri;

import java.net.URI;
import java.net.URISyntaxException;

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
