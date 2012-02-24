importPackage(java.lang);
importPackage(java.io);

// Executes a native command 
var pb = new ProcessBuilder(args);

// Merge stdout, stderr of process
pb.redirectErrorStream(true);

var process = null;
try {	
	try {
		process = pb.start();
	} catch (e) {
		throw "Unable to start native process: " + e.javaException;
	}
	
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
} finally {
	if (process != null) {
		process.destroy();
	}
}