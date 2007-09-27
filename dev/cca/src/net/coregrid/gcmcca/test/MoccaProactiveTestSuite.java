package net.coregrid.gcmcca.test;

import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.examples.hello.Hello;

import mocca.test.suites.MoccaAbstractTestSuite;



public class MoccaProactiveTestSuite extends MoccaAbstractTestSuite {
	                
    public void init() throws Exception {
    	//this hack makes ProActive class server start before any RMI communicaion is initiated
        ProActive.newActive(Hello.class.getName(), null);
        
        //here we add test cases to the suite
		addTest(new CompositeControllerWrapperTest());
	}

	public String getCACERTS_FILE() {
		return "/net/coregrid/gcmcca/test/config/security/h2otestCACerts.ks";
	}

	public char[] getCACERTS_FILE_PASSWORD() {
		return "h2o-trustedCodeCerts".toCharArray();
	}

	public String getKERNEL_CONFIG_FILE() {
		return "/net/coregrid/gcmcca/test/config/BasicTestKernelConfig.xml";
	}

	public String getPOLICY_FILE() {
		return "/net/coregrid/gcmcca/test/config/security/Policy.xml";
	}

	public String getPRIVKEY_FILE() {
		return "/net/coregrid/gcmcca/test/config/security/h2otestKeyStore.ks";
	}

	public char[] getPRIVKEY_FILE_PASSWORD() {
		return "h2o-trustedCodeCerts".toCharArray();
	}

	public String getUSERS_FILE() {
		return "/net/coregrid/gcmcca/test/config/security/Users.xml";
	}
      
 
}
