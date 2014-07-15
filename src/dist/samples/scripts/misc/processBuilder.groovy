// Executes a native command
def pb = new ProcessBuilder(args);

// Merge stdout, stderr of process
pb.redirectErrorStream(true);

def process = null;
try {	
	try {
		process = pb.start();
	} catch (e) {
		throw new Exception("Unable to start native process: " , e);
	}
	
	def isr = new InputStreamReader(process.getInputStream());
	def br = new BufferedReader(isr);
	
	def lineRead;
	try {
		while ((lineRead = br.readLine()) != null) {
			println(lineRead);
		}
	} catch (e) {
		throw new Exception("Unable to read native process output: " ,e);
	}
	
	def exitValue = -1;
	try {
		rc = process.waitFor();
	} catch (e) {		
		// Can be an InterruptedException
		if (e instanceof InterruptedException){
			Thread.currentThread().interrupt();
		} else {
                  throw new Exception("Unable to wait for the end of native process: " ,  e);
            } 		
	}	
} finally {
	if (process != null) {
		process.destroy();
	}
}