package glue.mocca.cca.ports;

import net.coregrid.gcmcca.wrappers.composite.WrapperAttributes;
import mocca.cca.CCAException;
import mocca.cca.ComponentID;
import mocca.cca.ports.BuilderService;
import mocca.cca.ports.GoPort;
import mocca.client.MoccaBuilderClient;
import mocca.client.MoccaMainBuilder;

public class GoPortFractalGlueComponent implements GoPort, WrapperAttributes {

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

//	to be generated
	public int go() {
		System.out.println("Go invoked!");
		try {
			MoccaBuilderClient.invokeMethodOnComponent(this.componentID,
					GoPort.class.getName(), "MyGoPort", "go", new Object[] {} );
			return 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
}
