
// Awaited args:
// args[0] = url of the naming service
// args[1..n] = command

var url = args[0];
var cmd = [];
for(i=1;i<args.length;i++) {
    cmd[i-1] = args[i]
}

var localNode = null;
var process = null;

try {

	var localSpaceDir = null;
	// Init dataspaces
	try {

		localNode = org.objectweb.proactive.api.PAActiveObject.getActiveObjectNode(org.objectweb.proactive.api.PAActiveObject.getStubOnThis());
        org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes.configureApplication(localNode, 0xcafe, url);
		var localSpaceRoot = org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces.resolveScratchForAO();
		var remoteSpaceRoot = org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces.resolveOutput(org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces.DEFAULT_IN_OUT_NAME);
		var inputDsfo = remoteSpaceRoot.findFiles(org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_SELF).get(0);
		localSpaceRoot.copyFrom(inputDsfo, org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_ALL);
		localSpaceDir = localSpaceRoot.getRealURI().replace("file://", "");
	} catch (e) {
	   	throw "Unable to copy files from remote dataspace: " + e;
	}

	var pb = new java.lang.ProcessBuilder(cmd);
	pb.redirectErrorStream(true);
	pb.directory(new File(localSpaceDir));

	try {
		process = pb.start();
	} catch (e) {
		throw "Unable to start native process: " + e;
	}

	var isr = new java.io.InputStreamReader(process.getInputStream());
	var br = new java.io.BufferedReader(isr);

	var lineRead;
	try {
		while ((lineRead = br.readLine()) != null) {
			println(lineRead);
		}
	} catch (e) {
		throw "Unable to read native process output: " + e;
	}

	var exitValue = -1;
	try {
		rc = process.waitFor();
	} catch (e) {
		// Can be an InterruptedException
		if (e.javaException instanceof java.lang.InterruptedException){
			Thread.currentThread().interrupt();
		} else {
			throw "Unable to wait for the end of native process: " +  e;
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