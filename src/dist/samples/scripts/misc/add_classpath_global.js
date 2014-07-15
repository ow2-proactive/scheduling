/* Add all jars passed as parameters to the classpath of the forked JVM */
/* Those jars must be available in the global space */

for(i=0;i<args.length;i++) {
	try{
		myjar = globalspace.resolveFile(args[i]);
        path = new java.net.URI(myjar.getRealURI()).getPath();
        if (!myjar.exists()) {
            throw IllegalStateException(path + " doesn't exist");
        }
		forkEnvironment.addAdditionalClasspath(path);
	} catch(e) {
		println(e)
		throw e;
	}
}