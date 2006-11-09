package glue.net.coregrid.gcmcca.example;

import net.coregrid.gcmcca.example.HelloPort;
import net.coregrid.gcmcca.wrappers.composite.WrapperAttributes;
import mocca.cca.CCAException;
import mocca.cca.ComponentID;
import mocca.cca.ports.BuilderService;
import mocca.client.MoccaBuilderClient;
import mocca.client.MoccaMainBuilder;


public class HelloPortFractalGlueComponent implements HelloPort, WrapperAttributes {

	//id of wrapped CCA component
	private ComponentID componentID;
	
	public String getComponentID() {
		// TODO Auto-generated method stub
		try {
			return componentID.getSerialization();
		} catch (CCAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void setComponentID(String componentID) {
		//how to get builder...? As we are in the wrapper, then it is OK
		//TODO: change the way we obtain builder
		BuilderService builder = new MoccaMainBuilder();
		try {
			this.componentID = builder.getDeserialization(componentID);
		} catch (CCAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public String hello(String s) {
		// here we must delegate the call to a CCA Component
		try {
			return (String) MoccaBuilderClient.invokeMethodOnComponent(componentID, 
					HelloPort.class.getName(), "h", "hello", new Object[] {s});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error" + e.getMessage();
		}
	}

}
