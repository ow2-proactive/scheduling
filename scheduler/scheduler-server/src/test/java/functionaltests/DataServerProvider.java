package functionaltests;

import java.io.IOException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;


/**
 * Class used to starting ProActive Data Provider
 * A provider is started on client side, to "serve" all the input files needed on the computation nodes
 * Note: this provider will not launch a new JVM but execute inside the client JVM 
 *  
 */
public class DataServerProvider {

    public final String protocol = "proactive";
    private String rootDirectory;
    private String providerName;

    private FileSystemServerDeployer deployer;

    public String deployDataServer(String _rootDirectory, String _providerName) throws Exception {
        if (protocol.equals("proactive")) {
            return deployProActiveDataServer(_rootDirectory, _providerName);
        } else {
            throw new Exception("Unknown file transfer protocol: " + protocol);
        }
    }

    /**
     * 
     * @param _rootDirectory - the root data folder for the server 
     * @return the url of the server
     * @throws IOException
     */
    public String deployProActiveDataServer(String _rootDirectory, String _providerName) throws IOException {
        rootDirectory = _rootDirectory;
        providerName = _providerName;

        setupHook();
        String url = startServer();
        return url;
    }

    private void setupHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    stopServer();
                } catch (ProActiveException e) {
                    throw new ProActiveRuntimeException(e);
                }
            }
        });
    }

    private String startServer() throws IOException {
        if (providerName == null)
            deployer = new FileSystemServerDeployer(rootDirectory, true);
        else
            deployer = new FileSystemServerDeployer(providerName, rootDirectory, true);

        final String url = deployer.getVFSRootURL();
        System.out.println("PAProvider successfully started.\nVFS URL of this provider: " + url);
        return url;
    }

    public void stopServer() throws ProActiveException {
        if (deployer != null) {
            deployer.terminate();
            deployer = null;
        }
    }

    public boolean isServerStarted() {
        return deployer != null;
    }
}
