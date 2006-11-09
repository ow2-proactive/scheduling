package net.coregrid.gcmcca.wrappers.composite;

import mocca.cca.ComponentID;
import mocca.cca.ports.GoPort;
import mocca.client.MoccaBuilderClient;
import mocca.client.MoccaMainBuilder;



/**
 * @author malawski
 * 
 * The wrapper should extend the abstract wrapper class and
 * implement all server interfaces of the 
 * enclosed CCA system
 *
 * The code can be generated automatically from the a component 
 * type description (from ADL + ...?)
 */
public class FractalCompositeStarterComponent extends
		AbstractCCACompositeComponent implements GoPort {
	
	
	//to be generated
	public FractalCompositeStarterComponent() {} //proactive requires this...
	

	//to be generated
	public FractalCompositeStarterComponent(CCACompositeDescriptor desc) throws Exception {
		super(desc);
	}

	
	//to be generated
	public int go() {
		try {
			MoccaMainBuilder builder = new MoccaMainBuilder();
			ComponentID cid = builder.getDeserialization(this.compositeDescription.getServerComponentID("MyGoPort"));
			MoccaBuilderClient.invokeMethodOnComponent(cid,	GoPort.class.getName(), "MyGoPort", "go", new Object[] {} );
			return 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

}
