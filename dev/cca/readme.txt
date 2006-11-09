GCM-CCA Interoperability: ProActive - MOCCA Implementation
Author: Maciej Malawski malawski@agh.edu.pl

1. Compiling:

Running 
	ant
should work to compile everything.

2. Running tests:

	ant runBasicTests
	
It may require adding junit.jar to the classpath when calling ant. 
It is located in lib directory.

This test starts H2O kernel, deploys MOCCA component, then creates a wrapper and connects 
it to ProActive component. After successfully passing "Hello" around, 
it stops the kernel and ProActive runtime.

Some logs are written to net.coregrid.gcmcca.test.MoccaProactiveTestSuite.xml
and kernel.log files.

3. Running examples:

- There are ant tasks for running primitive wrapper generator, ADL generator and 
running primitive wrapper. 

- To run composite wrapper, first use lib/h2o/h2o-kernel to start the kernel
and then use ant task to run composite wrapper (FIXME: kernel URI is hardcoded).
