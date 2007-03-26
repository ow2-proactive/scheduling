package org.objectweb.proactive.core.ssh;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import static org.objectweb.proactive.core.ssh.SSH.logger;

/**
 * A helper class to manager SSH Public keys
 *
 */
public class SSHKeys {
    static final private String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    static final public String[] IDENTITY_FILES = new String[] {
            "identity", "id_rsa", "id_dsa"
        };

    /** Default directory for public keys */
    static final public String SSH_DIR = System.getProperty("user.home") +
        FILE_SEPARATOR + ".ssh" + FILE_SEPARATOR;

    /** Default suffix for public keys */
    private final static String KEY_SUFFIX = ".pub";
    private final static int KEY_SUFFIX_LEN = KEY_SUFFIX.length();

    /** A Cache of public keys */
    private static String[] keys = null;

    /**
     * Find all SSH keys inside SshParameters.getSshKeyDirectory()
     *
     * @return all keys found
     * @throws IOException If the base directory does not exist an {@link IOException}
     * is thrown
     */
    static synchronized public String[] getKeys() throws IOException {
        if (keys != null) {
            return keys;
        }

        File dir = new File(SshParameters.getSshKeyDirectory());
        if (!dir.exists()) {
            logger.error("Cannot open SSH connection, " + dir +
                "does not exist");
            throw new IOException(dir + "does not exist");
        }
        if (!dir.isDirectory()) {
            logger.error("Cannot open SSH connection, " + dir +
                "is not a directory");
            throw new IOException(dir + "does not exist");
        }
        if (!dir.canRead()) {
            logger.error("Cannot open SSH connection, " + dir +
                "is not readable");
            throw new IOException(dir + "does not exist");
        }

        String[] tmp = dir.list(new PrivateKeyFilter());

        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = dir.toString() + "/" + tmp[i];
            tmp[i] = tmp[i].substring(0, tmp[i].length() - 4);
        }

        keys = tmp;
        return keys;
    }

    static public class PrivateKeyFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            if (name.endsWith(".pub")) {
                // Look it this file without ".pub" exist
                File tmp = new File(dir,
                        name.substring(0, name.length() - KEY_SUFFIX_LEN));
                return tmp.exists() && tmp.canRead() && tmp.isFile();
            }

            return false;
        }
    }
}
