package active;
import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.api.ProActiveObject;

public class Main{
	public static void main(String args[])
	{
		String path = new String("deployment.xml");
		ProActiveDescriptor pad;
		VirtualNode workers; 
		Node node;
		try {
			pad = ProDeployment.getProactiveDescriptor(path);
			workers = pad.getVirtualNode("Agent");
			// Returns the VirtualNode Workers described
			// in the xml file as a java object
			workers.activate();
			// Activates the VirtualNode
			node = workers.getNode();
			// Returns the first node available among nodes
			// mapped to the VirtualNode
//			InitializedHelloWorld ao=(InitializedHelloWorld) ProActiveObject.newActive( 
//				InitializedHelloWorld.class.getName(), //instantiation class 
//				null); // constructor arguments
			InitializedHelloWorld ao = (InitializedHelloWorld)ProActiveObject.newActive(
					InitializedHelloWorld.class.getName(),
		            new Object [] {},
		            node);
			System.out.println(ao.sayHello()); //possible wait-by-necessity
			ao.terminate();
		}
		catch (NodeException nodeExcep){
			System.err.println(nodeExcep.getMessage());
		}
		catch (ActiveObjectCreationException aoExcep){
			System.err.println(aoExcep.getMessage());
		}
		catch(IOException ioExcep){
			System.err.println(ioExcep.getMessage());
		}
		catch(ProActiveException proExcep){
			System.err.println(proExcep.getMessage());
		}
		//quitting
	}
}