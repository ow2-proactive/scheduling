/* Add all jars passed as parameters to the classpath of the forked JVM */
/* Those jars must be available in the input space of the jobs */

for(i=0;i<args.length;i++) {
	try{
		myjar = localspace.resolveFile(args[i]);
		myjar.copyFrom(input.resolveFile(args[i]), org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_SELF);
		forkEnvironment.addAdditionalClasspath(new java.net.URI(myjar.getRealURI()).getPath());
	} catch(e) {
		println(e)
		throw e;
	}
}
