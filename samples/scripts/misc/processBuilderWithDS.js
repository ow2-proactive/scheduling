importPackage(java.lang);
importPackage(java.io);
importPackage(java.util);
importPackage(org.objectweb.proactive.api);
importPackage(org.objectweb.proactive.core.node);
importPackage(org.objectweb.proactive.extensions.dataspaces.core);
importPackage(org.objectweb.proactive.extensions.dataspaces.api);
importPackage(org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast);

// Awaited arguments:
// args[0] = url of the naming service
// args[1..n] = command

var url = args[0];
var cmd = Arrays.copyOfRange(args, 1, args.length);

var localNode = null;
var process = null;

try {
	var localSpaceDir = null;
	// Init dataspaces
	try {

		localNode = PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis());
	        DataSpacesNodes.configureApplication(localNode, 0xcafe, url);
		var localSpaceRoot = PADataSpaces.resolveScratchForAO();
		var remoteSpaceRoot = PADataSpaces.resolveOutput(PADataSpaces.DEFAULT_IN_OUT_NAME);
		var inputDsfo = remoteSpaceRoot.findFiles(org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_SELF).get(0);
		localSpaceRoot.copyFrom(inputDsfo, FileSelector.SELECT_ALL);
		localSpaceDir = localSpaceRoot.getRealURI().replace("file://", "");
	} catch (e) {
	   	throw "Unable to copy files from remote dataspace: " + e.javaException;
	}

	var pb = new ProcessBuilder(cmd);
	pb.redirectErrorStream(true);
	pb.directory(new File(localSpaceDir));

	try {
		println("Executing native command in dir: " + localSpaceDir);
		process = pb.start();
	} catch (e) {
		throw "Unable to start native process: " + e.javaException;
	}

	println("Reading process input stream (" + cmd + ")");
	var isr = new InputStreamReader(process.getInputStream());
	var br = new BufferedReader(isr);

	var lineRead;
	try {
		while ((lineRead = br.readLine()) != null) {
			println(lineRead);
		}
	} catch (e) {
		throw "Unable to read native process output: " + e.javaException;
	}

	var exitValue = -1;
	try {
		rc = process.waitFor();
	} catch (e) {
		// Can be an InterruptedException
		if (e.javaException instanceof InterruptedException){
			Thread.currentThread().interrupt();
		} else {
			throw "Unable to wait for the end of native process: " +  e;
		}
	}
	println("Script sucessful");

} finally {
	if (process != null) {
		process.destroy();
	}

	// Close dataspaces
	try {
		DataSpacesNodes.tryCloseNodeApplicationConfig(localNode);
	} catch (e) {
		println("Unable to close dataspaces: " + e.javaException);
	}
}