// Executes a native command
var pb = new java.lang.ProcessBuilder(args);

// Merge stdout, stderr of process
pb.redirectErrorStream(true);

var process = null;
try {	
	try {
		process = pb.start();
	} catch (e) {
		throw "Unable to start native process: " + e;
	}
	
	var isr = new InputStreamReader(process.getInputStream());
	var br = new BufferedReader(isr);
	
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
		if (e.javaException instanceof InterruptedException){
			Thread.currentThread().interrupt();
		} else {
                  throw "Unable to wait for the end of native process: " +  e;
            } 		
	}	
} finally {
	if (process != null) {
		process.destroy();
	}
}