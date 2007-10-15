package active;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.NodeException;

public class Main{
	public static void main(String args[])
	{
		try{
			HelloWorld	ao=(HelloWorld) ProActiveObject.newActive( 
				HelloWorld.class.getName(), //instantiation class 
				null); // constructor arguments
			System.out.println(ao.sayHello()); //possible wait-by-necessity
		}
		catch (NodeException nodeExcep){
			System.err.println(nodeExcep.getMessage());
		}
		catch (ActiveObjectCreationException aoExcep) {
			System.err.println(aoExcep.getMessage());
		}
 		//quitting
		ProActive.exitSuccess();
	}
}