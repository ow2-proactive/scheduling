// Awaited args:
// args[0] = url of the naming service
// args[1..n] = command

def url = args[0];
def cmd = [];
for (i = 1; i < args.size(); i++) {
    cmd[i-1] = args[i]
}

def localNode = null;
def process = null;

try {

    def localSpaceDir = null;
    // Init dataspaces
    try {

        localNode = org.objectweb.proactive.api.PAActiveObject.getActiveObjectNode(org.objectweb.proactive.api.PAActiveObject.getStubOnThis());
        org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes.configureApplication(localNode, 0xcafe, url);
        def localSpaceRoot = org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces.resolveScratchForAO();
        def remoteSpaceRoot = org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces.resolveOutput(org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces.DEFAULT_IN_OUT_NAME);
        def inputDsfo = remoteSpaceRoot.findFiles(org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_SELF).get(0);
        localSpaceRoot.copyFrom(inputDsfo, org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_ALL);
        localSpaceDir = localSpaceRoot.getRealURI().replace("file://", "");
    } catch (e) {
        throw new Exception("Unable to copy files from remote dataspace: ", e);
    }

    def pb = new java.lang.ProcessBuilder(cmd);
    pb.redirectErrorStream(true);
    pb.directory(new File(localSpaceDir));

    try {
        process = pb.start();
    } catch (e) {
        throw new Exception("Unable to start native process: " + cmd, e);
    }

    def isr = new java.io.InputStreamReader(process.getInputStream());
    def br = new java.io.BufferedReader(isr);

    def lineRead;
    try {
        while ((lineRead = br.readLine()) != null) {
            println(lineRead);
        }
    } catch (e) {
        throw new Exception("Unable to read native process output: ", e);
    }

    def exitValue = -1;
    try {
        rc = process.waitFor();
    } catch (e) {
        // Can be an InterruptedException
        if (e instanceof java.lang.InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new Exception("Unable to wait for the end of native process: ", e);
        }
    }

} finally {
    if (process != null) {
        process.destroy();
    }

    // Close dataspaces
    try {
        org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes.tryCloseNodeApplicationConfig(localNode);
    } catch (e) {
        println("Unable to close dataspaces: " + e);
    }
}