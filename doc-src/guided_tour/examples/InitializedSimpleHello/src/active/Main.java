package active;
import java.io.IOException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeException;

public class Main{
	public static void main(String args[])
	{
		try{
			InitializedHelloWorld ao=(InitializedHelloWorld) ProActive.newActive( 
				InitializedHelloWorld.class.getName(), //instantiation class 
				null); // constructor arguments
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
		//quitting
		ProActive.exitSuccess();
	}
}