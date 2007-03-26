package org.objectweb.proactive.core.ssh;

import java.io.IOException;
import java.util.HashMap;
import static org.objectweb.proactive.core.ssh.SSH.logger;
public class SSHConnectionCache {
    private static SSHConnectionCache singleton = null;
    private HashMap<String, SSHConnection> hash;

    static public SSHConnectionCache getSingleton() {
        if (singleton == null) {
            singleton = new SSHConnectionCache();
        }

        return singleton;
    }

    private SSHConnectionCache() {
        hash = new HashMap<String, SSHConnection>();
    }

    public SSHConnection getConnection(String username, String hostname,
        String port) throws IOException {
        String id = buildIdentifier(username, hostname, port);
        SSHConnection conn = hash.get(id);
        if (conn == null) {
            conn = new SSHConnection(username, hostname, port);
            hash.put(id, conn);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Connection to " + hostname + ":" + port +
                    " already opened");
            }
        }
        return conn;
    }

    private static String buildIdentifier(String user, String host, String port) {
        return port + user + host;
    }
}
